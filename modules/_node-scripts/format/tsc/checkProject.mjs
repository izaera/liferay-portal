/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {$} from 'execa';
import path from 'path';
import resolve from 'resolve';

import {
	PLAYWRIGHT_DIR,
	SRC_TSCONFIG_PATH,
	TEST_TSCONFIG_PATH,
	getNodeDirPath,
} from '../../util/constants.mjs';
import fileExists from '../../util/fileExists.mjs';

/**
 * @returns string|boolean
 * The output of the tsc command if captureOutput is passed as true or a boolean indicating if the
 * check was successful otherwise.
 */
export default async function checkProject(projectDir, captureOutput) {
	const tscPath = resolve.sync('typescript/bin/tsc', {basedir: '.'});

	if (projectDir === PLAYWRIGHT_DIR) {
		const nodeDirPath = await getNodeDirPath();

		const {all, exitCode} = await $({
			all: true,
			cwd: projectDir,
			reject: false,
			stdout: 'pipe',
		})`${path.join(nodeDirPath, 'bin', 'npm')} install`;

		if (exitCode !== 0) {
			if (captureOutput) {
				return all;
			}
			else {
				console.log(all);

				return false;
			}
		}
	}

	let content = '';
	let total = 0;

	const configArg = (await fileExists(
		path.join(projectDir, SRC_TSCONFIG_PATH)
	))
		? ` -b ${SRC_TSCONFIG_PATH}`
		: '';

	const {all} = await $({
		all: true,
		cwd: projectDir,
		reject: false,
		stdout: captureOutput ? 'pipe' : ['inherit', 'pipe'],
	})`${tscPath}${configArg}`;

	content += all;
	total += all.trim().length;

	if (await fileExists(path.join(projectDir, TEST_TSCONFIG_PATH))) {
		const {all: testAll} = await $({
			all: true,
			cwd: projectDir,
			reject: false,
			stdout: captureOutput ? 'pipe' : ['inherit', 'pipe'],
		})`${tscPath} -b ${TEST_TSCONFIG_PATH}`;

		content += '\n' + testAll;
		total += testAll.trim().length;
	}

	return captureOutput ? content : total;
}

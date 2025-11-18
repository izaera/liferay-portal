/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import os from 'os';
import path from 'path';

import {
	PLAYWRIGHT_DIR,
	SRC_TSCONFIG_PATH,
	getRootDir,
} from '../../util/constants.mjs';
import fileExists from '../../util/fileExists.mjs';
import indent from '../../util/indent.mjs';
import runConcurrentTasks from '../../util/runConcurrentTasks.mjs';
import checkProject from './checkProject.mjs';

/**
 * Run TypeScript checks in several projects in parallel.
 */
export default async function checkTypeScript(projectDirs) {
	let checksPassed = true;

	console.log('\n\n🔍️️️ Checking TypeScript projects...\n');

	const cpuCount = os.cpus().length;
	const rootDir = await getRootDir();

	if (projectDirs.length > 1) {
		console.log(
			indent(
				4,
				`ℹ️  A total of ${cpuCount} CPUs were detected: launching 'tsc' using ${cpuCount} workers\n`
			)
		);
	}

	await runConcurrentTasks(
		projectDirs.map((projectDir) => async () => {
			if (
				!(await fileExists(path.join(projectDir, SRC_TSCONFIG_PATH))) &&
				projectDir !== PLAYWRIGHT_DIR
			) {
				return;
			}

			let icon = '✅';
			let output = await checkProject(projectDir, true);

			output = output.trim();

			if (output) {
				checksPassed = false;
				icon = '❌';
				output = `\n${output
					.split('\n')
					.map((line) => `   ${line}`)
					.join('\n')}\n`;
			}

			console.log(
				indent(
					4,
					`${icon} Checked '${path.relative(rootDir, projectDir)}${output}'`
				)
			);
		})
	);

	return checksPassed;
}

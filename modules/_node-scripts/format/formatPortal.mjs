/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {PLAYWRIGHT_DIR, getProjectDirs} from '../util/constants.mjs';
import getNamedArguments from '../util/getNamedArguments.mjs';
import checkTypeScript from './tsc/checkTypeScript.mjs';
import checkAPISubmodules from './util/checkAPISubmodules.mjs';
import checkConfigFileNames from './util/checkConfigFileNames.mjs';
import checkPackageJSONFiles from './util/checkPackageJSONFiles.mjs';
import checkYarnLock from './util/checkYarnLock.mjs';
import formatGlobalNodeScripts from './util/formatGlobalNodeScriptsConfig.mjs';
import formatNodeScriptsHash from './util/formatNodeScriptsHash.mjs';
import formatSourceFiles from './util/formatSourceFiles.mjs';
import formatTsconfigFiles from './util/formatTsconfigFiles.mjs';

export default async function formatPortal(
	check,
	portalDir,
	rootDir,
	currentDir,
	files
) {
	const {emitSuppressed, ignoreTypescript} = getNamedArguments({
		emitSuppressed: '--emit-suppressed',
		ignoreTypescript: '--ignore-typescript',
	});

	let checksPassed = true;

	if (
		(!files ||
			!!files.find((file) => file.endsWith('/node-scripts.config.js'))) &&
		!(await formatGlobalNodeScripts(check, portalDir, rootDir))
	) {
		checksPassed = false;
	}

	if (
		(!files || !!files.find((file) => file.endsWith('/package.json'))) &&
		!(await formatTsconfigFiles(check, portalDir))
	) {
		checksPassed = false;
	}

	if (!(await formatSourceFiles(check, emitSuppressed, files))) {
		checksPassed = false;
	}

	if (
		(!files || !!files.find((file) => file.includes('/_node-scripts/'))) &&
		!(await formatNodeScriptsHash(check))
	) {
		checksPassed = false;
	}

	if (
		(!files ||
			!!files.find((file) => file.endsWith('/node-scripts.config.js'))) &&
		!(await checkAPISubmodules())
	) {
		checksPassed = false;
	}

	if (!(await checkConfigFileNames())) {
		checksPassed = false;
	}

	if (
		(!files || !!files.find((file) => file.endsWith('/package.json'))) &&
		!(await checkPackageJSONFiles())
	) {
		checksPassed = false;
	}

	if (
		(!files || !!files.find((file) => file.endsWith('/yarn.lock'))) &&
		!(await checkYarnLock())
	) {
		checksPassed = false;
	}

	if (
		!ignoreTypescript &&
		(!files ||
			!!files.find(
				(file) =>
					file.endsWith('/node-scripts.config.js') ||
					file.endsWith('/package.json') ||
					file.endsWith('.ts') ||
					file.endsWith('.tsx')
			))
	) {
		const projectDirs = await getProjectDirs();

		projectDirs.push(PLAYWRIGHT_DIR);

		if (!(await checkTypeScript(projectDirs))) {
			checksPassed = false;
		}
	}

	return checksPassed;
}

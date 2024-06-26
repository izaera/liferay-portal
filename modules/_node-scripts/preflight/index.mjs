/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {checkBannedDependencies} from './checkBannedDependencies.mjs';
import {checkConfigFileNames} from './checkConfigFileNames.mjs';
import {checkDependencyVersions} from './checkDependencyVersions.mjs';
import {checkMainEntryPoints} from './checkMainEntryPoints.mjs';
import {checkNpmScripts} from './checkNpmScripts.mjs';
import {checkPackageNames} from './checkPackageNames.mjs';
import {checkTsc} from './checkTsc.mjs';
import {checkYarnLock} from './checkYarnLock.mjs';
import getPackageJSONFiles from './util/getPackageJSONFiles.mjs';

/**
 * Runs the "preflight" checks (basically everything that is not already covered
 * by Prettier or ESLint).
 */
export default async function preflight({allFiles} = {allFiles: false}) {
	const packageJSONFiles = await getPackageJSONFiles();

	const results = await Promise.all([
		checkBannedDependencies(packageJSONFiles),
		checkConfigFileNames(),
		checkDependencyVersions(packageJSONFiles),
		checkMainEntryPoints(packageJSONFiles),
		checkNpmScripts(packageJSONFiles),
		checkPackageNames(packageJSONFiles),
		checkYarnLock(),
		checkTsc({allFiles}),
	]);

	const errors = results.flat();

	if (errors.length) {
		console.error(`
❌ Preflight check failed:
${errors.map((error) => `   · ${error}`).join('\n')}
`);

		throw new Error();
	}
}

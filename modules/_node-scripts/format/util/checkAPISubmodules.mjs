/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fg from 'fast-glob';
import path from 'path';

import {getRootDir} from '../../util/constants.mjs';
import indent from '../../util/indent.mjs';

const REGEX_API_DIR = /\/resources\/js\/api\/api\.(js|ts)$/;

/**
 * Runs checks against node-scripts.config.js files; detects:
 *
 * - invalid 'api' submodule paths
 *
 * Returns a (possibly empty) array of error messages.
 */
export default async function checkAPISubmodules() {
	let checksPassed = true;

	console.log('\n\n🔍️️️ Checking API submodules...\n');

	const nodeScriptConfigs = await fg('**/node-scripts.config.js', {
		ignore: ['**/build', '**/classes', '**/node_modules'],
	});

	const rootDir = await getRootDir();

	const configs = await Promise.all(
		nodeScriptConfigs.map(async (configPath) => {
			const module = await import(path.join(rootDir, configPath));

			return {config: module.default, path: configPath};
		})
	);

	const configsWithAPISubmodule = configs.filter(({config}) => {
		return config?.submodules?.api;
	});

	configsWithAPISubmodule.forEach(({config, path}) => {
		if (!REGEX_API_DIR.test(config.submodules.api)) {
			console.log(
				indent(
					4,
					`❌ Invalid API module at path ${path} (must be located in /resources/js/api/api.{js|ts})`
				)
			);

			checksPassed = false;
		}
		else {
			console.log(indent(4, `✅ Checked '${path}'`));
		}
	});

	return checksPassed;
}

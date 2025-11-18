/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import createGlobalConfig from '../../util/createGlobalConfig.mjs';
import indent from '../../util/indent.mjs';
import projectScopeRequire from '../../util/projectScopeRequire.mjs';

export default async function formatGlobalNodeScripts(
	check,
	portalDir,
	rootDir
) {
	let checksPassed = true;

	console.log(
		`\n\n🔍️️️ ${check ? 'Checking' : 'Formatting'} global 'node-scripts.config.js' file...\n`
	);

	const globalConfig = await projectScopeRequire(
		'./node-scripts.config.js',
		rootDir
	);

	const {config: newGlobalConfig, hash: newHash} = await createGlobalConfig();

	if (globalConfig.hash !== newHash) {
		if (check) {
			console.log(
				indent(4, `❌ Outdated global 'node-scripts.config.js' found`)
			);

			checksPassed = false;
		}
		else {
			const globalConfigPath = path.join(
				rootDir,
				'node-scripts.config.js'
			);

			await fs.writeFile(globalConfigPath, newGlobalConfig, 'utf-8');

			console.log(
				indent(
					4,
					`✍️  Regenerated '${path.relative(portalDir, globalConfigPath)}'`
				)
			);
		}
	}
	else {
		console.log(indent(4, `✅ Checked 'node-scripts.config.js'`));
	}

	return checksPassed;
}

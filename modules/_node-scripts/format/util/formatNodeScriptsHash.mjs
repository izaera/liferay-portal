/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import {NODE_SCRIPTS_DIR} from '../../util/constants.mjs';
import digestNodeScripts from '../../util/digestNodeScripts.mjs';
import indent from '../../util/indent.mjs';
import objectSF from '../../util/objectSF.mjs';

export default async function formatNodeScriptsHash(check) {
	let checksPassed = true;

	console.log(
		`\n\n🔍️️️ ${check ? 'Checking' : 'Formatting'} '@liferay/node-scripts' hash...\n`
	);

	const expectedHash = await digestNodeScripts();

	const packageJSONPath = path.join(NODE_SCRIPTS_DIR, 'package.json');

	const packageJSON = JSON.parse(await fs.readFile(packageJSONPath, 'utf-8'));

	if (packageJSON['com.liferay']['sha256'] !== expectedHash) {
		if (check) {
			console.log(
				indent(
					4,
					`❌ Incorrect 'com.liferay.sha256' field found in '@liferay/node-scripts/package.json' (expected: ${expectedHash})`
				)
			);

			checksPassed = false;
		}
		else {
			packageJSON['com.liferay']['sha256'] = expectedHash;

			await fs.writeFile(packageJSONPath, objectSF(packageJSON), 'utf-8');

			console.log(
				indent(
					4,
					`✍️  Updated 'com.liferay.sha256' field in '@liferay/node-scripts/package.json'`
				)
			);
		}
	}
	else {
		console.log(indent(4, `✅ Checked '@liferay/node-scripts'`));
	}

	return checksPassed;
}

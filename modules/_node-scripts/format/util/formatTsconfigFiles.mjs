/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import indent from '../../util/indent.mjs';
import visitOutdatedTsconfigFiles from '../tsconfig/visitOutdatedTsconfigFiles.mjs';

export default async function formatTsconfigFiles(check, portalDir) {
	let checksPassed = true;

	console.log(
		`\n\n🔍️️️ ${check ? 'Checking' : 'Formatting'} 'tsconfig.json' files...\n`
	);

	let outdatedFilesFound = false;

	await visitOutdatedTsconfigFiles(async (filePath, json) => {
		outdatedFilesFound = true;

		if (check) {
			console.log(indent(4, `❌ Outdated file '${filePath}' found`));

			checksPassed = false;
		}
		else {
			await fs.writeFile(filePath, json, 'utf-8');

			console.log(
				indent(
					4,
					`✍️  Regenerated '${path.relative(portalDir, filePath)}'`
				)
			);
		}
	});

	if (!outdatedFilesFound) {
		console.log(indent(4, `✅ No outdated 'tsconfig.json' files found`));
	}

	return checksPassed;
}

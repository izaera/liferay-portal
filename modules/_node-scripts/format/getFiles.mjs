/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fg from 'fast-glob';
import path from 'path';

import getNamedArguments from '../util/getNamedArguments.mjs';
import gitUtil from '../util/gitUtil.mjs';

export default async function getFiles(portalDir, rootDir, currentDir) {
	const {currentBranch, localChanges} = getNamedArguments({
		currentBranch: '--current-branch',
		localChanges: '--local-changes',
	});

	let files;

	if (currentDir === rootDir) {
		if (currentBranch) {
			files = await gitUtil('current-branch');
		}
		else if (localChanges) {
			files = await gitUtil('local-changes');
		}
		else {
			files = undefined;
		}
	}
	else {
		if (currentBranch || localChanges) {
			console.error(`
❌ Arguments --current-branch or --local-changes are not valid when formatting a single project.
`);

			process.exit(2);
		}

		files = await fg(['**/*'], {
			cwd: currentDir,
			dot: true,
			ignore: ['build/**', 'node_modules/**'],
		});

		files = files
			.map((file) => path.resolve(currentDir, file))
			.map((file) => path.relative(portalDir, file));
	}

	return files;
}

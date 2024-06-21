/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fg from 'fast-glob';
import fs from 'fs';
import fsPromises from 'fs/promises';
import path from 'path';

export default async function getPackageJSONFiles() {
	let packagePaths = await fg('**/package.json', {
		ignore: [
			'_node-scripts',
			'**/build',
			'**/classes',
			'**/frontend-js-jquery-web',
			'**/node_modules',
			'**/osb-faro-theme',
			'**/osb-faro-web',
			'**/sdk',
			'test',
		],
	});

	// Filters out packages that have their own yarn.lock

	packagePaths = packagePaths.filter((packagePath) => {

		// Ignore root level package.json

		if (packagePath === 'package.json') {
			return true;
		}

		return !fs.existsSync(
			path.join(path.dirname(packagePath), 'yarn.lock')
		);
	});

	// Read package.json files

	return await Promise.all(
		packagePaths.map(async (pkg) => ({
			json: JSON.parse(await fsPromises.readFile(pkg, 'utf-8')),
			path: pkg,
		}))
	);
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';
import url from 'url';

import getDependenciesVersionNumbers from './util/getDependenciesVersionNumbers.mjs';

/**
 * Matches all projects' dependencies between them and shows those that are not the same. Then it
 * does the same with devDependencies.
 */
export async function checkDependencyVersions(packageJSONFiles) {
	const errors = [];

	const __dirname = path.dirname(url.fileURLToPath(import.meta.url));

	packageJSONFiles = [
		...packageJSONFiles,
		{
			json: JSON.parse(
				await fs.readFile(
					path.join(__dirname, '..', 'package.json'),
					'utf-8'
				)
			),
			path: '_node-scripts/package.json',
		},
	];

	const devDependenciesVersionNumbers = getDependenciesVersionNumbers(
		packageJSONFiles,
		'devDependencies'
	);

	for (const [dependency, {paths, versions}] of Object.entries(
		devDependenciesVersionNumbers
	)) {
		if (versions.size > 1) {
			errors.push(
				`Multiple versions for development dependency '${dependency}' ` +
					`have been found: ${[...versions].join(', ')}\n` +
					`${paths.map((path) => `       - ${path}`).join('\n')}`
			);
		}
	}

	const dependenciesVersionNumbers = getDependenciesVersionNumbers(
		packageJSONFiles,
		'dependencies'
	);

	for (const [dependency, {paths, versions}] of Object.entries(
		dependenciesVersionNumbers
	)) {
		if (versions.size > 1) {
			errors.push(
				`Multiple versions for regular dependency '${dependency}' ` +
					`have been found: ${[...versions].join(', ')}\n` +
					`${paths.map((path) => `       - ${path}`).join('\n')}`
			);
		}
	}

	return errors;
}

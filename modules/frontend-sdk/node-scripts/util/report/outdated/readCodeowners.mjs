/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

/**
 * Looks for a `.github/CODEOWNERS` file in `dir` or any of its ancestors and
 * returns a function that gives the owners of a directory, or `null` when no
 * such file exists.
 *
 * The file maps repository paths (no globs) to teams, so the most specific
 * path that contains the directory decides its owners. A path listed without
 * owners leaves its directory unowned.
 *
 * @return {?function(string): Array<string>} a function that takes an
 *     absolute directory and returns its owners (empty when unowned)
 */
export default async function readCodeowners(dir) {
	let repositoryDir = path.resolve(dir);
	let content;

	while (true) {
		try {
			content = await fs.readFile(
				path.join(repositoryDir, '.github', 'CODEOWNERS'),
				'utf8'
			);

			break;
		}
		catch (error) {
			if (error.code !== 'ENOENT') {
				throw error;
			}
		}

		const parentDir = path.dirname(repositoryDir);

		if (parentDir === repositoryDir) {
			return null;
		}

		repositoryDir = parentDir;
	}

	const owners = new Map();

	for (const line of content.split(/\r?\n/)) {
		const [pattern, ...teams] = line.trim().split(/\s+/);

		if (pattern && !pattern.startsWith('#')) {
			owners.set(pattern.replace(/^\/|\/$/g, ''), teams);
		}
	}

	return (absoluteDir) => {
		let relativePath = path
			.relative(repositoryDir, absoluteDir)
			.split(path.sep)
			.join('/');

		while (relativePath && relativePath !== '.') {
			if (owners.has(relativePath)) {
				return owners.get(relativePath);
			}

			const index = relativePath.lastIndexOf('/');

			relativePath = index === -1 ? '' : relativePath.substring(0, index);
		}

		return [];
	};
}

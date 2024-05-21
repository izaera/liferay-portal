/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import {ROOT_DIR, SRC_PATH} from '../util/constants.mjs';

const IGNORED_DIRS = ['modules'];
const NO_RECURSE_DIRS = [
	'_node-scripts', 'build', 'classes', 'node_modules', 'sdk', 'test'
];

/**
 * @returns
 * {
 *	 '@liferay/frontend-js-react-web': {
 *		base: 'modules/apps/frontend-js/frontend-js-react-web', 
 *		main: 'src/main/resources/META-INF/resources/js/index.ts'
 *	 },
 *	 ...
 * }
 */
export default async function getMainEntryPoints() {
	const dirs = await getFrontendProjectDirs(ROOT_DIR);

	const dirJsons = await Promise.all(dirs.map(getDirJson));

	const mainEntryPoints = {};

	for (const [dir, json] of dirJsons) {
		if (!json.main) {
			continue;
		}

		mainEntryPoints[json.name] = {
			base: path.relative(ROOT_DIR, dir),
			main: `${SRC_PATH}/${json.main}`
		};
	}

	return mainEntryPoints;

}

async function getDirJson(projectDir) {
	try {
		return [
			projectDir,
			JSON.parse(
				await fs.readFile(path.join(projectDir, 'package.json'), 'utf-8')
			)
		];
	}
	catch(error) {
		if (error.code !== 'ENOENT') {
			throw error;
		}

		return {};
	}
}

async function getFrontendProjectDirs(dir) {
	const projectDirs = [];

	for (const dirent of await fs.readdir(dir, {withFileTypes: true})) {
		if (dirent.name === 'package.json' && !IGNORED_DIRS.includes(path.basename(dir))) {
			projectDirs.push(path.resolve(dir));
			break;
		}
		else if (NO_RECURSE_DIRS.includes(dirent.name)) {
			continue;
		}
		else if (dirent.isDirectory()) {
			for (const childDir of await getFrontendProjectDirs(path.resolve(dir, dirent.name))) {
				projectDirs.push(childDir);
			}
		}
	}

	return projectDirs;
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs';
import path from 'path';

import {SRC_PATH} from '../util/constants.mjs';

export async function checkMainEntryPoints(packageJSONFiles) {
	const errors = [];

	packageJSONFiles.forEach((pkg) => {
		const bad = (message) => errors.push(`${pkg.path}: ${message}`);

		try {
			const {main} = pkg.json;

			const moduleDir = path.join(pkg.path, '..');

			// Check for main entry point

			if (!main) {
				const indexExists = [
					'index.js',
					'index.es.js',
					'index.ts',
					'index.tsx',
				].find(
					(file) =>
						fs.existsSync(path.join(moduleDir, SRC_PATH, file)) ||
						fs.existsSync(
							path.join(moduleDir, SRC_PATH, 'js', file)
						)
				);

				if (indexExists) {
					bad(
						`package.json doesn't contain a "main" entry point when you have an ${indexExists} file - https://github.com/liferay/liferay-frontend-projects/issues/719`
					);
				}
			}

			// Check that main entry point doesn't 'export default'

			if (main && main !== 'package.json') {
				const filePath = path.join(moduleDir, SRC_PATH, main);

				if (!fs.existsSync(filePath)) {
					bad(
						`package.json contains a "main" entry point that doesn't exist.`
					);
				}
				else {
					const entryFile = fs.readFileSync(filePath);

					if (entryFile.toString().match(/\s*export\s+default\s*/i)) {
						bad(
							`package.json's "main" entry point contains "export default". Use named exports only.`
						);
					}
				}
			}
		}
		catch (error) {
			bad(`error thrown during checks: ${error}`);
		}
	});

	return errors;
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const ALLOWED_NPM_SCRIPTS = {
	'build': [
		'liferay-npm-scripts theme build',
		'node-scripts build',
		'webpack',

		// Format for custom builds is node scripts/buildXXX.mjs. A custom build can appear alone or
		// after `node-scripts build`.

		/^node scripts\/build.*\.mjs$/,
		/^node-scripts build && node scripts\/build.*\.mjs$/,
	],
	'checkFormat': ['node-scripts format --check'],
	'coverage': [

		// Some scripts pass extra arguments.

		/^node-scripts test --coverage[^;&]*$/,
	],
	'format': ['node-scripts format'],
	'start': ['webpack serve'],
	'test': [

		// Some scripts define env vars before `node-scripts test` and others pass extra arguments.

		/^[^&;]*node-scripts test[^;&]*$/,
	],
	'test:all': ['node-scripts test'],
	'test:watch': ['node-scripts test --watch'],
};

export async function checkNpmScripts(packageJSONFiles) {
	const errors = [];

	packageJSONFiles.forEach((pkg) => {
		const bad = (message) => errors.push(`${pkg.path}: ${message}`);

		try {
			const {scripts} = pkg.json;

			if (scripts) {
				for (const name of Object.keys(scripts)) {
					if (!ALLOWED_NPM_SCRIPTS[name]) {
						bad(
							`package.json's script "${name}" is not allowed: ${scripts[name]}`
						);
					}
					else {
						const script = scripts[name];

						let ok = false;

						for (const expr of ALLOWED_NPM_SCRIPTS[name]) {
							if (
								(typeof expr === 'string' && script === expr) ||
								(expr instanceof RegExp && script.match(expr))
							) {
								ok = true;
								break;
							}
						}

						if (!ok) {
							bad(
								`package.json's script "${name}" contains invalid value: ${scripts[name]}`
							);
						}
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

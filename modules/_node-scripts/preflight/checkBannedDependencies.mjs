/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import path from 'path';

import {getRootDir} from '../util/constants.mjs';
import projectScopeRequire from '../util/projectScopeRequire.mjs';

const ALLOWED_NON_GLOBAL_DEPENDENCIES = [
	'@liferay/amd-loader',
	'@liferay/npm-scripts',
	'@vscode/ripgrep',
	'alloy-ui',
	'alloyeditor',
	'axios',
	'base64-js',
	'browser-tabs-lock',
	'ckeditor4-react',
	'ckeditor4',
	'codemirror',
	'core-js',
	'd3',
	'es-module-shims',
	'esbuild',
	'fetch-mock',
	'fs',
	'gulp',
	'hash.js',
	'history',
	'html-webpack-plugin',
	'jest-fetch-mock',
	'leaflet',
	'liferay-font-awesome',
	'liferay-theme-tasks',
	'lodash',
	'mini-css-extract-plugin',
	'minimist',
	'os-browserify',
	'path-browserify',
	'path-to-regexp',
	'react-dnd-test-utils',
	'recharts',
	'resize-observer-polyfill',
	'resolve',
	'swagger-ui-react',
	'timers-browserify',
	'webpack',
];

export async function checkBannedDependencies(packageJSONFiles) {
	const errors = [];

	const definedDependenciesSet = await collectDefinedDependencies();

	packageJSONFiles.forEach((pkg) => {
		const bad = (message) => errors.push(`${pkg.path}: ${message}`);

		try {
			const {dependencies} = pkg.json;

			const dependencyNames = dependencies
				? Object.keys(dependencies)
				: [];

			dependencyNames.forEach((name) => {
				if (
					!definedDependenciesSet.has(name) &&
					!ALLOWED_NON_GLOBAL_DEPENDENCIES.includes(name)
				) {
					bad(
						`dependency not provided by a specific module: ${name} - See https://issues.liferay.com/browse/LPS-168443\n`
					);
				}
			});
		}
		catch (error) {
			bad(`error thrown during checks: ${error}`);
		}
	});

	return errors;
}

async function collectDefinedDependencies() {
	let rootConfig = {};

	const rootDir = await getRootDir();

	if (!rootDir) {
		return new Set();
	}

	try {
		rootConfig = await projectScopeRequire(
			path.join(rootDir, 'node-scripts.config.js')
		);
	}
	catch (error) {
		return new Set();
	}

	const esmImports = new Set(
		Object.entries(rootConfig.imports).reduce(
			(acc, [moduleName, dependencies]) => {
				return [...acc, moduleName, ...dependencies];
			},
			[]
		)
	);

	return new Set([...esmImports]);
}

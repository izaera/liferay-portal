/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import {
	BUILD_SASS_CACHE_PATH,
	SRC_PATH,
	WORK_IMPORT_PATH,
} from '../../../util/constants.mjs';
import fileExists from '../../../util/fileExists.mjs';
import getFlatName from '../../../util/getFlatName.mjs';
import extractFileHash from '../../util/extractFileHash.mjs';
import getCSSLoadJavaScript from '../../util/getCSSLoadJavaScript.mjs';
import {
	getImportBridgeJavaScript,
	getImportBridgePath,
} from '../util/importBridge.mjs';

//
// CAVEATS FOR FUTURE DEVELOPERS:
//
// 1. Code generated via onLoad cannot rely on onResolve above, all generated
// imports must be already resolved because it looks like esbuild does not
// recursively apply onResolve/onLoad chains.
//
// That's one of the reasons why export bridges are written directly to disk
// instead of generating them on-the-fly, since they need esbuild resolution to
// happen for them to work.
//
// 2. Using resolve() resolutions inside onResolve (instead of returning {}) is
// not a good idea because it uses Node's resolution algorith, which may load
// server side versions of polymorphic libraries or even fail to resolve Node
// built-in packages.
//

/**
 * This plugin tells esbuild how to resolve paths, provides virtual temporary
 * modules that help linking stuff together, and generate helper code to load
 * CSS files.
 */
export default function getLinkerPlugin(
	globalImports,
	overridenPackageSymbols,
	projectWebContextPath,
	moduleName
) {
	return {
		name: 'linker-plugin',

		setup(build) {

			//
			// Resolutions
			//

			build.onResolve(
				{
					filter: /.*/,
				},
				(info) => {
					const {path} = info;

					// Resolve exported module locally (inside export briges)

					if (moduleName !== 'main' && path === moduleName) {
						return {};
					}

					// Resolve DXP runtime URLs as external

					if (path.includes('/__liferay__/')) {
						return {
							external: true,
							path,
						};
					}

					// Pass resolution of non global imports to esbuild

					const globalImport = globalImports[path];

					if (!globalImport) {
						return {};
					}

					// Resolve global imports

					const {external, submodule, webContextPath} = globalImport;

					if (path.endsWith('.css')) {

						// For CSS files use external URLs pointing to .js stubs

						return {
							external: true,
							path: `${webContextPath}/__liferay__/exports/${getFlatName(path)}.js`,
						};
					}
					else if (external) {

						// For externals use local import bridges

						return {
							path: getImportBridgePath(path),
						};
					}
					else if (submodule) {

						// For submodules use external URLs

						const submodulePath = path
							.split('/')
							.slice(path.startsWith('@') ? 2 : 1)
							.join('/');

						return {
							external: true,
							path: `${webContextPath}/__liferay__/${submodulePath}.js`,
						};
					}

					// For packages use external URLs

					return {
						external: true,
						path: `${webContextPath}/__liferay__/index.js`,
					};
				}
			);

			//
			// Import bridges instantiation and caching
			//

			build.onLoad(
				{
					filter: new RegExp(`.*/${WORK_IMPORT_PATH}/.*`),
				},
				async (info) => {
					const {path: filePath} = info;

					const importModuleName = path
						.basename(filePath)
						.replaceAll(/\.js$/g, '')
						.replaceAll('$', '/');

					const {webContextPath} = globalImports[importModuleName];

					const contents = await getImportBridgeJavaScript(
						overridenPackageSymbols,
						importModuleName,
						webContextPath
					);

					if (!(await fileExists(filePath))) {
						await fs.mkdir(path.dirname(filePath), {
							recursive: true,
						});
						await fs.writeFile(filePath, contents, 'utf-8');
					}

					return {
						contents,
						loader: 'js',
					};
				}
			);

			//
			// Local SASS imports JavaScript stub generator
			//

			build.onLoad(
				{
					filter: /\.scss$/,
				},
				async ({path: filePath}) => {
					const projectRelativeFilePath = path.relative(
						SRC_PATH,
						filePath
					);

					const projectRelativeBaseNamePath =
						projectRelativeFilePath.replace(/\.scss$/, '');

					const cssFiles = await fs.readdir(
						path.join(
							BUILD_SASS_CACHE_PATH,
							path.dirname(projectRelativeFilePath)
						)
					);

					const cssBasename = cssFiles.find((cssFile) =>
						cssFile.startsWith(
							`${path.basename(projectRelativeBaseNamePath)}.(`
						)
					);

					const hash = extractFileHash(cssBasename);

					const cssBaseURI = projectRelativeFilePath
						.split(path.sep)
						.join(path.posix.sep)
						.replace(/\.scss$/, '');

					return {
						contents: getCSSLoadJavaScript(
							projectWebContextPath,
							`${cssBaseURI}.(${hash}).css`
						),
						loader: 'js',
					};
				}
			);
		},
	};
}

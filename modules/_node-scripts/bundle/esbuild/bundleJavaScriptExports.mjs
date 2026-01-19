/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import path from 'path';
import Sonda from 'sonda/esbuild';

import {
	BUILD_MAIN_EXPORTS_PATH,
	BUNDLE_REPORTS_PATH,
} from '../../util/constants.mjs';
import getFlatName from '../../util/getFlatName.mjs';
import getLinkerPlugin from './plugins/getLinkerPlugin.mjs';
import {getExportBridgePath, writeExportBridge} from './util/exportBridge.mjs';
import relocateSourcemap from './util/relocateSourcemap.mjs';
import runEsbuild from './util/runEsbuild.mjs';

export default async function bundleJavaScriptExports(
	globalImports,
	overridenPackageSymbols,
	projectAlias,
	projectExports,
	projectWebContextPath
) {
	if (!projectExports.length) {
		return;
	}

	await Promise.all(
		projectExports
			.filter((moduleName) => !moduleName.endsWith('.css'))
			.map((moduleName) =>
				bundle(
					globalImports,
					overridenPackageSymbols,
					projectAlias,
					projectWebContextPath,
					moduleName
				)
			)
	);
}

async function bundle(
	globalImports,
	overridenPackageSymbols,
	projectAlias,
	projectWebContextPath,
	moduleName
) {
	const entryPoint = {
		in: getExportBridgePath(moduleName),
		out: `exports/${getFlatName(moduleName)}`,
	};

	const esbuildConfig = {
		alias: projectAlias,
		bundle: true,
		entryNames: '[dir]/[name].([hash])',
		entryPoints: [entryPoint],
		format: 'esm',
		outdir: BUILD_MAIN_EXPORTS_PATH,
		plugins: [
			getLinkerPlugin(
				globalImports,
				overridenPackageSymbols,
				projectWebContextPath,
				moduleName
			),
		],
		sourcemap: true,
		target: ['es2022'],
	};

	if (process.env.CREATE_BUNDLE_REPORTS) {
		esbuildConfig.plugins.push(
			Sonda({
				brotli: false,
				detailed: false,
				enabled: true,
				filename: path.join(
					BUNDLE_REPORTS_PATH,
					`${entryPoint.out}.html`
				),
				format: 'html',
				gzip: true,
				open: false,
				sources: false,
			}),
			Sonda({
				brotli: false,
				detailed: false,
				enabled: true,
				filename: path.join(
					BUNDLE_REPORTS_PATH,
					`${entryPoint.out}.json`
				),
				format: 'json',
				gzip: true,
				open: false,
			})
		);
	}

	await writeExportBridge(overridenPackageSymbols, moduleName);

	const {metafile} = await runEsbuild(esbuildConfig, getFlatName(moduleName));
	const {outputs} = metafile;

	await Promise.all([
		...Object.keys(outputs).map(async (output) => {
			if (output.endsWith('.map')) {
				return relocateSourcemap(
					path.join(output),
					projectWebContextPath
				);
			}
		}),
	]);
}

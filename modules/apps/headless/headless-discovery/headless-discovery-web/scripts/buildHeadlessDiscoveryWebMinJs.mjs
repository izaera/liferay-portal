/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import * as esbuild from 'esbuild';
import {polyfillNode} from 'esbuild-plugin-polyfill-node';
import fs from 'fs/promises';
import path from 'path';

main();

async function main() {
	const outDir = path.resolve(
		'build',
		'node',
		'packageRunBuild',
		'resources'
	);

	await Promise.all([
		fs.copyFile(
			path.resolve('src', 'index.html'),
			path.resolve(outDir, 'index.html')
		),
		fs.copyFile(
			path.resolve('src', 'css', 'main.css'),
			path.resolve(outDir, 'main.css')
		),
		esbuild.build({
			bundle: true,
			entryNames: 'headless-discovery-web-min',
			entryPoints: [path.resolve('src', 'index.js')],
			loader: {
				'.js': 'jsx',
			},
			outdir: outDir,
			plugins: [polyfillNode({})],
			sourcemap: true,
			target: ['es2020'],
		}),
	]);
}

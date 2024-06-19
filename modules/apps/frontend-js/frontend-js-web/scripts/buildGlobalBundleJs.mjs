/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import * as esbuild from 'esbuild';
import path from 'path';

main();

async function main() {
	const outDir = path.resolve(
		'build',
		'node',
		'packageRunBuild',
		'resources',
		'liferay'
	);

	await esbuild.build({
		bundle: true,
		entryNames: 'global.bundle',
		entryPoints: [
			path.resolve(
				'src',
				'main',
				'resources',
				'META-INF',
				'resources',
				'liferay',
				'global.es.js'
			),
		],
		loader: {
			'.js': 'jsx',
		},
		outdir: outDir,
		sourcemap: true,
		target: ['es2020'],
	});
}

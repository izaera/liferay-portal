/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import {ROOT_DIR, SRC_PATH, SRC_TSCONFIG_PATH} from '../../util/constants.mjs';

export default async function writeTsconfig(
	mainEntryPoints, projectDependencies, projectDescription) {

	const rootDir = path.posix.relative(SRC_PATH, path.join(ROOT_DIR));
	
	const tsBuildInfoFile = 
		path.posix.relative(
			SRC_PATH, 
			path.join(ROOT_DIR, '.tsc', 'buildinfo', `${projectDescription.name}.tsbuildinfo`)
		);

	const tscTypesDirProjectRelativePath = 
		path.posix.relative(SRC_PATH, path.join(ROOT_DIR, '.tsc', 'types'));

	const paths = {};
	const references = [];

	for (const dependency of Object.keys(projectDependencies)) {
		const mainEntryPoint = mainEntryPoints[dependency];

		if (!mainEntryPoint) {
			continue;
		}

		const mainEntryPointPath = path.join(
			ROOT_DIR,
			...`${mainEntryPoint.base}/${mainEntryPoint.main}`.split('/')
		);

		paths[dependency] = [path.posix.relative(SRC_PATH, mainEntryPointPath)];

		const projectPath = 
			path.posix.relative(
				SRC_PATH, 
				path.join(ROOT_DIR, mainEntryPoint.base)
			);

		references.push({path: `${projectPath}/${SRC_TSCONFIG_PATH}`});
	}

	const json = {
		compilerOptions: {
			allowSyntheticDefaultImports: true,
			baseUrl: '.',
			checkJs: false,
			composite: true,
			declarationDir: tscTypesDirProjectRelativePath,
			emitDeclarationOnly: true,
			jsx: 'react',
			module: 'es6',
			moduleResolution: 'node',
			paths, 			
			rootDir,
			sourceMap: false,
			strict: true,
			target: 'es6',
			tsBuildInfoFile,
			typeRoots: [
				'../../../../../../../../node_modules/@types'
			]
		},
		include: [
			'**/*',
			'../../../../../../../../global.d.ts'
		],
		references
	};

	await fs.writeFile(
		path.join(SRC_PATH, 'tsconfig.json'), 
		JSON.stringify(json, null, '\t'), 
		'utf-8'
	);
}

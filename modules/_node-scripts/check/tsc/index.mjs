/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import childProcess from 'child_process';
import path from 'path';

import generateTsconfig from '../../generate/tsconfig/index.mjs';

export default async function main() {
	await generateTsconfig();
	
	childProcess.spawnSync(
		'yarn',
		[
			'tsc',
			'-b',
			path.join('src', 'main', 'resources', 'META-INF', 'resources', 'tsconfig.json'),
			...process.argv.slice(3)
		], 
		{
			stdio: 'inherit',
		}
	);
}

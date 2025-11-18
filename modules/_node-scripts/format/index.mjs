/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import path from 'path';

import {
	NODE_SCRIPTS_DIR,
	PLAYWRIGHT_DIR,
	getRootDir,
} from '../util/constants.mjs';
import getNamedArguments from '../util/getNamedArguments.mjs';
import formatNodeScripts from './formatNodeScripts.mjs';
import formatPlaywright from './formatPlaywright.mjs';
import formatPortal from './formatPortal.mjs';
import formatProject from './formatProject.mjs';
import getFiles from './getFiles.mjs';

export default async function main() {
	const {check} = getNamedArguments({
		check: '--check',
	});

	const rootDir = await getRootDir();
	const portalDir = path.resolve(rootDir, '..');
	const currentDir = path.resolve('.');

	const files = await getFiles(portalDir, rootDir, currentDir);

	console.log(
		`\nℹ️  ${check ? 'Checking' : 'Formatting'} ${files ? files.length : 'ALL'} files...`
	);

	let checksPassed;

	if (currentDir === rootDir) {
		checksPassed = await formatPortal(
			check,
			portalDir,
			rootDir,
			currentDir,
			files
		);
	}
	else if (currentDir === NODE_SCRIPTS_DIR) {
		checksPassed = await formatNodeScripts(
			check,
			portalDir,
			rootDir,
			currentDir,
			files
		);
	}
	else if (currentDir === PLAYWRIGHT_DIR) {
		checksPassed = await formatPlaywright(
			check,
			portalDir,
			rootDir,
			currentDir,
			files
		);
	}
	else {
		checksPassed = await formatProject(
			check,
			portalDir,
			rootDir,
			currentDir,
			files
		);
	}

	if (checksPassed) {
		console.log('\n\n🎉 Everything is correct.\n\n');
	}
	else {
		console.error(
			'\n\n💥 Some errors could not be fixed automatically.\n\n'
		);
		process.exit(1);
	}
}

/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import childProcess from 'child_process';
import fs from 'fs/promises';

import {
	MODULES_DIR,
	YARN_LOCK_FILE,
	YARN_SCRIPT_FILE,
} from '../../locations.mjs';
import print from '../../print.mjs';

export default async function formatYarnLock(check) {
	let checksPassed = true;

	print(
		1,
		print.subTitle(
			`> ${check ? 'Checking' : 'Formatting'} 'yarn.lock' file...\n`
		)
	);

	if (!(await updateYarnLock(check))) {
		checksPassed = false;
	}

	if (!(await checkInvalidReferences())) {
		checksPassed = false;
	}

	return checksPassed;
}

async function updateYarnLock(check) {
	let checksPassed = true;

	const originalYarnLock = await fs.readFile(YARN_LOCK_FILE, 'utf-8');

	try {
		await new Promise((resolve, reject) => {
			const child = childProcess.fork(YARN_SCRIPT_FILE, {
				cwd: MODULES_DIR,
				stdio: 'ignore',
			});

			child.on('exit', (code) => {
				if (code === 0) {
					resolve();
				}
				else {
					reject(
						new Error(`Yarn execution failed: exit code ${code}`)
					);
				}
			});

			child.on('error', (error) => {
				reject(new Error(`Yarn execution failed: ${error}`));
			});
		});

		const newYarnLock = await fs.readFile(YARN_LOCK_FILE, 'utf-8');

		const modified = newYarnLock !== originalYarnLock;

		if (check) {
			await fs.writeFile(YARN_LOCK_FILE, originalYarnLock, 'utf-8');

			if (modified) {
				print(
					2,
					print.error('ERROR:'),
					`File 'yarn.lock' needs to be updated using 'yarn install'`,
					'\n'
				);

				checksPassed = false;
			}
		}
		else if (modified) {
			print(
				2,
				print.success('SUCCESS:'),
				`Updated 'yarn.lock' file`,
				'\n'
			);
		}
	}
	catch (error) {
		await fs.writeFile(YARN_LOCK_FILE, originalYarnLock, 'utf-8');

		print(
			2,
			print.error('ERROR:'),
			`Unhandled error formatting 'yarn.lock' file`
		);
		print(3, error, '\n');

		checksPassed = false;
	}

	return checksPassed;
}

async function checkInvalidReferences() {
	let checksPassed = true;

	const errorLines = {};

	const yarnLock = await fs.readFile(YARN_LOCK_FILE, 'utf-8');

	const lines = yarnLock.split('\n');

	for (let i = 0; i < lines.length; i++) {
		const line = lines[i].trimStart();

		if (!line.startsWith('resolved ')) {
			continue;
		}

		if (!line.startsWith('resolved "https://registry.yarnpkg.com/')) {
			errorLines[i + 1] = line.substring(9);

			checksPassed = false;
		}
	}

	if (!checksPassed) {
		print(
			2,
			print.error('ERROR:'),
			'Global',
			print.underline(`'yarn.lock'`),
			'file contains invalid references to packages'
		);

		for (const [i, line] of Object.entries(errorLines)) {
			print(3, `(${i}:0) ${line}`);
		}

		print(2, '');
	}

	return checksPassed;
}

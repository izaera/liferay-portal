/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {readFileSync} from 'fs';
import path from 'path';

import {getRootDir} from '../../util/constants.mjs';
import indent from '../../util/indent.mjs';

const LOCAL_REGISTRY_REGEX = /^\s+resolved\s".*(localhost|127.0.0.1).*$/gm;

export default async function checkYarnLock() {
	let checksPassed = true;

	console.log(`\n\n🔍️️️ Checking 'yarn.lock' file...\n`);

	const yarnLock = path.join(await getRootDir(), 'yarn.lock');

	if (!yarnLock) {
		return [];
	}

	const yarnLockContent = readFileSync(yarnLock, 'utf8');

	if (LOCAL_REGISTRY_REGEX.test(yarnLockContent)) {
		console.log(
			indent(
				4,
				`❌ The 'yarn.lock' file contains references to a local npm registry (remove them)`
			)
		);

		checksPassed = false;
	}

	if (checksPassed) {
		console.log(indent(4, `✅ Checked 'yarn.lock'`));
	}

	return checksPassed;
}

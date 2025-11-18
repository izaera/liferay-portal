/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getNamedArguments from '../util/getNamedArguments.mjs';
import checkTypeScript from './tsc/checkTypeScript.mjs';
import formatSourceFiles from './util/formatSourceFiles.mjs';

export default async function formatPlaywright(
	check,
	portalDir,
	rootDir,
	currentDir,
	files
) {
	const {emitSuppressed, ignoreTypescript} = getNamedArguments({
		emitSuppressed: '--emit-suppressed',
		ignoreTypescript: '--ignore-typescript',
	});

	let checksPassed = true;

	if (!(await formatSourceFiles(check, emitSuppressed, files))) {
		checksPassed = false;
	}

	if (!ignoreTypescript && !checkTypeScript([currentDir])) {
		checksPassed = false;
	}

	return checksPassed;
}

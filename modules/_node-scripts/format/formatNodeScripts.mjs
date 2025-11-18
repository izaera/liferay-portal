/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getNamedArguments from '../util/getNamedArguments.mjs';
import formatNodeScriptsHash from './util/formatNodeScriptsHash.mjs';
import formatSourceFiles from './util/formatSourceFiles.mjs';

/**
 * Executes the standard format tasks but then checks if node-scripts version number must be bumped
 * according to https://liferay.atlassian.net/browse/LPD-25771
 */
export default async function formatNodeScripts(
	check,
	portalDir,
	rootDir,
	currentDir,
	files
) {
	const {emitSuppressed} = getNamedArguments({
		emitSuppressed: '--emit-suppressed',
	});

	let checksPassed = true;

	if (!(await formatSourceFiles(check, emitSuppressed, files))) {
		checksPassed = false;
	}

	if (!(await formatNodeScriptsHash(check))) {
		checksPassed = false;
	}

	return checksPassed;
}

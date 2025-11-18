/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import indent from '../../util/indent.mjs';
import formatFiles from '../prettier/formatFiles.mjs';

export default async function formatSourceFiles(check, emitSuppressed, files) {
	let checksPassed = true;

	console.log(
		`\n\n🔍️️️ ${check ? 'Checking' : 'Formatting'} files with 'prettier' and 'eslint'...\n`
	);

	const formatOutput = await formatFiles(!check, files, {emitSuppressed});

	if (formatOutput) {
		console.log(indent(4, formatOutput));
	}
	else {
		console.log(indent(4, `✅ All files are correctly formatted`));
	}

	if (formatOutput && check) {
		checksPassed = false;
	}
	else if (formatOutput) {
		if (formatOutput.includes('✖')) {
			checksPassed = false;
		}
	}

	return checksPassed;
}

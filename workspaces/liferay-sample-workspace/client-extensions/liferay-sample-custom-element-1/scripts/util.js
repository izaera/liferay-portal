/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/* eslint-disable no-console */

const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

function emit(buildDir, baseName, content) {
	const hash = crypto.createHash('sha256').update(content).digest('hex');

	const outputRelPath = insertHash(baseName, hash);

	console.log(`    -> ${outputRelPath}`);

	const outputPath = path.join(buildDir, outputRelPath);

	fs.mkdirSync(path.dirname(outputPath), {recursive: true});
	fs.writeFileSync(outputPath, content, 'utf-8');

	return hash;
}

function insertHash(filePath, hash) {
	const extname = path.extname(filePath);

	return (
		filePath.substring(0, filePath.length - extname.length) +
		'.' +
		hash +
		extname
	);
}

module.exports = {
	emit,
	insertHash,
};

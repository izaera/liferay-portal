/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const fs = require('fs');
const path = require('path');

const {emit} = require('./util');

function buildCss(baseDir, buildDir, entryPoint) {
	emit(
		buildDir,
		entryPoint,
		fs.readFileSync(path.join(baseDir, entryPoint), 'utf-8')
	);
}

module.exports = buildCss;

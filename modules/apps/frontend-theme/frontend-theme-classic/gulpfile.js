/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/* eslint-disable @liferay/no-extraneous-dependencies */

'use strict';

const crypto = require('crypto');
const fs = require('fs');
const gulp = require('gulp');
const liferayThemeTasks = require('liferay-theme-tasks');

liferayThemeTasks.registerTasks({
	gulp,
	hookFn(gulp) {
		gulp.hook('before:build:war', function (done) {
			hashify('./build/css', 'clay.css');
			hashify('./build/css', 'clay_rtl.css');
			hashify('./build/css', 'main.css');
			hashify('./build/css', 'main_rtl.css');
			hashify('./build/js', 'main.js');

			done();
		});
	},
});

function hashify(dir, fileName) {
	const content = fs.readFileSync(`${dir}/${fileName}`, 'utf-8');

	const hash = calculateFileHash(content);

	const i = fileName.lastIndexOf('.');

	const hashedFileName =
		fileName.substring(0, i) + `.(${hash})` + fileName.substring(i);

	fs.copyFileSync(`${dir}/${fileName}`, `${dir}/${hashedFileName}`);
}

function calculateFileHash(content) {

	// Calculate hash (MD5 is enough because we don't need to be crypto-safe)

	let blob = crypto.createHash('md5').update(content).digest();

	// Truncate hash to make URL shorter

	blob = blob.slice(0, 8);

	// Convert bytes to base64 and replace non alphabetic base64 chars
	// (+, /) by URL friendly chars

	let hash = blob.toString('base64');

	hash = hash.replaceAll('+', '$');
	hash = hash.replaceAll('/', '@');

	// Remove the trailing = signs. Since they are base64 padding markers they
	// can be discarded for the purposes of creating a collision resistant hash

	hash = hash.replaceAll('=', '');

	return hash;
}

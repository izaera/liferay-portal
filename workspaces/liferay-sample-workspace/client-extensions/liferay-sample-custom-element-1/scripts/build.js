/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/* eslint-disable no-console */

const fs = require('fs');
const path = require('path');

const builders = {
	'.css': require('./build-css'),
	'.js': require('./build-js'),
};

const rmdirSync = fs.rmSync || fs.rmdirSync;

const projectDir = path.resolve(__dirname, '..');

const buildJsonPath = path.join(projectDir, 'build.json');
const buildJson = JSON.parse(fs.readFileSync(buildJsonPath, 'utf-8'));

const baseDir = path.resolve(projectDir, buildJson.baseDir);
const buildDir = path.resolve(projectDir, buildJson.buildDir);
const {entryPoints} = buildJson;

console.log('');
console.log(
	`> Building from '${buildJson.baseDir}' to '${buildJson.buildDir}'...`
);

if (fs.existsSync(buildDir)) {
	rmdirSync(buildDir, {recursive: true});
}

fs.mkdirSync(buildDir, {recursive: true});

entryPoints.forEach((entryPoint) => {
	if (!fs.existsSync(path.join(baseDir, entryPoint))) {
		console.error(`Entry point '${entryPoint}' cannot be found`);
		process.exit(2);
	}

	const extname = path.extname(entryPoint);
	const builder = builders[extname];

	if (!builder) {
		console.error(`No builder for '${extname}' files available`);
		process.exit(3);
	}

	console.log(`> Building '${entryPoint}':`);

	builder(baseDir, buildDir, entryPoint);
});

console.log('');

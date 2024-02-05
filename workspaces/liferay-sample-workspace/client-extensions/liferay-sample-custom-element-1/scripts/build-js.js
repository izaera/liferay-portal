/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/* eslint-disable no-console */

const acorn = require('acorn');
const acornWalk = require('acorn-walk');
const escodegen = require('escodegen');
const fs = require('fs');
const path = require('path');

const {emit, insertHash} = require('./util');

function buildJs(baseDir, buildDir, entryPoint) {
	const importSort = getImportSort(path.join(baseDir, entryPoint));

	const fileHashes = {};

	importSort.forEach((filePath) => {
		const content = buildFile(filePath, fileHashes, baseDir, buildDir);

		fileHashes[filePath] = emit(
			buildDir,
			path.relative(baseDir, filePath),
			content
		);
	});
}

function buildFile(filePath, fileHashes) {
	const fileDir = path.dirname(filePath);

	const ast = parse(filePath);

	acornWalk.simple(ast, {
		ImportDeclaration(node) {
			const imported = node.source.value;

			if (!imported.startsWith('.')) {
				return;
			}

			const importedPath = path.resolve(fileDir, imported);

			const importedHash = fileHashes[importedPath];

			if (!importedHash) {
				console.error(
					`Module '${imported}' imported in '${filePath}' has no computed hash yet`
				);
				process.exit(1);
			}

			node.source.value = insertHash(imported, importedHash);
		},
	});

	return escodegen.generate(ast);
}

function getImportSort(filePath) {
	const visited = {};

	const importedFiles = recursiveGetImportedFiles(filePath).filter((file) => {
		if (visited[file]) {
			return;
		}

		visited[file] = true;

		return true;
	});

	importedFiles.push(filePath);

	return importedFiles;
}

function parse(filePath) {
	return acorn.parse(fs.readFileSync(filePath, 'utf-8'), {
		ecmaVersion: 2022,
		sourceType: 'module',
	});
}

function recursiveGetImportedFiles(filePath) {
	const fileDir = path.dirname(filePath);
	const importedFiles = [];

	acornWalk.simple(parse(filePath), {
		ImportDeclaration(node) {
			const imported = node.source.value;

			if (!imported.startsWith('.')) {
				return;
			}

			const importedPath = path.resolve(fileDir, imported);

			if (!fs.existsSync(importedPath)) {
				console.error(
					`Module '${imported}' imported in '${filePath}' cannot be found`
				);
				process.exit(1);
			}

			importedFiles.push(...recursiveGetImportedFiles(importedPath));

			importedFiles.push(importedPath);
		},
	});

	return importedFiles;
}

module.exports = buildJs;

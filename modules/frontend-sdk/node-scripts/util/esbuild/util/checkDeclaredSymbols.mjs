/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import print from '../../print.mjs';
import getExportedSymbols from './getExportedSymbols.mjs';
import getSymbolsFromEsbuild from './getSymbolsFromEsbuild.mjs';

/**
 * Report symbols an export bridge should have declared and did not.
 *
 * getExportedSymbols() reads a module by requiring it, and when that fails
 * because the module is an ES module, by parsing it with acorn. The parse is
 * the fallible half: it has to recognise every shape an export can take, and a
 * shape it does not recognise is not an error, it is a symbol that quietly
 * never reaches the bridge. react-dropzone lost `useDropzone` that way, because
 * its ES build declares the name on the export instead of listing it as a
 * specifier and nothing noticed until the bridges were compared by hand.
 *
 * Only under-declaration is looked for. Declaring a symbol the target does not
 * have is already fatal for an ES module, since esbuild link checks the bridge
 * while bundling it, so that case never reaches this far.
 *
 * Only ES module targets are looked at. A CommonJS target's symbols come from
 * requiring it and reading its keys, which is the authoritative answer, and
 * esbuild cannot statically enumerate a CommonJS module anyway.
 *
 * This runs behind CHECK_EXPORTED_SYMBOLS because answering the question costs
 * two more bundles of the module's dependency graph per package, which is far
 * too much to spend on every build. Run it when the inference changes, or when
 * a package is upgraded and its export shape may have moved.
 */
export default async function checkDeclaredSymbols(
	overridenPackageSymbols,
	moduleName,
	metafile
) {
	if (!process.env.CHECK_EXPORTED_SYMBOLS) {
		return;
	}

	if (!isESModuleTarget(metafile, moduleName)) {
		return;
	}

	const declaredSymbols = await getExportedSymbols(
		overridenPackageSymbols,
		moduleName
	);

	let actualSymbols;

	try {
		actualSymbols = await getSymbolsFromEsbuild(moduleName);
	}
	catch (error) {

		// Some modules only bundle inside the real build, with the linker
		// plugin and a stylesheet loader in place. Not being able to ask is not
		// the same as an answer, so say so and move on.

		print(
			0,
			print.warning('WARNING:'),
			`Unable to check the declared symbols of ${moduleName}: ${error.message.split('\n')[0]}`
		);

		return;
	}

	const missingSymbols = Object.keys(actualSymbols).filter(
		(symbol) => symbol !== '__esModule' && !declaredSymbols[symbol]
	);

	if (!missingSymbols.length) {
		return;
	}

	throw new Error(
		`The export bridge for ${moduleName} is missing ${missingSymbols.length} ` +
			`symbol(s) that the module exports: ${missingSymbols.join(', ')}. ` +
			`Nothing importing ${moduleName} can reach them. Either teach ` +
			`parseESMExports() the shape it did not recognise, or declare the ` +
			`symbols through a symbols override in node-scripts.config.js.`
	);
}

function isESModuleTarget(metafile, moduleName) {
	for (const input of Object.values(metafile.inputs)) {
		for (const {original, path} of input.imports || []) {
			if (original === moduleName) {
				return metafile.inputs[path]?.format === 'esm';
			}
		}
	}

	return false;
}

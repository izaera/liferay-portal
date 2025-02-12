import fs from 'fs';
import path from 'path';

import unquote from './unquote.mjs';

export default function compareBundleImports(leftDir, rightDir) {
	const leftBundleImports = getBundleImports(leftDir);
	const rightBundleImports = getBundleImports(rightDir);

	const bundles = [
		...new Set([
			...Object.keys(leftBundleImports),
			...Object.keys(rightBundleImports),
		])
	].sort();

	let totalChanges = 0;

	for (const bundle of bundles) {
		const leftImports = leftBundleImports[bundle] ?? [];
		const rightImports = rightBundleImports[bundle] ?? [];

		const added = rightImports.filter(url => !leftImports.includes(url));
		const removed = leftImports.filter(url => !rightImports.includes(url));

		if (!added.length && !removed.length) {
			continue;
		}

		console.log(`📦️ ${bundle}:`);

		for (const url of added) {
			console.log(`+ 🔴 ${url}`);
			totalChanges++;
		}

		for (const url of removed) {
			console.log(`- 🟢 ${url}`);
			totalChanges--;
		}

		console.log('');
	}

	if (totalChanges < 0) {
		console.log(`- 🟢 [${Math.abs(totalChanges)} removals] ### TOTAL ###`);
	}
	else if (totalChanges > 0) {
		console.log(`+ 🔴 [${totalChanges} additions] ### TOTAL ###`);
	}
	else {
		console.log(`= ⚪️ [0 changes] ### TOTAL ###`);
	}
}

function getBundleImports(dir) {
	const imports = {};

	const lines = 
		fs.readFileSync(path.join(dir, 'bundle-imports.csv'), 'utf-8')
			.split('\n')
			.slice(1)
			.sort();

	for (const line of lines) {
		let [bundle, url] = line.split(';');

		bundle = unquote(bundle);
		url = unquote(url);

		if (!imports[bundle]) {
			imports[bundle] = [];
		}

		imports[bundle].push(url);
	}

	return imports;
}

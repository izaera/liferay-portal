import fs from 'fs';
import path from 'path';

import unquote from './unquote.mjs';

export default function compareJavaImports(leftDir, rightDir) {
	const leftJavaImports = getJavaImports(leftDir);
	const rightJavaImports = getJavaImports(rightDir);

	const projects = [
		...new Set([
			...Object.keys(leftJavaImports),
			...Object.keys(rightJavaImports),
		])
	].sort();

	let totalChanges = 0;

	for (const project of projects) {
		const leftImports = leftJavaImports[project] ?? [];
		const rightImports = rightJavaImports[project] ?? [];

		const added = rightImports.filter(url => !leftImports.includes(url));
		const removed = leftImports.filter(url => !rightImports.includes(url));

		if (!added.length && !removed.length) {
			continue;
		}

		console.log(`📂 ${project}:`);

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

function getJavaImports(dir) {
	const imports = {};

	const lines = 
		fs.readFileSync(path.join(dir, 'java-imports.csv'), 'utf-8')
			.split('\n')
			.slice(1)
			.sort();

	for (const line of lines) {
		let [project, url] = line.split(';');

		project = unquote(project);
		url = unquote(url);

		if (!imports[project]) {
			imports[project] = [];
		}

		imports[project].push(url);
	}

	return imports;
}

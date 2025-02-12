import fs from 'fs';
import path from 'path';

import unquote from './unquote.mjs';

export default function compareBundleSizes(leftDir, rightDir, detailed) {
	const leftBundleSizes = getBundleSizes(leftDir);
	const rightBundleSizes = getBundleSizes(rightDir);

	const added = 
		Object.keys(rightBundleSizes)
			.filter(bundle => !leftBundleSizes[bundle])
			.sort((left, right) => rightBundleSizes[right] - rightBundleSizes[left]);

	const removed = 
		Object.keys(leftBundleSizes)
			.filter(bundle => !rightBundleSizes[bundle])
			.sort((left, right) => leftBundleSizes[right] - leftBundleSizes[left]);

	const increased = 
		Object.keys(leftBundleSizes)
			.filter(bundle => rightBundleSizes[bundle] && rightBundleSizes[bundle] > leftBundleSizes[bundle])
			.sort((left, right) => 
					(rightBundleSizes[right] - leftBundleSizes[right]) 
					- 
					(rightBundleSizes[left] - leftBundleSizes[left]));

	const decreased = 
		Object.keys(leftBundleSizes)
			.filter(bundle => rightBundleSizes[bundle] && rightBundleSizes[bundle] < leftBundleSizes[bundle])
			.sort((left, right) => 
					(leftBundleSizes[right] - rightBundleSizes[right]) 
					- 
					(leftBundleSizes[left] - rightBundleSizes[left]));

	const untouched = 
		Object.keys(leftBundleSizes)
			.filter(bundle => rightBundleSizes[bundle] && rightBundleSizes[bundle] == leftBundleSizes[bundle]);

	for (const bundle of added) {
		console.log(`+ 🔴 [${formatSize(rightBundleSizes[bundle])}] ${bundle}`);
	}
	
	console.log('');

	for (const bundle of increased) {
		console.log(`> 🔴 [${formatSize(leftBundleSizes[bundle])} + ${formatSize(rightBundleSizes[bundle] - leftBundleSizes[bundle])}] ${bundle}`);
	}

	console.log('');

	for (const bundle of removed) {
		console.log(`- 🟢 [${formatSize(leftBundleSizes[bundle])}] ${bundle}`);
	}

	console.log('');

	for (const bundle of decreased) {
		console.log(`< 🟢 [${formatSize(leftBundleSizes[bundle])} - ${formatSize(leftBundleSizes[bundle] - rightBundleSizes[bundle])}] ${bundle}`);
	}

	if (detailed) {
		console.log('');

		for (const bundle of untouched) {
			console.log(`= ⚪️ [${formatSize(leftBundleSizes[bundle])} + ${formatSize(rightBundleSizes[bundle] - leftBundleSizes[bundle])}] ${bundle}`);
		}
	}

	const leftTotal = Object.values(leftBundleSizes).reduce((total, size) => total + size, 0);
	const rightTotal = Object.values(rightBundleSizes).reduce((total, size) => total + size, 0);

	const totalChange = rightTotal - leftTotal;

	console.log('');

	if (totalChange < 0) {
		console.log(`< 🟢 [${formatSize(leftTotal)} - ${formatSize(Math.abs(totalChange))}] ### TOTAL ###`);
	}
	else if (totalChange > 0) {
		console.log(`> 🔴 [${formatSize(leftTotal)} + ${formatSize(totalChange)}] ### TOTAL ###`);
	}
	else {
		console.log(`= ⚪️ [${formatSize(leftTotal)}] ### TOTAL ###`);
	}
}

function formatSize(size) {
	if (size < 1_000) {
		return `${size}`;
	}
	else if (size < 1_000_000) {
		return `${(size/1_000).toFixed(2)}K`;
	}
	else {
		return `${(size/1_000_000).toFixed(2)}M`;
	}
}

function getBundleSizes(dir) {
	const sizes = {};

	const lines = 
		fs.readFileSync(path.join(dir, 'bundle-sizes.csv'), 'utf-8')
			.split('\n')
			.slice(1)
			.sort();

	for (const line of lines) {
		let [bundle, _gzip, size] = line.split(';');

		bundle = unquote(bundle);
		size = unquote(size);

		sizes[bundle] = Number(size);
	}

	return sizes;
}

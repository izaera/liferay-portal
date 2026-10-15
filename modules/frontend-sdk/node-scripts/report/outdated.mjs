/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {$} from 'execa';
import fg from 'fast-glob';
import fs from 'fs/promises';
import path from 'path';

import getNamedArguments from '../util/getNamedArguments.mjs';
import print from '../util/print.mjs';
import {
	addHealth,
	classifyGraph,
	createGraph,
} from '../util/report/outdated/buildOutdatedGraph.mjs';
import fetchAdvisories from '../util/report/outdated/fetchAdvisories.mjs';
import fetchPackuments from '../util/report/outdated/fetchPackuments.mjs';
import parseYarnLock from '../util/report/outdated/parseYarnLock.mjs';
import readCodeowners from '../util/report/outdated/readCodeowners.mjs';
import renderOutdatedReport from '../util/report/outdated/renderOutdatedReport.mjs';

const DEFAULT_REGISTRY = 'https://registry.yarnpkg.com';

export default async function main() {
	const {
		output,
		refresh,
		registry: registryArgument,
	} = getNamedArguments({
		output: '--output',
		refresh: '--refresh',
		registry: '--registry',
	});

	const rootDir = process.cwd();

	let lockContent;

	try {
		lockContent = await fs.readFile(
			path.join(rootDir, 'yarn.lock'),
			'utf8'
		);
	}
	catch (error) {
		if (error.code !== 'ENOENT') {
			throw error;
		}

		print(
			0,
			print.error('\nERROR:'),
			`No yarn.lock file was found in ${rootDir}. Run this command from`,
			`\n       the directory that holds the yarn.lock file (for example,`,
			`\n       'modules' or a workspace).\n`
		);

		process.exit(2);
	}

	const registry =
		typeof registryArgument === 'string'
			? registryArgument
			: await getYarnRegistry();

	const manifests = await readManifests(rootDir);
	const rootManifest = manifests[0];

	const getOwners = await readCodeowners(rootDir);

	if (getOwners) {
		for (const manifest of manifests) {
			manifest.owners = getOwners(
				path.join(rootDir, path.dirname(manifest.path))
			);
		}
	}

	const graph = createGraph({
		lock: parseYarnLock(lockContent),
		manifests,
		resolutions: rootManifest.resolutions,
		workspaceNames: new Set(
			manifests.slice(1).map((manifest) => manifest.name)
		),
	});

	const names = new Set();

	for (const node of graph.nodes.values()) {
		if (node.registry) {
			names.add(node.name);
		}
	}

	print(
		0,
		print.info('\nINFO:'),
		`Checking ${graph.nodes.size} locked packages (${names.size} distinct`,
		`names) against ${registry}...`
	);

	const cacheDir = path.join(
		rootDir,
		'build',
		'node-scripts',
		'report-outdated',
		'registry'
	);

	const packuments = await fetchPackuments(names, {
		cacheDir,
		refresh: !!refresh,
		registry,
	});

	classifyGraph(graph, packuments);

	const versionsByName = new Map();

	for (const node of graph.nodes.values()) {
		if (node.registry) {
			if (!versionsByName.has(node.name)) {
				versionsByName.set(node.name, new Set());
			}

			versionsByName.get(node.name).add(node.version);
		}
	}

	const advisories = await fetchAdvisories(versionsByName, {
		cacheDir,
		refresh: !!refresh,
		registry,
	});

	const rootNames = new Set();

	for (const {node} of graph.roots.values()) {
		if (node.registry) {
			rootNames.add(node.name);
		}
	}

	const releaseDates = await fetchPackuments(rootNames, {
		cacheDir,
		full: true,
		refresh: !!refresh,
		registry,
	});

	addHealth(graph, {advisories, releaseDates});

	const reportFile =
		typeof output === 'string' ? output : 'outdated-packages.html';

	await fs.writeFile(
		reportFile,
		renderOutdatedReport(graph, {
			dir: path.basename(rootDir),
			hasAdvisories: !!advisories,
			hasOwners: !!getOwners,
			refreshCommand: getRefreshCommand(rootDir),
			registry,
		})
	);

	const outdatedCount = [...graph.nodes.values()].filter(
		(node) => node.outdated
	).length;

	print(
		0,
		print.info('INFO:'),
		`Found ${outdatedCount} outdated packages. Wrote report file:`,
		`${reportFile}\n`
	);
}

/**
 * Returns the command that refreshes entries of the yarn.lock of the given
 * directory, run from that directory.
 */
function getRefreshCommand(rootDir) {
	const name = path.basename(rootDir);

	if (name === 'modules') {
		return 'cd modules && npx node-scripts refresh:yarn-lock';
	}

	if (path.basename(path.dirname(rootDir)) === 'workspaces') {
		return `cd workspaces/${name} && node ../../modules/frontend-sdk/node-scripts/bin.js refresh:yarn-lock`;
	}

	return `cd ${name} && node-scripts refresh:yarn-lock`;
}

async function getYarnRegistry() {
	try {
		const {stdout} = await $`yarn config get registry`;

		const registry = stdout.trim();

		if (/^https?:\/\//.test(registry)) {
			return registry;
		}
	}
	catch {

		// Fall back to the default registry

	}

	return DEFAULT_REGISTRY;
}

/**
 * Reads the root `package.json` and the `package.json` of every workspace
 * package. The root manifest always comes first.
 */
async function readManifests(rootDir) {
	const rootManifest = await readManifest(rootDir, 'package.json');

	let patterns = rootManifest.workspaces || [];

	if (!Array.isArray(patterns)) {
		patterns = patterns.packages || [];
	}

	const files = await fg(
		patterns.map((pattern) => `${pattern}/package.json`),
		{cwd: rootDir, ignore: ['**/node_modules/**']}
	);

	const manifests = [rootManifest];

	for (const file of files.sort()) {
		manifests.push(await readManifest(rootDir, file));
	}

	return manifests;
}

async function readManifest(rootDir, file) {
	const manifest = JSON.parse(
		await fs.readFile(path.join(rootDir, file), 'utf8')
	);

	return {...manifest, path: file};
}

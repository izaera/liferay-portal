/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import semver from 'semver';

import {splitPattern} from './parseYarnLock.mjs';

const MANIFEST_DEPENDENCY_TYPES = [
	'dependencies',
	'devDependencies',
	'optionalDependencies',
];

/**
 * Builds the graph of locked packages and works out which ones are outdated
 * and what has to change for each of them to be updated.
 *
 * @param {object} options
 * @param {Map<string, object>} options.lock the parsed `yarn.lock`
 * @param {Array<object>} options.manifests the `package.json` files that
 *     declare dependencies (`{dependencies, ..., path}`)
 * @param {object} options.resolutions the `resolutions` of the root
 *     `package.json`
 * @param {Set<string>} options.workspaceNames names of the workspace packages,
 *     which are linked rather than locked
 * @return {object} `{nodes, roots}` (see `createGraph`) plus, once
 *     `classifyGraph` runs, the outdated information
 */
export function createGraph({lock, manifests, resolutions, workspaceNames}) {
	const nodes = new Map();

	function getNode(pattern) {
		const entry = lock.get(pattern);

		if (!entry) {
			return null;
		}

		const {name, range} = resolveAlias(entry.name, entry.range);
		const id = `${name}@${entry.version}`;

		let node = nodes.get(id);

		if (!node) {
			node = {
				children: [],
				id,
				lockHeaders: new Set(),
				name,
				pending: entry.dependencies,
				range,
				registry: isRegistryEntry(entry),
				version: entry.version,
			};

			nodes.set(id, node);
		}

		node.lockHeaders.add(entry.header);

		return node;
	}

	const roots = new Map();

	for (const manifest of manifests) {
		for (const type of MANIFEST_DEPENDENCY_TYPES) {
			for (const [dependencyName, range] of Object.entries(
				manifest[type] || {}
			)) {
				if (
					workspaceNames.has(dependencyName) &&
					!range.startsWith('npm:')
				) {
					continue;
				}

				const node = getNode(`${dependencyName}@${range}`);

				if (!node) {
					continue;
				}

				let root = roots.get(node.id);

				if (!root) {
					root = {declarations: [], node};

					roots.set(node.id, root);
				}

				root.declarations.push({
					dependencyName,
					manifest: manifest.path,
					owners: manifest.owners,
					range,
					type,
				});
			}
		}
	}

	// Link every node to its children, discovering nodes as they are reached

	const queue = [...nodes.values()];

	while (queue.length) {
		const node = queue.shift();

		for (const [dependencyName, range] of Object.entries(node.pending)) {
			const known = nodes.size;
			const child = getNode(`${dependencyName}@${range}`);

			if (!child) {
				continue;
			}

			node.children.push({child, dependencyName, range});

			if (nodes.size > known) {
				queue.push(child);
			}
		}

		delete node.pending;
	}

	const pinned = new Map();

	for (const [pattern, version] of Object.entries(resolutions || {})) {
		pinned.set(getResolutionPackageName(pattern), {pattern, version});
	}

	for (const node of nodes.values()) {
		node.resolution = pinned.get(node.name);
	}

	return {nodes, roots};
}

/**
 * Flags outdated nodes and classifies the action that updates each of them,
 * for every edge (from a manifest or from another package) that leads to one.
 */
export function classifyGraph({nodes, roots}, packuments) {
	const resolver = createResolver(packuments);

	for (const node of nodes.values()) {
		const packument = node.registry ? packuments.get(node.name) : null;

		node.latest = packument?.distTags?.latest;

		node.outdated =
			!!node.latest &&
			!!semver.valid(node.version) &&
			semver.gt(node.latest, node.version);

		if (node.outdated) {
			node.diff = getDiff(node.version, node.latest);
		}

		node.deprecated = packument?.versions?.[node.version]?.deprecated;
	}

	for (const root of roots.values()) {
		if (root.node.outdated) {
			root.action = classifyDirectEdge(root, resolver);
		}
	}

	for (const node of nodes.values()) {
		for (const edge of node.children) {
			if (edge.child.outdated) {
				edge.action = classifyEdge(node, edge, packuments, resolver);
			}
		}
	}

	markDuplicates({nodes, roots});

	markOutdatedReachability(nodes);

	for (const root of roots.values()) {
		root.outdatedNodes = collectOutdated(root.node);
	}
}

/**
 * Adds the health signals of the report: the security advisories of every
 * node and, for the top-level dependencies, how old their locked and latest
 * releases are and how many majors they are behind.
 *
 * @param {object} graph the classified graph
 * @param {object} options
 * @param {?Map<string, Array<object>>} options.advisories the advisories of
 *     each `name@version`, or `null` when they could not be fetched
 * @param {Map<string, object>} options.releaseDates the full packuments (dist
 *     tags and publish times) of the top-level dependencies
 */
export function addHealth({nodes, roots}, {advisories, releaseDates}) {
	if (advisories) {
		for (const node of nodes.values()) {
			node.advisories = advisories.get(node.id);
		}
	}

	for (const root of roots.values()) {
		const {node} = root;
		const time = releaseDates.get(node.name)?.time || {};

		node.releasedAt = time[node.version];

		if (node.latest) {
			node.latestReleasedAt = time[node.latest];
		}

		if (node.outdated && node.diff === 'major') {
			node.majorsBehind = semver.major(node.latest)
				? semver.major(node.latest) - semver.major(node.version)
				: semver.minor(node.latest) - semver.minor(node.version);
		}
	}
}

function classifyDirectEdge(root, resolver) {
	const {node} = root;

	for (const {range} of root.declarations) {
		const wanted = resolver.getWanted(
			node.name,
			resolveAlias(node.name, range).range
		);

		if (wanted && semver.gt(wanted, node.version)) {
			return {target: wanted, type: 'refresh'};
		}
	}

	return {target: node.latest, type: 'raise-range'};
}

function classifyEdge(
	parent,
	{child, dependencyName, range},
	packuments,
	resolver
) {
	if (child.resolution) {
		return {pattern: child.resolution.pattern, type: 'resolution'};
	}

	const childRange = resolveAlias(dependencyName, range).range;

	if (!semver.validRange(childRange)) {
		return {type: 'unknown'};
	}

	const wanted = resolver.getWanted(child.name, childRange);

	if (wanted && semver.gt(wanted, child.version)) {
		return {target: wanted, type: 'refresh'};
	}

	const parentPackument = parent.registry
		? packuments.get(parent.name)
		: null;

	if (!parentPackument || !semver.valid(parent.version)) {
		return {type: 'unknown'};
	}

	const newerParentVersions = Object.keys(parentPackument.versions)
		.filter(
			(version) =>
				semver.valid(version) &&
				!semver.prerelease(version) &&
				semver.gt(version, parent.version)
		)
		.sort(semver.compare);

	// Find the best release of the child that some newer release of the parent
	// brings in, and the first release of the parent that brings it in

	let best = null;

	for (const version of newerParentVersions) {
		const parentRange =
			parentPackument.versions[version].dependencies[dependencyName];

		if (parentRange === undefined) {
			return {
				dropped: true,
				parentTarget: version,
				type: 'upgrade-parent',
			};
		}

		const candidate = resolver.getWanted(
			child.name,
			resolveAlias(dependencyName, parentRange).range
		);

		if (
			candidate &&
			semver.gt(candidate, child.version) &&
			(!best || semver.gt(candidate, best.target))
		) {
			best = {
				parentTarget: version,
				target: candidate,
				type: 'upgrade-parent',
			};

			if (candidate === child.latest) {
				break;
			}
		}
	}

	return (
		best || {parentLatest: parentPackument.distTags.latest, type: 'blocked'}
	);
}

function collectOutdated(rootNode) {
	const outdated = new Set();
	const visited = new Set([rootNode]);
	const queue = [rootNode];

	while (queue.length) {
		const node = queue.shift();

		if (node.outdated) {
			outdated.add(node);
		}

		for (const {child} of node.children) {
			if (child.reachesOutdated && !visited.has(child)) {
				visited.add(child);
				queue.push(child);
			}
		}
	}

	return outdated;
}

/**
 * Returns a helper that finds the highest release of a package that satisfies
 * a range, memoizing the answers because the same ranges come up many times.
 */
function createResolver(packuments) {
	const cache = new Map();

	return {
		getWanted(name, range) {
			const key = `${name}@${range}`;

			if (!cache.has(key)) {
				const versions = Object.keys(
					packuments.get(name)?.versions || {}
				);

				cache.set(
					key,
					semver.validRange(range)
						? semver.maxSatisfying(versions, range)
						: null
				);
			}

			return cache.get(key);
		},
	};
}

function getDiff(version, latest) {
	const diff = semver.diff(version, latest);

	return diff ? diff.replace(/^pre/, '').replace(/^release$/, 'patch') : null;
}

/**
 * Returns the package name a `resolutions` pattern applies to, which is its
 * last segment (for example, `uuid` for a pattern ending in `/sockjs/uuid`).
 */
function getResolutionPackageName(pattern) {
	const segments = pattern.split('/');
	const last = segments.pop();

	if (segments.length && segments[segments.length - 1].startsWith('@')) {
		return `${segments.pop()}/${last}`;
	}

	return last;
}

function isRegistryEntry(entry) {
	if (!entry.resolved) {
		return true;
	}

	return /^https?:\/\/registry\.(npmjs\.org|yarnpkg\.com)\//.test(
		entry.resolved
	);
}

/**
 * Marks the outdated nodes that a newer locked version of the same package
 * could replace, because every range that requests them also accepts it.
 * Refreshing `yarn.lock` merges such duplicates into that version.
 */
function markDuplicates({nodes, roots}) {
	const requestedRanges = new Map();

	function addRange(node, name, range) {
		if (!requestedRanges.has(node)) {
			requestedRanges.set(node, []);
		}

		requestedRanges.get(node).push(resolveAlias(name, range).range);
	}

	for (const root of roots.values()) {
		for (const {dependencyName, range} of root.declarations) {
			addRange(root.node, dependencyName, range);
		}
	}

	for (const node of nodes.values()) {
		for (const {child, dependencyName, range} of node.children) {
			addRange(child, dependencyName, range);
		}
	}

	const versionsByName = new Map();

	for (const node of nodes.values()) {
		if (semver.valid(node.version)) {
			if (!versionsByName.has(node.name)) {
				versionsByName.set(node.name, []);
			}

			versionsByName.get(node.name).push(node.version);
		}
	}

	for (const node of nodes.values()) {
		const ranges = requestedRanges.get(node);

		if (!node.outdated || !ranges) {
			continue;
		}

		const candidates = versionsByName
			.get(node.name)
			.filter((version) => semver.gt(version, node.version))
			.sort(semver.rcompare);

		node.duplicateOf = candidates.find((version) =>
			ranges.every(
				(range) =>
					semver.validRange(range) && semver.satisfies(version, range)
			)
		);
	}
}

function markOutdatedReachability(nodes) {
	const parents = new Map();

	for (const node of nodes.values()) {
		for (const {child} of node.children) {
			if (!parents.has(child)) {
				parents.set(child, []);
			}

			parents.get(child).push(node);
		}
	}

	const queue = [...nodes.values()].filter((node) => node.outdated);

	for (const node of queue) {
		node.reachesOutdated = true;
	}

	while (queue.length) {
		const node = queue.shift();

		for (const parent of parents.get(node) || []) {
			if (!parent.reachesOutdated) {
				parent.reachesOutdated = true;
				queue.push(parent);
			}
		}
	}
}

/**
 * Turns an `npm:` alias range into the real package name and range.
 */
function resolveAlias(name, range) {
	if (!range.startsWith('npm:')) {
		return {name, range};
	}

	const {name: realName, range: realRange} = splitPattern(range.substring(4));

	return {name: realName, range: realRange || '*'};
}

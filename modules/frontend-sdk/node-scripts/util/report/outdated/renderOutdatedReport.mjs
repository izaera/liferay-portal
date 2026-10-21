/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import semver from 'semver';

/**
 * Renders the outdated packages report as a self-contained HTML page.
 *
 * The graph is embedded as JSON and the page builds the tables in the browser.
 */
export default function renderOutdatedReport(
	graph,
	{dir, hasAdvisories, hasOwners, refreshCommand, registry}
) {
	const data = serializeGraph(graph, {
		dir,
		hasAdvisories,
		hasOwners,
		refreshCommand,
		registry,
	});

	const json = JSON.stringify(data).replace(/</g, '\\u003c');

	return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Outdated Packages</title>
<style>${STYLE}</style>
</head>
<body>
<header>
<h1>Outdated Packages</h1>
<p id="summary"></p>
<input id="filter" type="search" placeholder="Filter by package or team name, or by:&lt;top-level dependency&gt; for the packages it pulls in" autocomplete="off">
<div class="options" id="levels">
<span class="muted">Show packages that are behind by a:</span>
<label class="option"><input checked data-level="major" type="checkbox"> <span class="badge major">major</span></label>
<label class="option"><input checked data-level="minor" type="checkbox"> <span class="badge minor">minor</span></label>
<label class="option"><input checked data-level="patch" type="checkbox"> <span class="badge patch">patch</span></label>
</div>
<nav>
<button class="tab active" data-view="roots" type="button">Top-level dependencies</button>
<button class="tab" data-view="flat" type="button">All outdated packages</button>
<button class="tab" data-view="refresh" type="button">Lock refresh</button>
</nav>
<p class="legend">
<span class="legend-item" data-views="roots flat"><span class="badge deprecated">deprecated</span> the package is deprecated in the registry</span>
<span class="legend-item" data-views="roots flat"><span class="badge vulnerable">vulnerable</span> the package has known security advisories</span>
<span class="legend-item" data-views="roots"><span class="badge vulnerable">N vulnerable</span> how many of the outdated packages it pulls in have known security advisories</span>
<span class="legend-item" data-views="roots"><span class="badge unmaintained">no release since</span> the package has not published a release in two years</span>
<span class="legend-item" data-views="roots flat refresh"><span class="badge duplicate">duplicate of x.y.z</span> a newer version is already locked and every range that requests this one accepts it, so refreshing <code>yarn.lock</code> merges them</span>
<span class="legend-item" data-views="roots flat-actions"><span class="action refresh">refresh lock</span> a newer release fits the requested range, so refreshing <code>yarn.lock</code> is enough</span>
<span class="legend-item" data-views="flat-actions"><span class="action raise-range">raise range</span> change the range declared in <code>package.json</code></span>
<span class="legend-item" data-views="flat-actions"><span class="action upgrade-parent">upgrade parent</span> only a newer release of the package above brings it in</span>
<span class="legend-item" data-views="flat-actions"><span class="action blocked">blocked</span> even the latest release of the package above still requires it</span>
<span class="legend-item" data-views="roots flat-actions"><span class="action resolution">resolution</span> a <code>resolutions</code> entry pins it</span>
</p>
</header>
<main>
<section id="roots"></section>
<section id="flat" hidden></section>
<section id="refresh" hidden></section>
</main>
<script type="application/json" id="data">${json}</script>
<script>${SCRIPT}</script>
</body>
</html>
`;
}

function describeAction(action, {latest, parentName}) {
	if (!action) {
		return null;
	}

	switch (action.type) {
		case 'blocked':
			return `${parentName}@${action.parentLatest} (latest) still requires an outdated release`;

		case 'raise-range':
			return `raise the declared range to include ${action.target}`;

		case 'refresh':
			return action.target === latest
				? `refresh yarn.lock to ${action.target}`
				: `refresh yarn.lock to ${action.target} (latest is ${latest})`;

		case 'resolution':
			return `pinned by the "${action.pattern}" resolution`;

		case 'upgrade-parent':
			return action.dropped
				? `upgrade ${parentName} to ${action.parentTarget}, which no longer depends on it`
				: `upgrade ${parentName} to ${action.parentTarget} to get ${action.target}`;

		default:
			return 'unable to tell (not a registry range)';
	}
}

const SEVERITIES = ['low', 'moderate', 'high', 'critical'];

const UNMAINTAINED_AGE = 2 * 365 * 24 * 60 * 60 * 1000;

function getHealth(root) {
	const {node} = root;

	const severity = getMaxSeverity(node.advisories);

	const transitiveVulnerableNodes = [...root.outdatedNodes].filter(
		(candidate) => candidate !== node && candidate.advisories?.length
	);

	const transitiveSeverity = Math.max(
		-1,
		...transitiveVulnerableNodes.map((vulnerableNode) =>
			getMaxSeverity(vulnerableNode.advisories)
		)
	);

	const unmaintained =
		!!node.latestReleasedAt &&
		Date.now() - Date.parse(node.latestReleasedAt) > UNMAINTAINED_AGE;

	return {
		deprecated: !!node.deprecated,
		latestReleasedAt: node.latestReleasedAt,
		priority:
			node.deprecated || severity >= 2
				? 0
				: severity >= 0 || unmaintained
					? 1
					: 2,
		releasedAt: node.releasedAt,
		severity: SEVERITIES[severity],
		transitiveSeverity: SEVERITIES[transitiveSeverity],
		transitiveVulnerableCount: transitiveVulnerableNodes.length,
		unmaintained,
	};
}

function getMaxSeverity(advisories = []) {
	return Math.max(
		-1,
		...advisories.map((advisory) => SEVERITIES.indexOf(advisory.severity))
	);
}

function serializeGraph(
	{nodes, roots},
	{dir, hasAdvisories, hasOwners, refreshCommand, registry}
) {
	const indexes = new Map();
	const serializedNodes = [];

	function index(node) {
		if (!indexes.has(node)) {
			indexes.set(node, serializedNodes.length);
			serializedNodes.push(node);
		}

		return indexes.get(node);
	}

	const healthByRoot = new Map();

	for (const root of roots.values()) {
		healthByRoot.set(root, getHealth(root));
	}

	const sortedRoots = [...roots.values()]
		.filter((root) => root.outdatedNodes.size)
		.sort(
			(a, b) =>
				healthByRoot.get(a).priority - healthByRoot.get(b).priority ||
				SEVERITIES.indexOf(healthByRoot.get(b).transitiveSeverity) -
					SEVERITIES.indexOf(
						healthByRoot.get(a).transitiveSeverity
					) ||
				healthByRoot.get(b).transitiveVulnerableCount -
					healthByRoot.get(a).transitiveVulnerableCount ||
				b.outdatedNodes.size - a.outdatedNodes.size ||
				countMajors(b) - countMajors(a) ||
				a.node.name.localeCompare(b.node.name)
		);

	const actionsByNode = new Map();

	function addAction(node, action) {
		if (!actionsByNode.has(node)) {
			actionsByNode.set(node, new Map());
		}

		actionsByNode.get(node).set(action.text, action.type);
	}

	const serializedRoots = sortedRoots.map((root) => {
		let action = null;

		if (root.action) {
			action = {
				text: describeAction(root.action, {latest: root.node.latest}),
				type: root.action.type,
			};

			addAction(root.node, action);

			if (root.action.type === 'refresh') {
				root.node.refreshTo = root.action.target;
			}
		}

		return {
			action,
			declarations: root.declarations.map(
				({manifest, owners, range, type}) => [
					manifest,
					type,
					range,
					owners || [],
				]
			),
			health: healthByRoot.get(root),
			node: index(root.node),
		};
	});

	const rootsByOutdatedNode = new Map();

	sortedRoots.forEach((root, rootIndex) => {
		for (const node of root.outdatedNodes) {
			if (!rootsByOutdatedNode.has(node)) {
				rootsByOutdatedNode.set(node, []);
			}

			rootsByOutdatedNode.get(node).push(rootIndex);
		}
	});

	// The actions of the edges that lead to outdated packages, which the
	// tables list next to each package

	for (const node of nodes.values()) {
		if (!node.reachesOutdated) {
			continue;
		}

		for (const {action, child} of node.children) {
			if (!action || !child.outdated) {
				continue;
			}

			addAction(child, {
				text: describeAction(action, {
					latest: child.latest,
					parentName: node.name,
				}),
				type: action.type,
			});

			if (
				action.type === 'refresh' &&
				(!child.refreshTo || semver.gt(action.target, child.refreshTo))
			) {
				child.refreshTo = action.target;
			}
		}
	}

	const outdated = [...rootsByOutdatedNode.keys()]
		.sort(
			(a, b) =>
				a.name.localeCompare(b.name) ||
				a.version.localeCompare(b.version, undefined, {numeric: true})
		)
		.map((node) => ({
			actions: [...(actionsByNode.get(node) || new Map())].map(
				([text, type]) => ({text, type})
			),
			node: index(node),
			roots: rootsByOutdatedNode.get(node),
		}));

	return {
		dir,
		generated: new Date().toISOString(),
		hasAdvisories,
		hasOwners,
		nodes: serializedNodes.map((node) => ({
			advisories: node.advisories?.map(({severity, title, url}) => ({
				severity,
				title,
				url,
			})),
			deprecated: node.deprecated,
			diff: node.diff,
			duplicateOf: node.duplicateOf,
			latest: node.latest,
			lockHeaders: node.refreshTo
				? [...node.lockHeaders].sort()
				: undefined,
			name: node.name,
			outdated: node.outdated,
			refreshDiff: node.refreshTo
				? getDiff(node.version, node.refreshTo)
				: undefined,
			refreshTo: node.refreshTo,
			version: node.version,
		})),
		outdated,
		packageCount: nodes.size,
		refreshCommand,
		registry,
		roots: serializedRoots,
	};
}

function getDiff(version, target) {
	const diff = semver.diff(version, target);

	return diff ? diff.replace(/^pre/, '').replace(/^release$/, 'patch') : null;
}

function countMajors(root) {
	let count = 0;

	for (const node of root.outdatedNodes) {
		if (node.diff === 'major') {
			count++;
		}
	}

	return count;
}

const STYLE = `
:root {
	--bg: #fff;
	--fg: #1c1c24;
	--muted: #6b6c7e;
	--border: #e7e7ed;
	--row: #f7f8f9;
	--major: #da1414;
	--minor: #b95000;
	--patch: #287d3c;
	--refresh: #287d3c;
	--raise-range: #0b5fff;
	--upgrade-parent: #6a4bcf;
	--blocked: #da1414;
	--resolution: #6b6c7e;
	color-scheme: light dark;
}

@media (prefers-color-scheme: dark) {
	:root {
		--bg: #18191d;
		--fg: #e7e7ed;
		--muted: #a7a9bc;
		--border: #33353d;
		--row: #212227;
		--major: #ff6b6b;
		--minor: #f2a65a;
		--patch: #5bc77a;
		--refresh: #5bc77a;
		--raise-range: #6ea8ff;
		--upgrade-parent: #a891ff;
		--blocked: #ff6b6b;
		--resolution: #a7a9bc;
	}
}

body {
	background: var(--bg);
	color: var(--fg);
	font: 14px/1.5 system-ui, sans-serif;
	margin: 0;
}

header, main {
	margin: 0 auto;
	max-width: 1200px;
	padding: 0 16px;
}

h1 {
	font-size: 22px;
	margin: 24px 0 4px;
}

#summary, .legend, .muted {
	color: var(--muted);
}

#filter {
	background: var(--bg);
	border: 1px solid var(--border);
	border-radius: 6px;
	box-sizing: border-box;
	color: var(--fg);
	font: inherit;
	margin: 8px 0;
	padding: 8px 10px;
	width: 100%;
}

nav {
	border-bottom: 1px solid var(--border);
	display: flex;
	gap: 4px;
	margin-top: 8px;
}

.tab {
	background: none;
	border: 0;
	border-bottom: 2px solid transparent;
	color: var(--muted);
	cursor: pointer;
	font: inherit;
	padding: 8px 12px;
}

.tab.active {
	border-bottom-color: var(--raise-range);
	color: var(--fg);
	font-weight: 600;
}

.legend {
	font-size: 12px;
	line-height: 2;
}

.legend .action {
	margin-left: 12px;
}

.legend .action:first-child {
	margin-left: 0;
}

.name {
	font-weight: 600;
}

.version {
	font-family: ui-monospace, monospace;
	font-size: 12px;
}

.badge, .action {
	border: 1px solid currentColor;
	border-radius: 10px;
	font-size: 11px;
	margin-left: 6px;
	padding: 0 6px;
	white-space: nowrap;
}

.major { color: var(--major); }
.minor { color: var(--minor); }
.patch { color: var(--patch); }
.refresh { color: var(--refresh); }
.raise-range { color: var(--raise-range); }
.upgrade-parent { color: var(--upgrade-parent); }
.blocked { color: var(--blocked); }
.resolution, .unknown { color: var(--resolution); }
.duplicate { color: var(--refresh); }
.vulnerable { color: var(--major); }
.unmaintained { color: var(--minor); }

.intro {
	color: var(--muted);
	margin: 12px 0 0;
}

.command {
	background: var(--row);
	border: 1px solid var(--border);
	border-radius: 6px;
	font-family: ui-monospace, monospace;
	font-size: 12px;
	margin: 6px 0;
	overflow-x: auto;
	padding: 6px 10px;
}

.intro p {
	margin: 0;
}

.copy {
	background: none;
	border: 1px solid var(--border);
	border-radius: 6px;
	color: var(--fg);
	cursor: pointer;
	font: inherit;
	margin-left: 8px;
	padding: 2px 10px;
}

.patterns {
	font-family: ui-monospace, monospace;
	font-size: 12px;
	min-width: 18em;
	overflow-wrap: anywhere;
	white-space: pre-wrap;
}

.deprecated {
	color: var(--major);
}

.legend-item {
	margin-right: 12px;
}

.declarations {
	color: var(--muted);
	font-size: 12px;
}

#filter.flash {
	animation: flash 2s ease-out;
}

@keyframes flash {
	0%, 50% {
		background: color-mix(in srgb, var(--raise-range) 20%, var(--bg));
		border-color: var(--raise-range);
		box-shadow: 0 0 0 3px color-mix(in srgb, var(--raise-range) 35%, transparent);
	}
}

tr.highlight td {
	background: color-mix(in srgb, var(--raise-range) 15%, transparent);
}

table {
	border-collapse: collapse;
	margin: 12px 0;
	width: 100%;
}

th, td {
	border-bottom: 1px solid var(--border);
	padding: 6px 8px;
	text-align: left;
	vertical-align: top;
}

tbody tr:nth-child(odd) {
	background: var(--row);
}

td .action {
	display: inline-block;
	margin: 0 0 2px;
	white-space: normal;
}

a {
	color: var(--raise-range);
}

.table-wrapper {
	overflow-x: auto;
}

.options {
	display: flex;
	flex-wrap: wrap;
	gap: 4px 24px;
	margin-top: 12px;
}

.option {
	color: var(--muted);
	display: inline-flex;
	gap: 6px;
}

table.hide-actions th:nth-child(4),
table.hide-actions td:nth-child(4),
table.hide-roots th:nth-child(5),
table.hide-roots td:nth-child(5) {
	display: none;
}
`;

const SCRIPT = `
const data = JSON.parse(document.getElementById('data').textContent);
const {nodes, outdated, roots} = data;

const LEVELS = {major: 1, minor: 2, patch: 4};

let selectedLevels = LEVELS.major | LEVELS.minor | LEVELS.patch;

function el(tag, className, text) {
	const element = document.createElement(tag);

	if (className) {
		element.className = className;
	}

	if (text !== undefined) {
		element.textContent = text;
	}

	return element;
}

function describeDeprecated(container, node) {
	if (node.deprecated) {
		const badge = el('span', 'badge deprecated', 'deprecated');

		badge.title = node.deprecated;
		container.append(badge);
	}
}

function getMaxSeverity(advisories) {
	const severities = ['low', 'moderate', 'high', 'critical'];

	return severities[Math.max(...advisories.map((advisory) => severities.indexOf(advisory.severity)))];
}

function describeHealth(container, root) {
	const node = nodes[root.node];
	const {health} = root;

	describeDeprecated(container, node);

	if (node.advisories) {
		const badge = el('span', 'badge vulnerable', 'vulnerable (' + health.severity + ')');

		badge.title = node.advisories.map((advisory) => advisory.severity + ': ' + advisory.title).join('\\n');
		container.append(badge);
	}

	if (health.unmaintained) {
		container.append(el('span', 'badge unmaintained', 'no release since ' + health.latestReleasedAt.substring(0, 7)));
	}
}

function describeDuplicate(container, node) {
	if (node.duplicateOf) {
		const badge = el('span', 'badge duplicate', 'duplicate of ' + node.duplicateOf);

		badge.title = node.name + '@' + node.duplicateOf + ' is already locked and every range that requests ' + node.version + ' accepts it, so refreshing yarn.lock merges them.';
		container.append(badge);
	}
}

function describeAction(container, action) {
	if (action) {
		container.append(el('span', 'action ' + action.type, action.text));
	}
}

function renderRoots() {
	const tbody = document.querySelector('#roots tbody');

	tbody.textContent = '';

	roots.forEach((root, rootIndex) => {
		const node = nodes[root.node];
		const row = el('tr');
		const teams = getTeams(root);

		row.id = 'root-' + rootIndex;
		row.dataset.level = node.outdated ? node.diff : 'none';
		row.dataset.names = [node.name, ...(data.hasOwners ? teams : [])].join(' ');
		row.dataset.roots = rootIndex;

		const nameCell = el('td');

		const declarations = root.declarations.map(([manifest, type, range]) => manifest + ' (' + type + ' ' + range + ')');
		const declarationsDiv = el('div', 'declarations', 'Declared in ' + (declarations.length === 1 ? declarations[0] : declarations.length + ' package.json files'));

		declarationsDiv.title = declarations.join('\\n');
		nameCell.append(el('span', 'name', node.name), declarationsDiv);

		const lockedCell = el('td');

		lockedCell.append(el('span', 'version', node.version));
		describeDuplicate(lockedCell, node);

		if (root.health.releasedAt) {
			lockedCell.title = 'Released on ' + root.health.releasedAt.substring(0, 10);
		}

		const latestCell = el('td');

		if (node.outdated) {
			latestCell.append(el('span', 'version', node.latest), el('span', 'badge ' + node.diff, node.diff));

			// The latest version already tells how far to raise the declared
			// range

			if (root.action && root.action.type !== 'raise-range') {
				latestCell.append(el('br'));
				describeAction(latestCell, root.action);
			}
		}
		else {
			latestCell.append(el('span', 'muted', 'up to date'));
		}

		if (root.health.latestReleasedAt) {
			latestCell.title = 'Released on ' + root.health.latestReleasedAt.substring(0, 10);
		}

		const healthCell = el('td');

		describeHealth(healthCell, root);

		const pullsInCell = el('td');
		const count = outdated.filter((entry) => entry.node !== root.node && entry.roots.includes(rootIndex)).length;

		if (count) {
			const link = el('a', null, count + ' outdated');

			link.href = '#flat';
			link.addEventListener('click', (event) => {
				event.preventDefault();
				showView('flat', {reset: true});
				setFilter('by:' + getRootLabel(rootIndex));
			});

			pullsInCell.append(link);
		}

		if (root.health.transitiveVulnerableCount) {
			const badge = el('span', 'badge vulnerable', root.health.transitiveVulnerableCount + ' vulnerable (' + root.health.transitiveSeverity + ')');

			badge.title = root.health.transitiveVulnerableCount + ' of the outdated packages it pulls in ' + (root.health.transitiveVulnerableCount === 1 ? 'has' : 'have') + ' security advisories';
			pullsInCell.append(badge);
		}

		row.append(nameCell, lockedCell, latestCell, healthCell, pullsInCell);

		if (data.hasOwners) {
			row.append(el('td', null, teams.join(', ')));
		}

		tbody.append(row);
	});
}

function createRoots() {
	const section = document.getElementById('roots');

	const options = el('div', 'options');
	const option = el('label', 'option');
	const checkbox = el('input');

	checkbox.checked = true;
	checkbox.id = 'show-up-to-date';
	checkbox.type = 'checkbox';
	checkbox.addEventListener('change', applyFilters);
	option.append(checkbox, 'Show the top-level dependencies that are up to date but pull in outdated packages');
	options.append(option);

	const wrapper = el('div', 'table-wrapper');
	const table = el('table');
	const head = el('tr');

	for (const label of ['Dependency', 'Locked', 'Latest', 'Health', 'Pulls in', ...(data.hasOwners ? ['Teams'] : [])]) {
		head.append(el('th', null, label));
	}

	const thead = el('thead');

	thead.append(head);
	table.append(thead, el('tbody'));
	wrapper.append(table);
	section.append(options, wrapper);

	renderRoots();
}

const rootNameCounts = roots.reduce((counts, root) => {
	const {name} = nodes[root.node];

	counts[name] = (counts[name] || 0) + 1;

	return counts;
}, {});

/**
 * Returns the name of a top-level dependency, adding its version when several
 * versions of it are declared.
 */
function getRootLabel(rootIndex) {
	const node = nodes[roots[rootIndex].node];

	return rootNameCounts[node.name] > 1 ? node.name + '@' + node.version : node.name;
}

function describeOwners(owners) {
	return owners.length ? owners.join(', ') : 'unowned';
}

function getTeams(root) {
	return [...new Set(root.declarations.map(([, , , owners]) => describeOwners(owners)))].sort();
}

function renderFlat() {
	const section = document.getElementById('flat');
	const wrapper = el('div', 'table-wrapper');
	const table = el('table');
	const head = el('tr');

	for (const label of ['Package', 'Locked', 'Latest', 'Action', 'Pulled in by', ...(data.hasOwners ? ['Teams'] : [])]) {
		head.append(el('th', null, label));
	}

	const thead = el('thead');

	thead.append(head);

	const tbody = el('tbody');

	for (const entry of outdated) {
		const node = nodes[entry.node];
		const row = el('tr');

		const teams = data.hasOwners ? [...new Set(entry.roots.flatMap((rootIndex) => getTeams(roots[rootIndex])))].sort() : [];

		row.dataset.level = node.diff;
		row.dataset.names = [node.name, ...entry.roots.map((rootIndex) => nodes[roots[rootIndex].node].name), ...teams].join(' ');
		row.dataset.node = entry.node;
		row.dataset.roots = entry.roots.join(' ');

		const nameCell = el('td');

		nameCell.append(el('span', 'name', node.name));
		describeDeprecated(nameCell, node);

		if (node.advisories) {
			const badge = el('span', 'badge vulnerable', 'vulnerable (' + getMaxSeverity(node.advisories) + ')');

			badge.title = node.advisories.map((advisory) => advisory.severity + ': ' + advisory.title).join('\\n');
			nameCell.append(badge);
		}

		const lockedCell = el('td');

		lockedCell.append(el('span', 'version', node.version));
		describeDuplicate(lockedCell, node);

		const latestCell = el('td');

		latestCell.append(el('span', 'version', node.latest), el('span', 'badge ' + node.diff, node.diff));

		const actionCell = el('td');

		for (const action of entry.actions) {
			describeAction(actionCell, action);
			actionCell.append(el('br'));
		}

		const rootsCell = el('td');

		entry.roots.forEach((rootIndex, i) => {
			const link = el('a', null, getRootLabel(rootIndex));

			link.href = '#root-' + rootIndex;
			link.addEventListener('click', (event) => {
				event.preventDefault();
				showView('roots', {reset: true});

				const rootRow = document.getElementById('root-' + rootIndex);

				rootRow.scrollIntoView({block: 'center'});
				rootRow.classList.add('highlight');
				setTimeout(() => rootRow.classList.remove('highlight'), 2000);
			});

			if (i) {
				rootsCell.append(', ');
			}

			rootsCell.append(link);
		});

		row.append(nameCell, lockedCell, latestCell, actionCell, rootsCell);

		if (data.hasOwners) {
			row.append(el('td', null, teams.join(', ')));
		}
		tbody.append(row);
	}

	table.append(thead, tbody);
	wrapper.append(table);

	const options = el('div', 'options');

	for (const [className, label, checked] of [
		['hide-actions', 'Hide the "Action" column', true],
		['hide-roots', 'Hide the "Pulled in by" column', false],
	]) {
		const option = el('label', 'option');
		const checkbox = el('input');

		checkbox.checked = checked;
		checkbox.type = 'checkbox';
		table.classList.toggle(className, checked);
		checkbox.addEventListener('change', () => {
			table.classList.toggle(className, checkbox.checked);
			updateLegend();
		});

		option.append(checkbox, label);
		options.append(option);
	}

	section.append(options, wrapper);
}

// The search and level checkboxes of each tab, which come back when the tab is
// shown again

const filtersByView = {};

function showView(view, {reset = false} = {}) {
	const activeView = document.querySelector('.tab.active').dataset.view;

	filtersByView[activeView] = {
		levels: [...document.querySelectorAll('#levels input')].map((checkbox) => checkbox.checked),
		query: document.getElementById('filter').value,
	};

	for (const tab of document.querySelectorAll('.tab')) {
		tab.classList.toggle('active', tab.dataset.view === view);
	}

	for (const id of ['flat', 'refresh', 'roots']) {
		document.getElementById(id).hidden = view !== id;
	}

	const filters = (!reset && filtersByView[view]) || {levels: [true, true, true], query: ''};

	document.getElementById('filter').value = filters.query;

	document.querySelectorAll('#levels input').forEach((checkbox, i) => {
		checkbox.checked = filters.levels[i];
	});

	if (reset && view === 'roots') {
		document.getElementById('show-up-to-date').checked = true;
	}

	updateSelectedLevels();
	applyFilters();
	updateLegend();
}

function updateLegend() {
	const view = document.querySelector('.tab.active').dataset.view;
	const flatTable = document.querySelector('#flat table');

	const views = new Set([view]);

	if (view === 'flat' && flatTable && !flatTable.classList.contains('hide-actions')) {
		views.add('flat-actions');
	}

	for (const item of document.querySelectorAll('.legend-item')) {
		item.hidden = !item.dataset.views.split(' ').some((itemView) => views.has(itemView));
	}
}

for (const tab of document.querySelectorAll('.tab')) {
	tab.addEventListener('click', () => showView(tab.dataset.view));
}

function applyFilters() {
	const query = document.getElementById('filter').value.trim().toLowerCase();

	// "by:<top-level dependency>" matches that dependency in the first tab and
	// the packages it pulls in (itself excluded) in the other ones

	const byLabel = query.match(/^by:\\s*(.+)$/)?.[1];
	const byRoot = byLabel ? roots.findIndex((root, rootIndex) => getRootLabel(rootIndex).toLowerCase() === byLabel) : -1;

	function matches(item) {
		if (byLabel) {
			return byRoot !== -1 && item.dataset.roots.split(' ').includes(String(byRoot)) && Number(item.dataset.node) !== roots[byRoot].node;
		}

		return !query || item.dataset.names.toLowerCase().includes(query);
	}

	for (const row of document.querySelectorAll('#flat tbody tr, #refresh tbody tr')) {
		row.hidden = !matches(row) || !(LEVELS[row.dataset.level] & selectedLevels);
	}

	const showUpToDate = document.getElementById('show-up-to-date').checked;

	// Top-level dependencies are filtered by their own level, and the ones
	// that are up to date by their own checkbox

	for (const row of document.querySelectorAll('#roots tbody tr')) {
		const {level} = row.dataset;

		row.hidden = !matches(row) || (level === 'none' ? !showUpToDate : !(LEVELS[level] & selectedLevels));
	}
}

document.getElementById('filter').addEventListener('input', applyFilters);

/**
 * Fills the text filter and flashes it so that users notice why the table
 * shows fewer rows.
 */
function setFilter(text) {
	const filter = document.getElementById('filter');

	filter.value = text;
	applyFilters();

	filter.classList.remove('flash');
	void filter.offsetWidth;
	filter.classList.add('flash');
	filter.scrollIntoView({behavior: 'smooth', block: 'nearest'});
}

for (const checkbox of document.querySelectorAll('#levels input')) {
	checkbox.addEventListener('change', () => {
		updateSelectedLevels();
		applyFilters();
	});
}

function updateSelectedLevels() {
	selectedLevels = 0;

	for (const checkbox of document.querySelectorAll('#levels input')) {
		if (checkbox.checked) {
			selectedLevels |= LEVELS[checkbox.dataset.level];
		}
	}
}

function renderRefresh() {
	const section = document.getElementById('refresh');
	const entries = outdated.filter((entry) => nodes[entry.node].refreshTo);

	const intro = el('div', 'intro');

	intro.append(el('p', null, 'These ' + entries.length + ' packages get a newer release just by refreshing yarn.lock, without touching any package.json. To refresh them:'));

	const steps = el('ol');
	const copyStep = el('li', null, 'Copy the yarn.lock entries of the rows shown. Narrow them down with the filters first if needed; refreshes behind by a major may break things, so consider unchecking "major".');
	const copy = el('button', 'copy', 'Copy the visible entries');

	copy.type = 'button';
	copy.addEventListener('click', () => {
		const headers = [...section.querySelectorAll('tbody tr:not([hidden]) .patterns')].map((cell) => cell.textContent);

		navigator.clipboard.writeText(headers.join('\\n') + '\\n').then(() => {
			copy.textContent = 'Copied ' + headers.length + (headers.length === 1 ? ' entry' : ' entries');
		});
	});

	copyStep.append(copy);

	const runStep = el('li', null, 'From the root of liferay-portal, run the command below, paste the entries, and press Ctrl+D. It deletes them from yarn.lock and runs yarn install to resolve them again.');
	const command = el('pre', 'command', data.refreshCommand);

	runStep.append(command);
	steps.append(copyStep, runStep);
	intro.append(steps);

	const wrapper = el('div', 'table-wrapper');
	const table = el('table');
	const head = el('tr');

	for (const label of ['Package', 'Locked', 'Refresh to', 'yarn.lock entry', 'Pulled in by', ...(data.hasOwners ? ['Teams'] : [])]) {
		head.append(el('th', null, label));
	}

	const thead = el('thead');

	thead.append(head);

	const tbody = el('tbody');

	for (const entry of entries) {
		const node = nodes[entry.node];
		const row = el('tr');
		const rootNames = entry.roots.map(getRootLabel);
		const teams = data.hasOwners ? [...new Set(entry.roots.flatMap((rootIndex) => getTeams(roots[rootIndex])))].sort() : [];

		row.dataset.level = node.refreshDiff;
		row.dataset.names = [node.name, ...rootNames, ...teams].join(' ');
		row.dataset.node = entry.node;
		row.dataset.roots = entry.roots.join(' ');

		const nameCell = el('td');

		nameCell.append(el('span', 'name', node.name));

		const lockedCell = el('td');

		lockedCell.append(el('span', 'version', node.version));
		describeDuplicate(lockedCell, node);

		const refreshCell = el('td');

		refreshCell.append(el('span', 'version', node.refreshTo), el('span', 'badge ' + node.refreshDiff, node.refreshDiff));

		row.append(
			nameCell,
			lockedCell,
			refreshCell,
			el('td', 'patterns', node.lockHeaders.join('\\n')),
			el('td', null, rootNames.join(', '))
		);

		if (data.hasOwners) {
			row.append(el('td', null, teams.join(', ')));
		}

		tbody.append(row);
	}

	table.append(thead, tbody);
	wrapper.append(table);
	section.append(intro, wrapper);
}

document.getElementById('summary').textContent =
	roots.length + ' top-level dependencies pull in outdated packages (' +
	roots.filter((root) => nodes[root.node].outdated).length + ' are outdated themselves, ' +
	roots.filter((root) => root.health.deprecated).length + ' deprecated, ' +
	(data.hasAdvisories ? roots.filter((root) => nodes[root.node].advisories).length + ' with security advisories, ' + roots.filter((root) => root.health.transitiveVulnerableCount).length + ' pulling in outdated packages with security advisories, ' : '') +
	roots.filter((root) => root.health.unmaintained).length + ' unmaintained). ' +
	outdated.length + ' of the ' + data.packageCount + ' locked packages are outdated, and a lock refresh updates ' +
	outdated.filter((entry) => nodes[entry.node].refreshTo).length + ' of them. Lockfile: ' + data.dir +
	', registry: ' + data.registry + ', generated ' + new Date(data.generated).toLocaleString() + '.';

createRoots();
renderRefresh();
renderFlat();
updateLegend();
applyFilters();
`;

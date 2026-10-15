/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const DEPENDENCY_SECTIONS = new Set(['dependencies', 'optionalDependencies']);

/**
 * Parses a Yarn v1 lockfile.
 *
 * @param {string} content the lockfile contents
 * @return {Map<string, object>} a map from each `name@range` pattern to its
 *     entry (`{dependencies, header, name, range, resolved, version}`),
 *     where `dependencies` maps dependency names to the ranges they request
 *     and `header` is the line that opens the entry
 */
export default function parseYarnLock(content) {
	const entries = new Map();

	let entry;
	let section;

	for (const line of content.split(/\r?\n/)) {
		if (!line.trim() || line.startsWith('#')) {
			continue;
		}

		const indentation = line.length - line.trimStart().length;

		if (indentation === 0) {
			entry = {dependencies: {}, header: line};
			section = null;

			for (const pattern of splitPatterns(line.replace(/:$/, ''))) {
				entries.set(pattern, {...splitPattern(pattern), entry});
			}
		}
		else if (indentation === 2) {
			const [key, value] = splitKeyValue(line.trim());

			if (value === undefined) {
				section = key.replace(/:$/, '');
			}
			else {
				entry[key] = value;
				section = null;
			}
		}
		else if (DEPENDENCY_SECTIONS.has(section)) {
			const [name, range] = splitKeyValue(line.trim());

			entry.dependencies[name] = range;
		}
	}

	const result = new Map();

	for (const [pattern, {entry, name, range}] of entries) {
		result.set(pattern, {
			dependencies: entry.dependencies,
			header: entry.header,
			name,
			range,
			resolved: entry.resolved,
			version: entry.version,
		});
	}

	return result;
}

/**
 * Splits a `name@range` pattern, taking scoped names into account.
 */
export function splitPattern(pattern) {
	const index = pattern.indexOf('@', 1);

	return {
		name: pattern.substring(0, index),
		range: pattern.substring(index + 1),
	};
}

function splitKeyValue(text) {
	const match = text.match(/^("(?:[^"\\]|\\.)*"|\S+)(?:\s+(.*))?$/);

	if (!match) {
		return [text];
	}

	return [
		unquote(match[1]),
		match[2] === undefined ? undefined : unquote(match[2]),
	];
}

function splitPatterns(text) {
	return text.split(/,\s*/).map((pattern) => unquote(pattern.trim()));
}

function unquote(text) {
	if (text.startsWith('"') && text.endsWith('"')) {
		return text.slice(1, -1);
	}

	return text;
}

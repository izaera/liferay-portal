/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import crypto from 'crypto';
import fs from 'fs/promises';
import path from 'path';
import semver from 'semver';

import print from '../../print.mjs';

const CACHE_MAX_AGE = 24 * 60 * 60 * 1000;

const CHUNK_SIZE = 250;

/**
 * Asks the registry's bulk advisory endpoint which of the given package
 * versions have known security advisories.
 *
 * @param {Map<string, Set<string>>} versionsByName the locked versions of each
 *     package
 * @return {Map<string, Array<object>>} a map from `name@version` to its
 *     advisories (`{id, severity, title, url}`), or `null` when the registry
 *     does not answer
 */
export default async function fetchAdvisories(
	versionsByName,
	{cacheDir, refresh, registry}
) {
	const advisories = new Map();
	const names = [...versionsByName.keys()].sort();

	try {
		for (let i = 0; i < names.length; i += CHUNK_SIZE) {
			const body = {};

			for (const name of names.slice(i, i + CHUNK_SIZE)) {
				body[name] = [...versionsByName.get(name)].sort();
			}

			const response = await fetchChunk(body, {
				cacheDir,
				refresh,
				registry,
			});

			for (const [name, entries] of Object.entries(response)) {
				for (const version of body[name] || []) {
					const matching = entries.filter(
						(entry) =>
							semver.valid(version) &&
							semver.satisfies(version, entry.vulnerable_versions)
					);

					if (matching.length) {
						advisories.set(
							`${name}@${version}`,
							matching.map(({id, severity, title, url}) => ({
								id,
								severity,
								title,
								url,
							}))
						);
					}
				}
			}
		}
	}
	catch (error) {
		print(
			1,
			print.warning('WARNING:'),
			`Unable to fetch security advisories: ${error.message}`
		);

		return null;
	}

	return advisories;
}

async function fetchChunk(body, {cacheDir, refresh, registry}) {
	const json = JSON.stringify(body);

	const cacheFile = path.join(
		cacheDir,
		`advisories-${crypto.createHash('sha256').update(json).digest('hex')}.json`
	);

	if (!refresh) {
		try {
			const stat = await fs.stat(cacheFile);

			if (Date.now() - stat.mtimeMs < CACHE_MAX_AGE) {
				return JSON.parse(await fs.readFile(cacheFile, 'utf8'));
			}
		}
		catch (error) {
			if (error.code !== 'ENOENT') {
				throw error;
			}
		}
	}

	const response = await fetch(
		`${registry.replace(/\/$/, '')}/-/npm/v1/security/advisories/bulk`,
		{
			body: json,
			headers: {'content-type': 'application/json'},
			method: 'POST',
		}
	);

	if (!response.ok) {
		throw new Error(`HTTP ${response.status}`);
	}

	const result = await response.json();

	await fs.mkdir(cacheDir, {recursive: true});
	await fs.writeFile(cacheFile, JSON.stringify(result));

	return result;
}

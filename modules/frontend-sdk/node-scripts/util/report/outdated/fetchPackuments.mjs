/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs/promises';
import path from 'path';

import print from '../../print.mjs';

const ACCEPT_HEADER =
	'application/vnd.npm.install-v1+json; q=1.0, application/json; q=0.8';

const CACHE_MAX_AGE = 24 * 60 * 60 * 1000;

const CONCURRENCY = 16;

/**
 * Fetches the registry metadata (packument) of the given packages.
 *
 * By default it fetches the abbreviated packument, which holds the dist tags
 * and the dependencies of each version. With `full`, it fetches the full one
 * instead and keeps only the publish date of each version.
 *
 * Responses are cached in `cacheDir` for a day so that successive runs do not
 * hit the registry again.
 *
 * @return {Map<string, object>} a map from package name to its packument, or
 *     to `null` when the registry does not know the package
 */
export default async function fetchPackuments(
	names,
	{cacheDir, full = false, refresh, registry}
) {
	await fs.mkdir(cacheDir, {recursive: true});

	const packuments = new Map();
	const queue = [...names].sort();
	const total = queue.length;

	let fetched = 0;

	async function worker() {
		while (queue.length) {
			const name = queue.shift();

			packuments.set(
				name,
				await fetchPackument(name, {cacheDir, full, refresh, registry})
			);

			fetched++;

			if (fetched % 250 === 0) {
				print(
					1,
					print.info('INFO:'),
					`Fetched metadata of ${fetched} of ${total} packages`
				);
			}
		}
	}

	await Promise.all(Array.from({length: CONCURRENCY}, worker));

	return packuments;
}

async function fetchPackument(name, {cacheDir, full, refresh, registry}) {
	const cacheFile = path.join(
		cacheDir,
		`${name.replace('/', '__')}${full ? '.dates' : ''}.json`
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

	const url = `${registry.replace(/\/$/, '')}/${name.replace('/', '%2f')}`;

	for (let attempt = 1; ; attempt++) {
		try {
			const response = await fetch(url, {
				headers: {accept: full ? 'application/json' : ACCEPT_HEADER},
			});

			if (response.status === 404) {
				await fs.writeFile(cacheFile, 'null');

				return null;
			}

			if (!response.ok) {
				throw new Error(`HTTP ${response.status}`);
			}

			const json = await response.json();

			const packument = full
				? {distTags: json['dist-tags'] || {}, time: json.time || {}}
				: simplifyPackument(json);

			await fs.writeFile(cacheFile, JSON.stringify(packument));

			return packument;
		}
		catch (error) {
			if (attempt === 3) {
				throw new Error(
					`Unable to fetch the metadata of ${name} from ${url}: ${error.message}`
				);
			}
		}
	}
}

/**
 * Keeps only what the report needs: the dist tags and, for each version, its
 * dependencies and deprecation message.
 */
function simplifyPackument(packument) {
	const versions = {};

	for (const [version, manifest] of Object.entries(
		packument.versions || {}
	)) {
		versions[version] = {
			dependencies: {
				...manifest.optionalDependencies,
				...manifest.dependencies,
			},
			deprecated: manifest.deprecated || undefined,
		};
	}

	return {
		distTags: packument['dist-tags'] || {},
		versions,
	};
}

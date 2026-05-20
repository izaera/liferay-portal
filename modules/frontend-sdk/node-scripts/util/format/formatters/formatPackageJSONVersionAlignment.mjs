/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import fs from 'fs';
import path from 'path';

import getYarnWorkspaceProjects from '../../getYarnWorkspaceProjects.mjs';
import {MODULES_DIR, PORTAL_DIR} from '../../locations.mjs';
import print from '../../print.mjs';

const ALLOWED_VERSION_DIVERGENCES = {
	typescript: [
		'modules/apps/change-tracking/change-tracking-rest-client-js',
		'modules/apps/headless/headless-admin-taxonomy/headless-admin-taxonomy-client-js',
		'modules/apps/object/object-admin-rest-client-js',
		'modules/util/portal-tools-rest-builder-test-client-js',
	],
};

const EXCLUDED_WORKSPACES = [
	// 'test/playwright'
];

const NON_EXPLICIT_VERSION_RE = /[\^~<>|]|\s|^=/;

export default async function formatPackageJSONVersionAlignment() {
	let checksPassed = true;

	print(
		1,
		print.subTitle(
			`> Checking 'package.json' dependency version alignment...\n`
		)
	);

	const versionsByName = new Map();
	const nonExplicitVersions = new Map();

	const rootPkg = JSON.parse(
		fs.readFileSync(path.join(MODULES_DIR, 'package.json'), 'utf-8')
	);
	const rootDependencies = {
		...rootPkg.dependencies,
		...rootPkg.devDependencies,
	};

	for (const name of Object.keys(ALLOWED_VERSION_DIVERGENCES)) {
		if (!(name in rootDependencies)) {
			print(
				2,
				print.error('ERROR:'),
				'Dependency',
				print.underline(name),
				"is allowed to diverge but has no entry in 'modules/package.json'"
			);
			print(2, '');

			checksPassed = false;
		}
	}

	const dirs = [
		MODULES_DIR,
		...(await getYarnWorkspaceProjects()).filter(
			(dir) =>
				!EXCLUDED_WORKSPACES.includes(path.relative(MODULES_DIR, dir))
		),
	];

	for (const dir of dirs) {
		const pkgPath = path.join(dir, 'package.json');
		let pkg;

		try {
			pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf-8'));
		}
		catch (error) {
			continue;
		}

		const relPath = path.relative(PORTAL_DIR, pkgPath);
		const projectRelPath = path.relative(PORTAL_DIR, dir);

		for (const section of ['dependencies', 'devDependencies']) {
			if (!pkg[section]) {
				continue;
			}

			for (const [name, version] of Object.entries(pkg[section])) {
				if (NON_EXPLICIT_VERSION_RE.test(version)) {
					if (!nonExplicitVersions.has(name)) {
						nonExplicitVersions.set(name, new Map());
					}

					const filesByVersion = nonExplicitVersions.get(name);

					if (!filesByVersion.has(version)) {
						filesByVersion.set(version, new Set());
					}

					filesByVersion.get(version).add(relPath);
				}

				const allowedProjects = ALLOWED_VERSION_DIVERGENCES[name];

				if (allowedProjects && allowedProjects.includes(projectRelPath)) {
					continue;
				}

				if (!versionsByName.has(name)) {
					versionsByName.set(name, new Map());
				}

				const filesByVersion = versionsByName.get(name);

				if (!filesByVersion.has(version)) {
					filesByVersion.set(version, new Set());
				}

				filesByVersion.get(version).add(relPath);
			}
		}
	}

	const violations = [];

	for (const [name, filesByVersion] of versionsByName) {
		if (filesByVersion.size > 1) {
			violations.push([name, filesByVersion]);
		}
	}

	violations.sort((a, b) => a[0].localeCompare(b[0]));

	for (const [name, filesByVersion] of violations) {
		print(
			2,
			print.error('ERROR:'),
			'Dependency',
			print.underline(name),
			'has misaligned versions across modules:'
		);

		const sortedVersions = [...filesByVersion.entries()].sort(
			(a, b) => b[1].size - a[1].size
		);

		for (const [version, files] of sortedVersions) {
			print(3, `${version} in:`);

			for (const file of [...files].sort()) {
				print(4, file);
			}
		}

		print(2, '');

		checksPassed = false;
	}

	const nonExplicitEntries = [...nonExplicitVersions.entries()].sort(
		(a, b) => a[0].localeCompare(b[0])
	);

	// for (const [name, filesByVersion] of nonExplicitEntries) {
	// 	print(
	// 		2,
	// 		print.error('ERROR:'),
	// 		'Dependency',
	// 		print.underline(name),
	// 		'uses non-explicit version(s):'
	// 	);

	// 	const sortedVersions = [...filesByVersion.entries()].sort(
	// 		(a, b) => b[1].size - a[1].size
	// 	);

	// 	for (const [version, files] of sortedVersions) {
	// 		print(3, `${version} in:`);

	// 		for (const file of [...files].sort()) {
	// 			print(4, file);
	// 		}
	// 	}

	// 	print(2, '');

	// 	checksPassed = false;
	// }

	return checksPassed;
}

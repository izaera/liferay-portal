/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default function getDependenciesVersionNumbers(
	fileJSONs,
	dependencyType
) {
	return fileJSONs.reduce((dependenciesVersionNumbers, fileJSON) => {
		const {[dependencyType]: dependencies} = fileJSON.json;

		if (!dependencies) {
			return dependenciesVersionNumbers;
		}

		for (const [dependency, version] of Object.entries(dependencies)) {
			if (!dependenciesVersionNumbers[dependency]) {
				dependenciesVersionNumbers[dependency] = {
					paths: [],
					versions: new Set(),
				};
			}

			dependenciesVersionNumbers[dependency].paths.push(fileJSON.path);
			dependenciesVersionNumbers[dependency].versions.add(version);
		}

		return dependenciesVersionNumbers;
	}, {});
}

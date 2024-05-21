/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import getMainEntryPoints from '../../configuration/getMainEntryPoints.mjs';
import getProjectDependencies from '../../configuration/getProjectDependencies.mjs';
import getProjectDescription from '../../configuration/getProjectDescription.mjs';
import writeTsconfig from './writeTsconfig.mjs';

export default async function main() {
	const [
		mainEntryPoints,
		projectDependencies,
		projectDescription
	] = await Promise.all([
		getMainEntryPoints(),
		getProjectDependencies(),
		getProjectDescription()
	]);

	await Promise.all([
		writeTsconfig(mainEntryPoints, projectDependencies, projectDescription)
	]);
}

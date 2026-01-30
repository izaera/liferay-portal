/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

Liferay.FrontendESM = {
	_prefix: '', // This is initialized in PortalURLBuilderImplDynamicInclude

	buildURL(
		callerScriptURL: string,
		contextPath: string,
		submodule: string
	): string {
		const baseURL = new URL(`${callerScriptURL}`);

		baseURL.search = '';
		baseURL.hash = '';

		const prefix = getPrefix(baseURL.toString());

		return `${baseURL.toString()}/../${prefix}/${Liferay.FrontendESM._prefix}/${contextPath}/__liferay__/${submodule}.js`;
	},
};

function getPrefix(baseURL: string): string {
	let index;

	if (baseURL.includes('/combo/')) {
		index = baseURL.indexOf('/combo/');
	}
	else if (baseURL.includes('/combo?')) {
		index = baseURL.indexOf('/combo?');
	}
	else if (baseURL.includes('/o/js/-/')) {
		index = baseURL.indexOf('/o/js/-/');
	}
	else if (baseURL.includes('/o/')) {
		index = baseURL.indexOf('/o/');
	}
	else {
		throw new Error(`Invalid base URL: ${baseURL}`);
	}

	let prefix = '';

	const depth = baseURL.substring(index + 1).split('/').length - 1;

	for (let i = 0; i < depth; i++) {
		prefix += prefix.length ? '/..' : '..';
	}

	return prefix;
}

/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

const MARKER = '%c@liferay/personalization:';

export function log(...things: string[]): void {

	// eslint-disable-next-line no-console
	console.log(MARKER, 'color: blue; font-weight: bold;', ...things);
}

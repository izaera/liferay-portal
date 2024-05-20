/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React, {useMemo} from 'react';

export default function render(
	renderable:
		| NonNullable<React.ReactNode>
		| NonNullable<React.ForwardRefExoticComponent<any>>
		| (() => NonNullable<React.ReactNode>),
	renderData: {
		componentId?: string;
		portletId?: string;
		[key: string]: unknown;
	},
	container: Element
) {
}
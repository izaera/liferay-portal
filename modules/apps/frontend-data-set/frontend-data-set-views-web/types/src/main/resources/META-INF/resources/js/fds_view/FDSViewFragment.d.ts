/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/// <reference types="react" />

import PropTypes from 'prop-types';
declare function FDSViewFragment({
	id,
	filters,
	views,
	...otherProps
}: {
	id: string;
	filters: any;
	views: any[];
}): JSX.Element;
declare namespace FDSViewFragment {
	var propTypes: {
		id: PropTypes.Requireable<string>;
		filters: PropTypes.Requireable<any[]>;
		views: PropTypes.Requireable<any[]>;
	};
}
export default FDSViewFragment;

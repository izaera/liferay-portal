/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClientExtension} from 'frontend-js-components-web';
import PropTypes from 'prop-types';
import React from "react";

const getSelectedItemsLabel =
	(filterProps) =>
		filterProps.cxFilterImpl.getSelectedItemsLabel(filterProps);
const getOdataString =
	(filterProps) => filterProps.cxFilterImpl.getOdataString(filterProps);

function ClientExtensionFilter({
	cxFilterImpl,
	selectedData,
	setFilter,
}) {
	return (
		<ClientExtension
			args={{
				filter: {
					selectedData
				},
				setFilter: ({
					selectedData,
				}) =>
					setFilter({
						active: true,
						selectedData,
					}),
			}}
			htmlElementBuilder={cxFilterImpl.htmlElementBuilder}
		/>
	);
}

ClientExtensionFilter.propTypes = {
	cxFilterImpl: PropTypes.shape({
		getOdataString: PropTypes.func,
		getSelectedItemsLabel: PropTypes.func,
		htmlElementBuilder: PropTypes.func,
	}),
	selectedData: PropTypes.shape({
		exclude: PropTypes.bool,
		data: PropTypes.object,
	}),
	setFilter: PropTypes.func.isRequired,
};

export default {
	Component: ClientExtensionFilter,
	getSelectedItemsLabel,
	getOdataString
};

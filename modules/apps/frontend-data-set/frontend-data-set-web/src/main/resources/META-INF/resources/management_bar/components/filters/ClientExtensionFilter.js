/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClientExtension} from 'frontend-js-components-web';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from "react";

const getSelectedItemsLabel = ({selectedData}) => {
	return '';
};

const getOdataString = ({entityFieldType, id, selectedData}) => {
	return '';
};

function ClientExtensionFilter({
	entityFieldType,
	esmURL,
	id,
	selectedData,
	setFilter,
}) {
	const [htmlElementBuilder, setHTMLElementBuilder] = useState(() =>
		(() => document.createElement("div"))
	);

	useEffect(() => {
		const getModule = async () => {
			const cetModule = await import(
				/* webpackIgnore: true */ esmURL
			);

			setHTMLElementBuilder(() => cetModule['default']);
		};

		getModule();
	}, [esmURL]);

	return (
		<ClientExtension
			args={{
				filter: {
					selectedData
				},
				setFilter: ({
					odataFilterString,
					selectedData,
					selectedItemsLabel
				}) =>
					setFilter({
						active: true,
						id: id,
						... {
							odataFilterString,
							selectedData,
							selectedItemsLabel
						},
					}),
			}}
			htmlElementBuilder={htmlElementBuilder}
		/>
	);
}

ClientExtensionFilter.propTypes = {
	entityFieldType: PropTypes.string,
	esmURL: PropTypes.string,
	id: PropTypes.string.isRequired,
	selectedData: PropTypes.shape({
		exclude: PropTypes.bool,
		data: PropTypes.object,
	}),
	setFilter: PropTypes.func.isRequired,
};

export {getSelectedItemsLabel, getOdataString};
export default ClientExtensionFilter;

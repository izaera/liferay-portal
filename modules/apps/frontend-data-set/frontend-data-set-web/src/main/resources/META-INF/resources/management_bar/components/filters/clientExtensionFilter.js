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
	esmURL,
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
					selectedData,
				}) =>
					setFilter({
						active: true,
						selectedData,
					}),
			}}
			htmlElementBuilder={htmlElementBuilder}
		/>
	);
}

ClientExtensionFilter.propTypes = {
	esmURL: PropTypes.string,
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

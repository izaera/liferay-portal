/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

export default function ({
	clientExtensionItemSelectorURL,
	namespace,
}) {
	const fm = document.getElementById(`${namespace}fm`);
	const addResources = document.getElementById(`${namespace}addResources`);

	addURLButtonsEventListeners(
		fm, clientExtensionItemSelectorURL, `${namespace}itemSelected`);

	updateURLFieldsVisibility(fm, addResources.value);

	addResources.addEventListener('change', (event) => {
		updateURLFieldsVisibility(fm, event.target.value);
/*
		Liferay.fire('urlModeChanged', {
			mode: event.target.value
		});
*/
	});
}

function addURLButtonsEventListeners(fm, clientExtensionItemSelectorURL, selectEventName) {
	const manageResourcesButton = fm.querySelector('.add-resources-summary button');

	manageResourcesButton.addEventListener('click', (event) => {
		Liferay.Util.openSelectionModal(
			{
				iframeBodyCssClass: '',
				title: Liferay.Language.get('add-resources'),
				url: clientExtensionItemSelectorURL
			}
		);
	});


	const buttons = fm.querySelectorAll('.url-input-field button');

	for (const button of buttons) {
		button.addEventListener('click', (event) => {
			Liferay.Util.openSelectionModal(
				{
					iframeBodyCssClass: '',
					onSelect: function (event) {
						var selectedItem = event.value;

						if (selectedItem) {
							var itemValue = JSON.parse(selectedItem.value);

							window.alert(JSON.stringify(itemValue, null, 2));
						}
					},
					selectEventName,
					title: Liferay.Language.get('add-resources'),
					url: clientExtensionItemSelectorURL
				}
			);
		});
	}
}

function updateURLFieldsVisibility(fm, mode) {
	const inputs = fm.querySelectorAll('.url-input-field input');
	const disabled = mode == "fromComputer" ? true : false;

	for (const input of inputs) {
		input.disabled = disabled;
	}

	const addResourcesSummary = fm.querySelector('.add-resources-summary');
	const buttonWrappers = fm.querySelectorAll('.url-input-field .button-wrapper');
	const display = mode == "fromComputer" ? 'flex' : 'none';

	addResourcesSummary.style.display = display;

	for (const buttonWrapper of buttonWrappers) {
		buttonWrapper.style.display = display;
	}
}
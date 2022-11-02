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
	namespace,
}) {
	const fm = document.getElementById(`${namespace}fm`);
	const addResources = document.getElementById(`${namespace}addResources`);

	addURLButtonsEventListeners(fm);

	updateURLButtonsVisibility(fm, addResources.value);

	addResources.addEventListener('change', (event) => {
		updateURLButtonsVisibility(fm, event.target.value);
/*
		Liferay.fire('urlModeChanged', {
			mode: event.target.value
		});
*/
	});
}

function addURLButtonsEventListeners(fm) {
	for (const element of fm.elements) {
		if (element.dataset.isUrlButton) {
			element.addEventListener('click', (event) => {
				Liferay.Util.openSelectionModal(
					{
						onSelect: function (event) {
							var selectedItem = event.value;

							if (selectedItem) {
								var itemValue = JSON.parse(selectedItem.value);

								showItemSelectorValue.innerText = JSON.stringify(
									itemValue,
									null,
									2
								);
							}
						},
						selectEventName: '<portlet:namespace />itemSelected',
						title: '<liferay-ui:message key="add-resources" />',
						url: '<%= clientExtensionItemSelectorURL.toString() %>'
					}
				);
			});
		}
	}
}

function updateURLButtonsVisibility(fm, mode) {
	let disabled = false;
	let display = 'none';

	if (mode == 'fromComputer') {
		disabled = true;
		display = 'flex';
	}

	for (const element of fm.elements) {
		if (element.dataset.isUrlButton) {
			element.parentElement.style.display = display;
		}

		if (element.dataset.isUrlInput) {
			element.disabled = disabled;
		}
	}
}
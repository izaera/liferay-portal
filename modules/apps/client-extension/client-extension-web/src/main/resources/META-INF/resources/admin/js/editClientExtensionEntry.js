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

import {createPortletURL, openToast} from 'frontend-js-web';

export default function ({
	clientExtensionItemSelectorURL,
	getFileEntryURLURL,
	getManageResourcesSummaryURL,
	itemSelectedEventName,
	namespace,
}) {
	const fm = document.getElementById(`${namespace}fm`);

	const state = {
		activeURLInput: null,
		addResources: fm.querySelector(`#${namespace}addResources`),
		clientExtensionItemSelectorURL,
		fm,
		getFileEntryURLURL,
		getManageResourcesSummaryURL,
		itemSelectedEventName,
		manageResources: fm.querySelector('.manage-resources'),
		manageResourcesButton: fm.querySelector('.manage-resources button'),
		manageResourcesSummary:	fm.querySelector('.manage-resources .summary'),
		modalId: `${namespace}addResourcesDialog`,
		namespace,
	};

	// Initialize

	updateURLFieldsVisibility(state);

	// Handle events

	state.addResources.addEventListener(
		'change', () => updateURLFieldsVisibility(state));

	addURLButtonsEventListeners(state);

	Liferay.on(itemSelectedEventName, ({data}) => {
		const value = JSON.parse(data.value);
		const {fileEntryId} = value;

		if (state.activeURLInput) {
			fetchResource(
				`${getFileEntryURLURL.replace('FILE_ENTRY_ID', fileEntryId)}`)
			.then(({url}) => {
				state.activeURLInput.value = url;
			});

			updateManageResourcesSummary(state);

			Liferay.fire('closeModal', state.modalId)
		}
	});
}

function updateManageResourcesSummary(state) {
	fetchResource(state.getManageResourcesSummaryURL).then(({text}) => {
		state.manageResourcesSummary.innerHTML = text;
	});
}

function addURLButtonsEventListeners(state) {
	const modalProps = {
		id: state.modalId,
		iframeBodyCssClass: '',
		onClose: () => updateManageResourcesSummary(state),
		title: Liferay.Language.get('add-resources'),
		url: state.clientExtensionItemSelectorURL
	};

	state.manageResourcesButton.addEventListener('click', (event) => {
		state.activeURLInput = null;

		Liferay.Util.openModal(modalProps);
	});

	const buttons = findURLInputButtons(state);

	for (const button of buttons) {
		button.addEventListener('click', (event) => {
			state.activeURLInput = findURLInput(button);

			Liferay.Util.openModal(modalProps);
		});
	}
}

async function fetchResource(resourceURL) {
	return new Promise((resolve, reject) => {
		Liferay.Util.fetch(
			resourceURL
		).then(response => {
			return response.json();
		}).then(json => {
			if (json.error) {
				openToast({
					message: json.error,
					title: Liferay.Language.get('error'),
					type: 'danger',
				});
			}
			else {
				resolve(json);
			}
		});
	});
}

function findURLInput(button) {
	let parent = button.parentElement;

	while (!parent.classList.contains("url-input-field")) {
		parent = parent.parentElement;
	}

	return parent.querySelector('input');
}

function findURLInputButtons(state) {
	return state.fm.querySelectorAll('.url-input-field button');
}

function updateURLFieldsVisibility(state) {
	const {fm} = state;
	const inputs = fm.querySelectorAll('.url-input-field input');
	const disabled = state.addResources.value == "fromComputer" ? true : false;

	for (const input of inputs) {
		input.disabled = disabled;
	}

	const buttonWrappers = fm.querySelectorAll('.url-input-field .button-wrapper');
	const display = state.addResources.value == "fromComputer" ? 'flex' : 'none';

	state.manageResources.style.display = display;

	for (const buttonWrapper of buttonWrappers) {
		buttonWrapper.style.display = display;
	}
}
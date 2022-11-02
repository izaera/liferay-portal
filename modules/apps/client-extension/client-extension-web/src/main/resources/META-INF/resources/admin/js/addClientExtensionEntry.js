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
	addClientExtensionEntryURL,
	modalId,
	namespace,
}) {
	const form = document.getElementById(`${namespace}fm`);

	form.addEventListener('submit', (event) => {
		event.preventDefault();

		const name = form.querySelector(`#${namespace}name`).value;

		if (!name) {
			openToast({
				message: Liferay.Language.get('please-enter-a-valid-name'),
				title: Liferay.Language.get('error'),
				type: 'danger',
			});

			return;
		}

		Liferay.Util.fetch(
			addClientExtensionEntryURL,
			{
				body: new FormData(form),
				method: 'POST'
			}
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
				Liferay.Util.getOpener().Liferay.fire(
					'closeModal',
					{
						modalId,
						redirect: json.redirect,
					}
				)
			}
		});
	});
}

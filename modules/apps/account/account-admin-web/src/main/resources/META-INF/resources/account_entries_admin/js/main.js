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

Liferay.on('portletReady', (event) => {
	const {portletId} = event;

	if (
		!portletId.startsWith(
			'com_liferay_account_admin_web_internal_portlet_AccountEntriesAdminPortlet'
		)
	) {
		return;
	}

	const portletNamespace = '_' + portletId + '_';

	window[`${portletNamespace}addDomains`] = (eventName) => {
		const domainsInput = document.getElementById(
			`${portletNamespace}domain`
		);

		const domains = domainsInput.value.split(',');

		// Email domain regex from aui-form-validator.js

		const pattern = new RegExp(
			'^((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)+(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$',
			'i'
		);

		for (const domain of domains) {
			if (!pattern.test(domain.trim())) {
				const domainAlert = document.getElementById(
					`${portletNamespace}domainAlert`
				);

				domainAlert.classList.remove('hide');

				domainsInput.focus();

				return;
			}
		}

		const openingLiferay = Liferay.Util.getOpener().Liferay;

		openingLiferay.fire(eventName, {
			data: document.getElementById(`${portletNamespace}domain`).value,
		});

		openingLiferay.fire('closeModal');
	};
});

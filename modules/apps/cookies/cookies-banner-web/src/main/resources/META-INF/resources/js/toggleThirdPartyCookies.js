/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {COOKIE_TYPES, checkConsent} from 'frontend-js-web';

function flipThirdPartyCookie(type) {
	const selector = type ? `[data-third-party-cookie="${type}"]` : '[data-third-party-cookie]'

	document.querySelectorAll(selector).forEach(
		element => {
			element.removeAttribute('data-third-party-cookie')

			switch (element.tagName) {
				case 'SCRIPT': {
					const newScript = element.cloneNode();

					newScript.type = 'text/javascript';

					element.replaceWith(newScript);
					break;
				}
				case 'LINK': {
					const newLink = element.cloneNode();

					newLink.href = element.dataset['href'];
					newLink.removeAttribute('data-href');

					element.replaceWith(newLink);
					break;
				}
				case 'EMBED':
				case 'IFRAME':
				case 'IMG':
					element.src = element.dataset['src'];
					element.removeAttribute('data-src');
					break;
				default:
					// eslint-disable-next-line no-console
					console.log('Error: ', element.tagName);
			}
		}
	)
}

export default function toggleThirdPartyCookies() {
	Object.values(COOKIE_TYPES).forEach(
		type => {
			if (checkConsent(type)) {
				flipThirdPartyCookie(type)
			}
		}
	)
}
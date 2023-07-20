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
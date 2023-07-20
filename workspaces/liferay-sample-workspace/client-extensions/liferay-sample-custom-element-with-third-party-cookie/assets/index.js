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

(function () {
	'use strict';

	class CustomElementThirdPartyCookie extends HTMLElement {
		constructor() {
			super();

			const div = document.createElement('div')
			
			const link = document.createElement('link');
			link.setAttribute('data-href', 'https://liferay-3rd-party-cookie.vercel.app/api/set-cookie.css?type=link');
			link.setAttribute('rel', 'stylesheet');
			link.setAttribute('type', 'text/css');
			link.setAttribute('data-third-party-cookie', 'CONSENT_TYPE_PERSONALIZATION');
			document.head.appendChild(link);
			
			const script = document.createElement('script');
			script.setAttribute('src', 'https://liferay-3rd-party-cookie.vercel.app/api/set-cookie.js?type=script');
			script.setAttribute('type', 'text/plain');
			script.setAttribute('data-third-party-cookie', 'CONSENT_TYPE_PERSONALIZATION');
			document.head.appendChild(script);
	
			const img = document.createElement('img');
			img.setAttribute('data-src', 'https://liferay-3rd-party-cookie.vercel.app/api/set-cookie?type=img');
			img.setAttribute('data-third-party-cookie', 'CONSENT_TYPE_PERSONALIZATION');
			div.appendChild(img);
	
			const embed = document.createElement('embed');
			embed.setAttribute('data-src', 'https://liferay-3rd-party-cookie.vercel.app/api/set-cookie?type=embed');
			embed.setAttribute('data-third-party-cookie', 'CONSENT_TYPE_PERSONALIZATION');
			div.appendChild(embed);
	
			const iframe = document.createElement('iframe');
			iframe.setAttribute('data-src', 'https://liferay-3rd-party-cookie.vercel.app/api/set-cookie?type=iframe');
			iframe.setAttribute('data-third-party-cookie', 'CONSENT_TYPE_PERSONALIZATION');
			div.appendChild(iframe);
	
			this.root = div;
			this.rendered = false;
		}

		connectedCallback() {
			if (!this.rendered) {
				this.rendered = true;
				this.appendChild(this.root);
				this.root = this;
			}
		}
		
	}

	if (!customElements.get('third-party-cookie')) {
		customElements.define('third-party-cookie', CustomElementThirdPartyCookie);
	}
})();

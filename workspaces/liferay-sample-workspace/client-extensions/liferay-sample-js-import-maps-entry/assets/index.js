/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {audiences} from '/o/frontend-js-audiences-web/__liferay__/index.js';

const AUDIENCE_ID = '35137';

audiences.on(
	AUDIENCE_ID,
	function showFirefoxMessage() {
		const MESSAGE_ID = 'firefox_message';

		// If the message is already present, don't do anything (to avoid multiple personalizations
		// due to SPA navigations).
		const currentMessage = document.getElementById(MESSAGE_ID);

		if (currentMessage) return;

		// Look for the banner where we will add the language hint
		const div = document.getElementById('banner');

		if (!div) return;

		// Add the hint
		const message = document.createElement('div');

		message.id = MESSAGE_ID;
		message.style.color = 'white';
		message.style.backgroundColor = '#0000C0';
		message.style.paddingBottom = '1em';
		message.style.paddingTop = '1em';
		message.style.textAlign = 'center';

		message.innerHTML = `
			Hey, we see you are using Firefox 🦊! We welcome you as a FOSS supporter! 🎉
		`;

		div.appendChild(message);
	}
);


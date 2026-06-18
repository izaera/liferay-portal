import {audiences} from '/o/frontend-js-audiences-web/__liferay__/index.js';

const AUDIENCE_ID = 'the_audience';

audiences.on(
	AUDIENCE_ID,
	function showFirefoxMessage() {
		const MESSAGE_ID = 'firefox_message';

		// If the message is already present, don't do anything (to
		// avoid multiple personalizations due to SPA navigations).
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

		message.innerHTML = `Hey, we see you are using Firefox!`;

		div.appendChild(message);
	}
);
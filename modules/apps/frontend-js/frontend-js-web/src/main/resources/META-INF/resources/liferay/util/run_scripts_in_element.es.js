/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {v4 as uuidv4} from 'uuid';

function runJSFromText(sourceScriptElement, next, appendFn) {
	const {text, type} = sourceScriptElement;
	const scriptElement = document.createElement('script');

	if (Liferay.CSP.nonce) {
		scriptElement.setAttribute('nonce', Liferay.CSP.nonce);
	}

	const id = uuidv4();

	scriptElement.id = id;
	scriptElement.text =
`
${text}

console.log('SPA: about to remove', '${id}');

document.getElementById('${id}').remove();

console.log('SPA: after removing', '${id}');
`;
	scriptElement.type = type;

	const callback = function (event) {
		console.log('SPA: script error', event, text);
	};

	scriptElement.addEventListener('error', callback, {once: true});

	console.log('SPA: about to execute', id, text);

	if (appendFn) {
		appendFn(scriptElement);
	}
	else {
		document.head.appendChild(scriptElement);
	}

	next();
}

function runJSFromFile(sourceScriptElement, next, appendFn) {
	const {src, type} = sourceScriptElement;
	const scriptElement = document.createElement('script');

	if (Liferay.CSP.nonce) {
		scriptElement.setAttribute('nonce', Liferay.CSP.nonce);
	}

	scriptElement.src = src;
	scriptElement.type = type;

	const callback = function () {
		scriptElement.remove();

		next();
	};

	scriptElement.addEventListener('load', callback, {once: true});
	scriptElement.addEventListener('error', callback, {once: true});

	if (appendFn) {
		appendFn(scriptElement);
	}
	else {
		document.head.appendChild(scriptElement);
	}
}

function runScriptsInOrder(scripts, i, defaultFn, appendFn) {
	const scriptElement = scripts[i];

	const runNextScript = () => {
		if (i < scripts.length - 1) {
			runScriptsInOrder(scripts, i + 1, defaultFn, appendFn);
		}
		else if (defaultFn) {
			setTimeout(defaultFn);
		}
	};

	if (!scriptElement) {
		return;
	}
	else if (
		scriptElement.type &&
		scriptElement.type !== 'text/javascript' &&
		scriptElement.type !== 'module' &&
		scriptElement.type !== 'module-shim'
	) {
		runNextScript();
	}
	else {
		scriptElement.remove();

		if (scriptElement.src) {
			runJSFromFile(scriptElement, runNextScript, appendFn);
		}
		else {
			runJSFromText(scriptElement, runNextScript, appendFn);
		}
	}
}

export default function (element, defaultFn, appendFn) {
	const scripts = element.querySelectorAll('script');

	if (!scripts.length && defaultFn) {
		setTimeout(defaultFn);

		return;
	}

	runScriptsInOrder(scripts, 0, defaultFn, appendFn);
}

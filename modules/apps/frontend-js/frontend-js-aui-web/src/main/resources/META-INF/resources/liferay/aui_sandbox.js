/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

/**
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 */
(function () {
	const ALLOY = YUI();

	if (ALLOY.html5shiv) {
		ALLOY.html5shiv();
	}

	const originalUse = ALLOY.use;

//	let NEXT_CALL_ID = 1;

	ALLOY.use = function () {
//		const callId = NEXT_CALL_ID++;

		const args = Array.prototype.slice.call(arguments, 0);

		const currentURL = Liferay.currentURL;

		const originalCallback = args[args.length - 1];

//console.log(`>>>>> AUI.use(${callId}):`, ...args, currentURL);

		if (typeof originalCallback === 'function') {
//console.log(`>>>>> AUI.use(${callId}):`, 'changing callback');

			args[args.length - 1] = function () {
//console.log(`>>>>> AUI.use(${callId}):`, 'invoking callback', Liferay.currentURL, currentURL);

				if (Liferay.currentURL === currentURL) {
					originalCallback.apply(this, arguments);
				}
			};
		}

		return originalUse.apply(this, args);
	};

	window.AUI = function () {
		return ALLOY;
	};

	ALLOY.mix(AUI, YUI);

	AUI.$ = window.jQuery;
	AUI._ = window._;
})();

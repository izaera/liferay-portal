/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	Cookie,
	DynamicInlineScroll,
	DynamicSelect,
	MAP_HTML_CHARS_ESCAPED,
	STATUS_CODE,
	addParams,
	autoSize,
	createActionURL,
	createPortletURL,
	createRenderURL,
	createResourceURL,
	debounce,
	escapeHTML,
	focusFormField,
	formatStorage,
	formatXML,
	getCheckedCheckboxes,
	getCountries,
	getCropRegion,
	getDOM,
	getElement,
	getFormElement,
	getGeolocation,
	getLayoutIcons,
	getLexiconIconTpl,
	getOpener,
	getPortletNamespace,
	getRegions,
	getSelectedOptionValues,
	getTop,
	getURLWithSessionId,
	getUncheckedCheckboxes,
	getWindow,
	hideLayoutPane,
	inBrowserView,
	isTablet,
	loadClientExtensions,
	loadEditorClientExtensions,
	localStorage,
	minimizePortlet,
	navigate,
	normalizeFriendlyURL,
	ns,
	objectToFormData,
	objectToURLSearchParams,
	openWindow,
	portlet,
	postForm,
	proposeLayout,
	publishToLive,
	removeEntitySelection,
	runScriptsInElement,
	selectFolder,
	sessionStorage,
	setFormValues,
	showCapsLock,
	showLayoutPane,
	showTab,
	showTooltip,
	sub,
	toCharCode,
	toggleBoxes,
	toggleDisabled,
	toggleLayoutDetails,
	toggleRadio,
	toggleSelectBox,
	unescapeHTML,
	zIndex,
} from '../main/index.es';

/**
 * @deprecated As of Cavanaugh (7.4.x), replaced by `import {STATUS_CODE} from 'frontend-js-web'`
 */
Liferay.STATUS_CODE = STATUS_CODE;

/**
 * @deprecated As of Cavanaugh (7.4.x), replaced by `import {zIndex} from 'frontend-js-web'`
 */
Liferay.zIndex = zIndex;

Liferay.Address = {
	getCountries,
	getRegions,
};

/**
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 */
Liferay.DynamicSelect = DynamicSelect;

Liferay.LayoutExporter = {
	all: hideLayoutPane,
	details: toggleLayoutDetails,
	icons: getLayoutIcons(),
	proposeLayout,
	publishToLive,
	selected: showLayoutPane,
};

Liferay.Portal = {
	Tabs: {
		show: showTab,
	},
	ToolTip: {
		show: showTooltip,
	},
};

Liferay.Portlet = Liferay.Portlet || {};

Liferay.Portlet.minimize = minimizePortlet;

Liferay.Util = Liferay.Util || {};

Liferay.Util.MAP_HTML_CHARS_ESCAPED = MAP_HTML_CHARS_ESCAPED;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {addParams} from 'frontend-js-web'`
 */
Liferay.Util.addParams = addParams;

/**
 * Utils added to global namespace to be consumed by portal-web
 */
Liferay.Util.AutoSize = autoSize;
Liferay.Util.debounce = debounce;
Liferay.Util.DynamicInlineScroll = DynamicInlineScroll;
Liferay.Util.runScriptsInElement = runScriptsInElement;

/**
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 */
Liferay.Util.disableEsc = () => {
	if (document.all && window.event.keyCode === 27) {
		window.event.returnValue = false;
	}
};

const htmlEscapes = {
	'"': '&quot;',
	'&': '&amp;',
	"'": '&#39;',
	'<': '&lt;',
	'>': '&gt;',
};

const reUnescapedHtml = /[&<>"']/g;
const reHasUnescapedHtml = RegExp(reUnescapedHtml.source);

Liferay.Util.escape = (string) => {
	return string && reHasUnescapedHtml.test(string)
		? string.replace(reUnescapedHtml, (chr) => htmlEscapes[chr])
		: string || '';
};
Liferay.Util.escapeHTML = escapeHTML;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {focusFormField} from 'frontend-js-web'`
 */
Liferay.Util.focusFormField = focusFormField;

Liferay.Util.formatStorage = formatStorage;
Liferay.Util.formatXML = formatXML;
Liferay.Util.getCheckedCheckboxes = getCheckedCheckboxes;
Liferay.Util.getUncheckedCheckboxes = getUncheckedCheckboxes;
Liferay.Util.getCropRegion = getCropRegion;

/**
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 */
Liferay.Util.getDOM = getDOM;

/**
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 */
Liferay.Util.getElement = getElement;

Liferay.Util.getGeolocation = getGeolocation;
Liferay.Util.getFormElement = getFormElement;
Liferay.Util.getLexiconIconTpl = getLexiconIconTpl;
Liferay.Util.getOpener = getOpener;
Liferay.Util.getPortletNamespace = getPortletNamespace;
Liferay.Util.getSelectedOptionValues = getSelectedOptionValues;
Liferay.Util.getTop = getTop;
Liferay.Util.getURLWithSessionId = getURLWithSessionId;
Liferay.Util.getWindow = getWindow;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {inBrowserView} from 'frontend-js-web'`
 */
Liferay.Util.inBrowserView = inBrowserView;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {isTablet} from 'frontend-js-web'`
 */
Liferay.Util.isTablet = isTablet;

Liferay.Util.loadClientExtensions = loadClientExtensions;
Liferay.Util.loadEditorClientExtensions = loadEditorClientExtensions;
Liferay.Util.navigate = navigate;
Liferay.Util.ns = ns;
Liferay.Util.objectToFormData = objectToFormData;
Liferay.Util.objectToURLSearchParams = objectToURLSearchParams;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {normalizeFriendlyURL} from 'frontend-js-web'`
 */
Liferay.Util.normalizeFriendlyURL = normalizeFriendlyURL;

Liferay.Util.PortletURL = {
	createActionURL,
	createPortletURL,
	createRenderURL,
	createResourceURL,
};

Liferay.Util.postForm = postForm;
Liferay.Util.setFormValues = setFormValues;
Liferay.Util.toCharCode = toCharCode;

/**
 * @deprecated As of Athanasius (7.3.x), replaced by `import {toggleDisabled} from 'frontend-js-web'`
 */
Liferay.Util.toggleDisabled = toggleDisabled;

Liferay.Util.openWindow = openWindow;
Liferay.Util.removeEntitySelection = removeEntitySelection;
Liferay.Util.selectFolder = selectFolder;
Liferay.Util.showCapsLock = showCapsLock;
Liferay.Util.sub = sub;
Liferay.Util.toggleBoxes = toggleBoxes;
Liferay.Util.toggleRadio = toggleRadio;
Liferay.Util.toggleSelectBox = toggleSelectBox;

const htmlUnescapes = {
	'&#39;': "'",
	'&amp;': '&',
	'&gt;': '>',
	'&lt;': '<',
	'&quot;': '"',
};

const reEscapedHtml = /&(?:amp|lt|gt|quot|#(0+)?39);/g;
const reHasEscapedHtml = RegExp(reEscapedHtml.source);

Liferay.Util.unescape = (string) => {
	return string && reHasEscapedHtml.test(string)
		? string.replace(
				reEscapedHtml,
				(entity) => htmlUnescapes[entity] || "'"
			)
		: string || '';
};

Liferay.Util.unescapeHTML = unescapeHTML;

Liferay.Util.Cookie = Cookie;

Liferay.Util.LocalStorage = localStorage;
Liferay.Util.SessionStorage = sessionStorage;

window.portlet = portlet;

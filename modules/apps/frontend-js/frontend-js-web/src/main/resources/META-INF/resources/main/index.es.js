/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export {default as DefaultEventHandler} from './liferay/DefaultEventHandler.es';
export {default as DynamicInlineScroll} from './liferay/DynamicInlineScroll.es';
export {default as DynamicSelect} from './liferay/DynamicSelect';
export {default as PortletBase} from './liferay/PortletBase.es';
export {
	ALIGN_POSITIONS,
	align,
	getAlignBestRegion,
	getAlignRegion,
	suggestAlignBestRegion,
} from './liferay/align';
export {default as AOP} from './liferay/aop/AOP.es';
export {default as autoSize} from './liferay/autosize/autosize.es';
export {cancelDebounce, debounce} from './liferay/debounce/debounce.es';
export {
	getLayoutIcons,
	hideLayoutPane,
	proposeLayout,
	publishToLive,
	showLayoutPane,
	toggleLayoutDetails,
} from './liferay/layout_exporter.es';
export {showTab} from './liferay/portal/tabs.es';
export {showTooltip} from './liferay/portal/tooltip.es';
export {
	minimizePortlet,
	default as portlet,
} from './liferay/portlet/portlet.es';
export {default as STATUS_CODE} from './liferay/status_code';
export {default as addParams} from './liferay/util/add_params';
export {default as getCountries} from './liferay/util/address/get_countries.es';
export {default as getRegions} from './liferay/util/address/get_regions.es';
export {default as buildFragment} from './liferay/util/build_fragment';
export {CONSENT_TYPES as COOKIE_TYPES} from './liferay/util/consent';
export {checkConsent} from './liferay/util/consent';
export {
	getCookie,
	setCookie,
	removeCookie,
	default as Cookie,
} from './liferay/util/cookie/cookie';
export {default as focusFormField} from './liferay/util/focus_form_field';
export {default as getFormElement} from './liferay/util/form/get_form_element.es';
export {default as objectToFormData} from './liferay/util/form/object_to_form_data.es';
export {default as postForm} from './liferay/util/form/post_form.es';
export {default as setFormValues} from './liferay/util/form/set_form_values.es';
export {default as formatStorage} from './liferay/util/format_storage.es';
export {default as formatXML} from './liferay/util/format_xml.es';
export {
	getCheckedCheckboxes,
	getUncheckedCheckboxes,
} from './liferay/util/get_checkboxes';
export {default as getCropRegion} from './liferay/util/get_crop_region.es';
export {default as getDOM} from './liferay/util/get_dom';
export {default as getElement} from './liferay/util/get_element';
export {default as getGeolocation} from './liferay/util/get_geolocation';
export {default as getLexiconIconTpl} from './liferay/util/get_lexicon_icon_template';
export {default as getOpener} from './liferay/util/get_opener';
export {default as getPortletNamespace} from './liferay/util/get_portlet_namespace.es';
export {default as getSelectedOptionValues} from './liferay/util/get_selected_option_values';
export {default as getTop} from './liferay/util/get_top';
export {default as getURLWithSessionId} from './liferay/util/get_url_with_session_id';
export {default as getWindow} from './liferay/util/get_window';
export {
	escapeHTML,
	unescapeHTML,
	MAP_HTML_CHARS_ESCAPED,
} from './liferay/util/html_util';
export {default as inBrowserView} from './liferay/util/in_browser_view';
export {default as isObject} from './liferay/util/is_object';
export {default as isTablet} from './liferay/util/is_tablet';
export {default as localStorage} from './liferay/util/local_storage';
export {default as memoize} from './liferay/util/memoize';
export {default as navigate} from './liferay/util/navigate.es';
export {default as normalizeFriendlyURL} from './liferay/util/normalize_friendly_url';
export {default as ns} from './liferay/util/ns.es';
export {default as objectToURLSearchParams} from './liferay/util/object_to_url_search_params.es';
export {default as openWindow} from './liferay/util/open_window';
export {default as createActionURL} from './liferay/util/portlet_url/create_action_url.es';
export {default as createPortletURL} from './liferay/util/portlet_url/create_portlet_url.es';
export {default as createRenderURL} from './liferay/util/portlet_url/create_render_url.es';
export {default as createResourceURL} from './liferay/util/portlet_url/create_resource_url.es';
export {default as printPage} from './liferay/util/print_page';
export {isReducedMotion} from './liferay/util/reducedMotion';
export {default as removeEntitySelection} from './liferay/util/remove_entity_selection';
export {default as runScriptsInElement} from './liferay/util/run_scripts_in_element.es';
export {default as selectFolder} from './liferay/util/select_folder';
export {default as sessionStorage} from './liferay/util/session_storage';
export {default as showCapsLock} from './liferay/util/show_caps_lock';
export {default as sub} from './liferay/util/sub';
export {default as toCharCode} from './liferay/util/to_char_code.es';
export {default as toggleBoxes} from './liferay/util/toggle_boxes';
export {default as toggleDisabled} from './liferay/util/toggle_disabled';
export {default as toggleRadio} from './liferay/util/toggle_radio';
export {default as toggleSelectBox} from './liferay/util/toggle_select_box';
export {default as zIndex} from './liferay/zIndex';
export {default as loadClientExtensions} from './utils/client_extensions/loadClientExtensions';
export {default as loadEditorClientExtensions} from './utils/client_extensions/loadEditorClientExtensions';
export {loadModule} from './utils/client_extensions/loadModule';
export {default as dateUtils} from './utils/date_time/index';

// Re-export preloaded global stuff to create a migration path for ES code.
// The idea is to be able to import Liferay stuff as frontend-js-web exports (in
// ES code) so that we may end up removing the Liferay global object one day.

const {
	SideNavigation,
	component,
	componentReady,
	destroyComponent,
	destroyComponents,
	destroyUnfulfilledPromises,
	getComponentCache,
	initComponentCache,
} = Liferay;

const {BREAKPOINTS, Disposable, EventEmitter, EventHandler, throttle} =
	Liferay.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED;

const {
	delegate,
	fetch,
	getLexiconIcon,
	getPortletConfigurationIconAction,
	getPortletId,
	isPhone,
	setPortletConfigurationIconAction,
	toggleControls,
} = Liferay.Util;

const {get: getSessionValue, set: setSessionValue} = Liferay.Util.Session;

export {
	BREAKPOINTS,
	Disposable,
	EventEmitter,
	EventHandler,
	SideNavigation,
	component,
	componentReady,
	delegate,
	destroyComponent,
	destroyComponents,
	destroyUnfulfilledPromises,
	fetch,
	getComponentCache,
	getLexiconIcon,
	getPortletConfigurationIconAction,
	getPortletId,
	getSessionValue,
	initComponentCache,
	isPhone,
	setPortletConfigurationIconAction,
	setSessionValue,
	throttle,
	toggleControls,
};

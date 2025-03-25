/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// Legacy stuff

import './liferay/dom_task_runner';

import './liferay/events';

import './liferay/lazy_load';

import './liferay/liferay';

// ES-like stuff

import BREAKPOINTS from './liferay/breakpoints';
import {
	component,
	componentReady,
	destroyComponent,
	destroyComponents,
	destroyUnfulfilledPromises,
	getComponentCache,
	initComponentCache,
} from './liferay/component.es';
import delegate from './liferay/delegate/delegate.es';
import Disposable from './liferay/events/Disposable';
import EventEmitter from './liferay/events/EventEmitter';
import EventHandler from './liferay/events/EventHandler';
import SideNavigation from './liferay/side_navigation.es';
import throttle from './liferay/throttle.es';
import fetch from './liferay/util/fetch.es';
import getLexiconIcon from './liferay/util/get_lexicon_icon';
import getPortletId from './liferay/util/get_portlet_id';
import isPhone from './liferay/util/is_phone';
import {
	getPortletConfigurationIconAction,
	setPortletConfigurationIconAction,
} from './liferay/util/portlet_configuration_icon_action';
import {getSessionValue, setSessionValue} from './liferay/util/session.es';
import toggleControls from './liferay/util/toggle_controls';

import './liferay/workflow';

Liferay.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED = {
	BREAKPOINTS,
	Disposable,
	EventEmitter,
	EventHandler,
	throttle,
};

Liferay.SideNavigation = SideNavigation;
Liferay.component = component;
Liferay.componentReady = componentReady;
Liferay.destroyComponent = destroyComponent;
Liferay.destroyComponents = destroyComponents;
Liferay.destroyUnfulfilledPromises = destroyUnfulfilledPromises;
Liferay.getComponentCache = getComponentCache;
Liferay.initComponentCache = initComponentCache;

Liferay.Util.Session = {
	get: getSessionValue,
	set: setSessionValue,
};

Liferay.Util.delegate = delegate;
Liferay.Util.fetch = fetch;
Liferay.Util.getLexiconIcon = getLexiconIcon;
Liferay.Util.getPortletConfigurationIconAction =
	getPortletConfigurationIconAction;
Liferay.Util.getPortletId = getPortletId;
Liferay.Util.isPhone = isPhone;
Liferay.Util.setPortletConfigurationIconAction =
	setPortletConfigurationIconAction;
Liferay.Util.toggleControls = toggleControls;

// eslint-disable-next-line @liferay/imports-first
import './liferay/portlet'; // Relies on Liferay.Util.getPortletId

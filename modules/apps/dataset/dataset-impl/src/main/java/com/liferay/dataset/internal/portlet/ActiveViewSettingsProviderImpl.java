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

package com.liferay.dataset.internal.portlet;

import com.liferay.dataset.portlet.ActiveViewSettingsProvider;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = ActiveViewSettingsProvider.class)
public class ActiveViewSettingsProviderImpl
	implements ActiveViewSettingsProvider {

	@Override
	public String getActiveViewSettingsJSON(
		HttpServletRequest httpServletRequest, String id) {

		PortalPreferences portalPreferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(
				httpServletRequest);

		return portalPreferences.getValue(
			_getNamespace(httpServletRequest, id), "activeViewSettingsJSON",
			"{}");
	}

	@Override
	public void setActiveViewSettingsJSON(
		HttpServletRequest httpServletRequest, String id,
		String activeViewSettingsJSON) {

		PortalPreferences portalPreferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(
				httpServletRequest);

		portalPreferences.setValue(
			_getNamespace(httpServletRequest, id), "activeViewSettingsJSON",
			activeViewSettingsJSON);
	}

	private String _getNamespace(
		HttpServletRequest httpServletRequest, String id) {

		StringBundler sb = new StringBundler(7);

		sb.append("com.liferay.dataset.active.view.settings");
		sb.append(StringPool.POUND);

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		String portletNamespace = _portal.getPortletNamespace(
			portletDisplay.getId());

		sb.append(portletNamespace);

		sb.append(StringPool.POUND);
		sb.append(themeDisplay.getPlid());
		sb.append(StringPool.POUND);
		sb.append(id);

		return sb.toString();
	}

	@Reference
	private Portal _portal;

}
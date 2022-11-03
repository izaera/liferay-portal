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

package com.liferay.client.extension.web.internal.display.context;

import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.type.CET;
import com.liferay.client.extension.web.internal.display.context.util.CETLabelUtil;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class AddClientExtensionEntryDisplayContext {

	public AddClientExtensionEntryDisplayContext(
		PortletRequest portletRequest) {

		_portletRequest = portletRequest;
	}

	public String getName() {
		return ParamUtil.getString(_portletRequest, "name");
	}

	public String getRedirect() {
		return ParamUtil.getString(_portletRequest, "redirect");
	}

	public String getTitle() {
		ThemeDisplay themeDisplay = _getThemeDisplay();

		return LanguageUtil.get(
			_getHttpServletRequest(),
			CETLabelUtil.getNewLabel(
				themeDisplay.getLocale(), getType()));
	}

	public String getType() {
		return ParamUtil.getString(_portletRequest, "type");
	}

	private HttpServletRequest _getHttpServletRequest() {
		return PortalUtil.getHttpServletRequest(_portletRequest);
	}

	private ThemeDisplay _getThemeDisplay() {
		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		return (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	private final PortletRequest _portletRequest;

}
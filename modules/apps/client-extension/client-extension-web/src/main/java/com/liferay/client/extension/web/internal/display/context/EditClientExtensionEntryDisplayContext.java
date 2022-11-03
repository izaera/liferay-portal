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
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.PortletRequest;

import javax.portlet.PortletURL;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class EditClientExtensionEntryDisplayContext {

	public EditClientExtensionEntryDisplayContext(
		CET cet, ClientExtensionEntry clientExtensionEntry,
		PortletRequest portletRequest) {

		_cet = cet;
		_clientExtensionEntry = clientExtensionEntry;
		_portletRequest = portletRequest;
	}

	public String getAddResourcesLabel() {
		return LanguageUtil.get(_getHttpServletRequest(), "add-resources");
	}

	public List<SelectOption> getAddResourcesSelectOptions() {
		return ListUtil.fromArray(
			new SelectOption(
				LanguageUtil.get(_getHttpServletRequest(), "from-url"),
					"fromURL"),
			new SelectOption(
				LanguageUtil.get(_getHttpServletRequest(), "from-computer"),
					"fromComputer"));
	}

	public String getCmd() {
		if (_clientExtensionEntry == null) {
			return Constants.ADD;
		}

		return Constants.UPDATE;
	}

	public String getDescription() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "description");
	}

	public long getClientExtensionEntryId() {
		return _clientExtensionEntry.getClientExtensionEntryId();
	}

	public String getEditJSP() {
		return _cet.getEditJSP();
	}

	public String getExternalReferenceCode() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "externalReferenceCode");
	}

	public Map<String, Object> getFrontendComponentContext() {
		String clientExtensionItemSelectorURL = PortletURLBuilder.create(
			_getRenderPortletURL()
		).setMVCRenderCommandName(
			"/client_extension_admin/client_extension_item_selector"
		).setParameter(
			"clientExtensionEntryId", getClientExtensionEntryId()
		).setWindowState(
			LiferayWindowState.POP_UP
		).buildString();


		return HashMapBuilder.<String, Object>put(
			"clientExtensionItemSelectorURL", clientExtensionItemSelectorURL
		).build();
	}

	public String getName() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "name");
	}

	public String getProperties() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "properties");
	}

	public String getRedirect() {
		return ParamUtil.getString(_portletRequest, "redirect");
	}

	public String getSourceCodeURL() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "sourceCodeURL");
	}

	public String getTitle() {
		ThemeDisplay themeDisplay = _getThemeDisplay();

		if (_clientExtensionEntry == null) {
			return LanguageUtil.get(
				_getHttpServletRequest(),
				CETLabelUtil.getNewLabel(
					themeDisplay.getLocale(), _cet.getType()));
		}

		return _cet.getName(themeDisplay.getLocale());
	}

	public String getType() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "type");
	}

	public String getTypeLabel() {
		ThemeDisplay themeDisplay = _getThemeDisplay();

		return LanguageUtil.get(
			_getHttpServletRequest(),
			CETLabelUtil.getTypeNameLabel(themeDisplay.getLocale(), getType()));
	}

	public boolean isPropertiesVisible() {
		return _cet.hasProperties();
	}

	private HttpServletRequest _getHttpServletRequest() {
		return PortalUtil.getHttpServletRequest(_portletRequest);
	}

	private ThemeDisplay _getThemeDisplay() {
		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		return (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	private PortletURL _getRenderPortletURL() {
		HttpServletRequest httpServletRequest =
			PortalUtil.getHttpServletRequest(_portletRequest);

		String portletId = _getPortletId(httpServletRequest);

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(httpServletRequest);

		return requestBackedPortletURLFactory.createRenderURL(portletId);
	}

	private String _getPortletId(HttpServletRequest httpServletRequest) {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		return portletDisplay.getId();
	}

	private final CET _cet;
	private final ClientExtensionEntry _clientExtensionEntry;
	private final PortletRequest _portletRequest;

}
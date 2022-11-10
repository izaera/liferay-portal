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
import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.display.context.util.CETLabelUtil;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.PortletRequest;

import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.WindowStateException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class EditClientExtensionEntryDisplayContext {

	public EditClientExtensionEntryDisplayContext(
		CET cet, ClientExtensionEntry clientExtensionEntry,
		ClientExtensionRepository clientExtensionRepository,
		PortletRequest portletRequest, PortletResponse portletResponse) {

		_cet = cet;
		_clientExtensionEntry = clientExtensionEntry;
		_clientExtensionRepository = clientExtensionRepository;
		_portletRequest = portletRequest;
		_portletResponse = portletResponse;
	}

	public String getAddResourcesLabel() {
		return LanguageUtil.get(_getHttpServletRequest(), "add-resources");
	}

	public List<SelectOption> getAddResourcesSelectOptions() {
		return ListUtil.fromArray(
			new SelectOption(
				LanguageUtil.get(_getHttpServletRequest(), "from-url"),
					"fromURL", !isInternalURLs()),
			new SelectOption(
				LanguageUtil.get(_getHttpServletRequest(), "from-computer"),
					"fromComputer", isInternalURLs()));
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

	public String getEditJSP() {
		return _cet.getEditJSP();
	}

	public String getExternalReferenceCode() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "externalReferenceCode");
	}

	public Map<String, Object> getFrontendComponentContext()
		throws WindowStateException {

		return HashMapBuilder.<String, Object>put(
			"clientExtensionItemSelectorURL",
				_getClientExtensionItemSelectorURL()
		).put(
			"getFileEntryURLURL", _getGetFileEntryURLURL()
		).put(
			"getManageResourcesSummaryURL",
				_getGetManageResourcesSummaryURL()
		).put(
			"itemSelectedEventName",
				_portletResponse.getNamespace() +
					ClientExtensionAdminPortletKeys.ITEM_SELECTED
		).build();
	}

	private String _getClientExtensionItemSelectorURL()
		throws WindowStateException {

		LiferayPortletURL liferayPortletURL =
			_getLiferayPortletURL(PortletRequest.RENDER_PHASE);

		liferayPortletURL.setParameter(
			"clientExtensionEntryId",
			String.valueOf(_clientExtensionEntry.getClientExtensionEntryId()));

		liferayPortletURL.setParameter(
			"mvcRenderCommandName",
			"/client_extension_admin/client_extension_item_selector");

		liferayPortletURL.setWindowState(LiferayWindowState.POP_UP);

		return liferayPortletURL.toString();
	}

	private String _getGetManageResourcesSummaryURL() {
		LiferayPortletURL liferayPortletURL =
			_getLiferayPortletURL(PortletRequest.RESOURCE_PHASE);

		liferayPortletURL.setResourceID(
			"/client_extension_admin/get_manage_resources_summary");

		liferayPortletURL.setParameter(
			"clientExtensionEntryId",
			String.valueOf(_clientExtensionEntry.getClientExtensionEntryId()));

		return liferayPortletURL.toString();
	}

	private String _getGetFileEntryURLURL() {
		LiferayPortletURL liferayPortletURL =
			_getLiferayPortletURL(PortletRequest.RESOURCE_PHASE);

		liferayPortletURL.setResourceID(
			"/client_extension_admin/get_file_entry_url");

		liferayPortletURL.setParameter(
			"fileEntryId", "FILE_ENTRY_ID");

		return liferayPortletURL.toString();
	}

	private LiferayPortletURL _getLiferayPortletURL(String lifecycle) {
		LiferayPortletResponse liferayPortletResponse =
			PortalUtil.getLiferayPortletResponse(_portletResponse);

		return liferayPortletResponse.createLiferayPortletURL(
				LayoutConstants.DEFAULT_PLID,
				ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
				lifecycle);
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

	public boolean isInternalURLs() {
		return BeanParamUtil.getBoolean(
			_clientExtensionEntry, _portletRequest, "internalURLs");
	}

	public boolean isPropertiesVisible() {
		return _cet.hasProperties();
	}

	public String getManageResourcesSummaryText() throws PortalException {
		return LanguageUtil.format(
			_getHttpServletRequest(), "x-folders-and-x-files-added",
			new Object[] {
				0,
				_clientExtensionRepository.getFileEntriesCount(
					_clientExtensionEntry.getClientExtensionEntryId(),
					ClientExtensionRepository.Status.ALL)});
	}

	private HttpServletRequest _getHttpServletRequest() {
		return PortalUtil.getHttpServletRequest(_portletRequest);
	}

	private ThemeDisplay _getThemeDisplay() {
		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		return (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	private final CET _cet;
	private final ClientExtensionEntry _clientExtensionEntry;
	private final ClientExtensionRepository _clientExtensionRepository;
	private final PortletRequest _portletRequest;
	private final PortletResponse _portletResponse;
}
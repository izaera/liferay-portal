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

import com.liferay.client.extension.constants.ClientExtensionEntryConstants;
import com.liferay.client.extension.exception.ClientExtensionEntryCustomElementCSSURLsException;
import com.liferay.client.extension.exception.ClientExtensionEntryCustomElementHTMLElementNameException;
import com.liferay.client.extension.exception.ClientExtensionEntryCustomElementURLsException;
import com.liferay.client.extension.exception.ClientExtensionEntryIFrameURLException;
import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.type.CETCustomElement;
import com.liferay.client.extension.type.CETIFrame;
import com.liferay.client.extension.type.factory.CETFactory;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SelectOption;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.bean.BeanParamUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.PortletCategory;
import com.liferay.portal.kernel.servlet.MultiSessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.WebAppPool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.portlet.PortletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class EditClientExtensionEntryDisplayContext {

	public EditClientExtensionEntryDisplayContext(
		CETFactory cetFactory, ClientExtensionEntry clientExtensionEntry,
		PortletRequest portletRequest) {

		_clientExtensionEntry = clientExtensionEntry;
		_portletRequest = portletRequest;

		CETCustomElement cetCustomElement = null;
		boolean customElementUseESM = false;
		boolean instanceable = false;
		String portletCategoryName = null;

		if ((clientExtensionEntry != null) &&
			Objects.equals(
				clientExtensionEntry.getType(),
				ClientExtensionEntryConstants.TYPE_CUSTOM_ELEMENT)) {

			cetCustomElement = cetFactory.customElement(clientExtensionEntry);

			customElementUseESM = cetCustomElement.isUseESM();
			instanceable = cetCustomElement.isInstanceable();
			portletCategoryName = cetCustomElement.getPortletCategoryName();
		}
		else if ((clientExtensionEntry != null) &&
				 Objects.equals(
					 clientExtensionEntry.getType(),
					 ClientExtensionEntryConstants.TYPE_IFRAME)) {

			CETIFrame cetIFrame = cetFactory.iFrame(clientExtensionEntry);

			instanceable = cetIFrame.isInstanceable();
			portletCategoryName = cetIFrame.getPortletCategoryName();
		}

		_cetCustomElement = cetCustomElement;
		_customElementUseESM = customElementUseESM;
		_instanceable = instanceable;

		if (Validator.isNull(portletCategoryName)) {
			portletCategoryName = "category.remote-apps";
		}

		_portletCategoryName = portletCategoryName;
	}

	public ClientExtensionEntry getClientExtensionEntry() {
		return _clientExtensionEntry;
	}

	public long getClientExtensionEntryId() {
		return BeanParamUtil.getLong(
			_clientExtensionEntry, _portletRequest, "clientExtensionEntryId");
	}

	public String getCmd() {
		if (_clientExtensionEntry == null) {
			return Constants.ADD;
		}

		return Constants.UPDATE;
	}

	public String[] getCustomElementCSSURLs() {
		String[] customElementCSSURLs = StringPool.EMPTY_ARRAY;

		if (_cetCustomElement != null) {
			String cssURLsString = _cetCustomElement.getCSSURLs();

			customElementCSSURLs = cssURLsString.split(StringPool.NEW_LINE);
		}

		customElementCSSURLs = ParamUtil.getStringValues(
			_portletRequest, "customElementCSSURLs", customElementCSSURLs);

		if (customElementCSSURLs.length == 0) {
			customElementCSSURLs = new String[1];
		}

		return customElementCSSURLs;
	}

	public String[] getCustomElementURLs() {
		String[] customElementURLs = StringPool.EMPTY_ARRAY;

		if (_cetCustomElement != null) {
			String urlsString = _cetCustomElement.getURLs();

			customElementURLs = urlsString.split(StringPool.NEW_LINE);
		}

		customElementURLs = ParamUtil.getStringValues(
			_portletRequest, "customElementURLs", customElementURLs);

		if (customElementURLs.length == 0) {
			customElementURLs = new String[1];
		}

		return customElementURLs;
	}

	public String getDescription() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "description");
	}

	public String getName() {
		return BeanParamUtil.getString(
			_clientExtensionEntry, _portletRequest, "name");
	}

	public List<SelectOption> getPortletCategoryNameSelectOptions()
		throws Exception {

		List<SelectOption> selectOptions = new ArrayList<>();

		ThemeDisplay themeDisplay = (ThemeDisplay)_portletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PortletCategory rootPortletCategory = (PortletCategory)WebAppPool.get(
			themeDisplay.getCompanyId(), WebKeys.PORTLET_CATEGORY);

		boolean found = false;

		for (PortletCategory portletCategory :
				rootPortletCategory.getCategories()) {

			selectOptions.add(
				new SelectOption(
					LanguageUtil.get(
						themeDisplay.getLocale(), portletCategory.getName()),
					portletCategory.getName(),
					_portletCategoryName.equals(portletCategory.getName())));

			if (Objects.equals(
					portletCategory.getName(), "category.remote-apps")) {

				found = true;
			}
		}

		if (!found) {
			selectOptions.add(
				new SelectOption(
					LanguageUtil.get(
						themeDisplay.getLocale(), "category.remote-apps"),
					"category.remote-apps",
					Objects.equals(
						_portletCategoryName, "category.remote-apps")));
		}

		return ListUtil.sort(
			selectOptions,
			new Comparator<SelectOption>() {

				@Override
				public int compare(
					SelectOption selectOption1, SelectOption selectOption2) {

					String label1 = selectOption1.getLabel();
					String label2 = selectOption2.getLabel();

					return label1.compareTo(label2);
				}

			});
	}

	public String getRedirect() {
		return ParamUtil.getString(_portletRequest, "redirect");
	}

	public String getTitle() {
		if (_clientExtensionEntry == null) {
			return LanguageUtil.get(_getHttpServletRequest(), "new-remote-app");
		}

		ThemeDisplay themeDisplay = _getThemeDisplay();

		return _clientExtensionEntry.getName(themeDisplay.getLocale());
	}

	public boolean isCustomElementUseESM() {
		return ParamUtil.getBoolean(
			_getHttpServletRequest(), "customElementUseESM",
			_customElementUseESM);
	}

	public boolean isEditingClientExtensionEntryType(String type) {
		return type.equals(_getClientExtensionEntryType());
	}

	public boolean isInstanceable() {
		return ParamUtil.getBoolean(
			_getHttpServletRequest(), "instanceable", _instanceable);
	}

	public boolean isInstanceableDisabled() {
		if (_clientExtensionEntry != null) {
			return true;
		}

		return false;
	}

	public boolean isTypeDisabled() {
		if (_clientExtensionEntry != null) {
			return true;
		}

		return false;
	}

	private String _getClientExtensionEntryType() {
		String errorSection = _getErrorSection();

		if (errorSection != null) {
			return errorSection;
		}

		if (_clientExtensionEntry == null) {
			return ClientExtensionEntryConstants.TYPE_IFRAME;
		}

		return _clientExtensionEntry.getType();
	}

	private String _getErrorSection() {
		if (MultiSessionErrors.contains(
				_portletRequest,
				ClientExtensionEntryIFrameURLException.class.getName())) {

			return ClientExtensionEntryConstants.TYPE_IFRAME;
		}

		if (MultiSessionErrors.contains(
				_portletRequest,
				ClientExtensionEntryCustomElementCSSURLsException.class.
					getName()) ||
			MultiSessionErrors.contains(
				_portletRequest,
				ClientExtensionEntryCustomElementHTMLElementNameException.class.
					getName()) ||
			MultiSessionErrors.contains(
				_portletRequest,
				ClientExtensionEntryCustomElementURLsException.class.
					getName())) {

			return ClientExtensionEntryConstants.TYPE_CUSTOM_ELEMENT;
		}

		return null;
	}

	private HttpServletRequest _getHttpServletRequest() {
		return PortalUtil.getHttpServletRequest(_portletRequest);
	}

	private ThemeDisplay _getThemeDisplay() {
		HttpServletRequest httpServletRequest = _getHttpServletRequest();

		return (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	private final CETCustomElement _cetCustomElement;
	private final ClientExtensionEntry _clientExtensionEntry;
	private final boolean _customElementUseESM;
	private final boolean _instanceable;
	private final String _portletCategoryName;
	private final PortletRequest _portletRequest;

}
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

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.item.selector.criterion.ClientExtensionItemSelectorCriterion;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.item.selector.ItemSelectorReturnTypeResolver;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.RepositoryEntry;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class ClientExtensionItemSelectorDisplayContext {

	public ClientExtensionItemSelectorDisplayContext(
		ClientExtensionItemSelectorCriterion
			clientExtensionItemSelectorCriterion,
		ClientExtensionRepository clientExtensionRepository,
		ItemSelectorView<ClientExtensionItemSelectorCriterion> itemSelectorView,
		String itemSelectedEventName,
		ItemSelectorReturnTypeResolverHandler
			itemSelectorReturnTypeResolverHandler,
		Language language, PortletURL portletURL) {

		_clientExtensionItemSelectorCriterion =
			clientExtensionItemSelectorCriterion;
		_clientExtensionRepository = clientExtensionRepository;
		_itemSelectorView = itemSelectorView;
		_itemSelectedEventName = itemSelectedEventName;
		_itemSelectorReturnTypeResolverHandler =
			itemSelectorReturnTypeResolverHandler;
		_language = language;
		_portletURL = portletURL;
	}

	public Set<String> getAllowedCreationMenuUIItemKeys() {
		return Collections.emptySet();
	}

	public String getEmptyResultsMessage(Locale locale) {
		return _language.get(locale, "there-are-no-client-extension-resources");
	}

	public String[] getExtensions() {
		return new String[] {".js", ".css"};
	}

	public String getItemSelectedEventName() {
		return _itemSelectedEventName;
	}

	public ItemSelectorReturnTypeResolver<?, ?>
	getItemSelectorReturnTypeResolver() {

		return _itemSelectorReturnTypeResolverHandler.
			getItemSelectorReturnTypeResolver(
				_clientExtensionItemSelectorCriterion, _itemSelectorView,
				FileEntry.class);
	}

	public long getMaxFileSize() {
		return Long.MAX_VALUE;
	}

	public String getMimeTypeRestriction() {
		return _clientExtensionItemSelectorCriterion.getMimeTypeRestriction();
	}

	public List<RepositoryEntry> getPortletFileEntries()
		throws PortalException {

		_fileEntries = _clientExtensionRepository.getFileEntries(
			_clientExtensionItemSelectorCriterion.getClientExtensionEntryId());

		return (List)_fileEntries;
	}

	public int getPortletFileEntriesCount() {
		return _fileEntries.size();
	}

	public PortletURL getPortletURL(
		HttpServletRequest httpServletRequest,
		LiferayPortletResponse liferayPortletResponse)
		throws PortletException {

		return PortletURLBuilder.create(
			PortletURLUtil.clone(_portletURL, liferayPortletResponse)
		).setParameter(
			"selectedTab", getTitle(httpServletRequest.getLocale())
		).buildPortletURL();
	}

	public String getTitle(Locale locale) {
		return _itemSelectorView.getTitle(locale);
	}

	public PortletURL getUploadURL(
		LiferayPortletResponse liferayPortletResponse) {

		return PortletURLBuilder.createActionURL(
			liferayPortletResponse,
			ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN
		).setActionName(
			"/client_extension_admin/upload_resource"
		).setParameter(
			"clientExtensionEntryId",
			_clientExtensionItemSelectorCriterion.getClientExtensionEntryId()
		).buildPortletURL();
	}

	private final ClientExtensionItemSelectorCriterion
		_clientExtensionItemSelectorCriterion;
	private final ClientExtensionRepository _clientExtensionRepository;
	private List<FileEntry> _fileEntries;
	private final String _itemSelectedEventName;
	private final ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;
	private final ItemSelectorView<ClientExtensionItemSelectorCriterion>
		_itemSelectorView;
	private final Language _language;
	private final PortletURL _portletURL;

}
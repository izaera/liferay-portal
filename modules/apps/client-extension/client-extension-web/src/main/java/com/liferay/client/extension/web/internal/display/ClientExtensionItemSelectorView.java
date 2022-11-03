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

package com.liferay.client.extension.web.internal.display;

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminWebKeys;
import com.liferay.client.extension.web.internal.display.context.ClientExtensionItemSelectorDisplayContext;
import com.liferay.client.extension.web.internal.item.selector.criterion.ClientExtensionItemSelectorCriterion;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.criteria.FileEntryItemSelectorReturnType;
import com.liferay.item.selector.criteria.URLItemSelectorReturnType;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.ListUtil;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletURL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	property = "item.selector.view.key=" + ClientExtensionAdminWebKeys.ITEM_SELECTOR_VIEW_KEY,
	service = ItemSelectorView.class
)
public class ClientExtensionItemSelectorView
	implements ItemSelectorView<ClientExtensionItemSelectorCriterion> {

	@Override
	public Class<? extends ClientExtensionItemSelectorCriterion>
	getItemSelectorCriterionClass() {

		return ClientExtensionItemSelectorCriterion.class;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return _language.get(locale, "client-extension-resources");
	}

	@Override
	public void renderHTML(
		ServletRequest servletRequest, ServletResponse servletResponse,
		ClientExtensionItemSelectorCriterion
			clientExtensionItemSelectorCriterion,
		PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		ClientExtensionItemSelectorDisplayContext
			clientExtensionItemSelectorDisplayContext =
			new ClientExtensionItemSelectorDisplayContext(
				clientExtensionItemSelectorCriterion,
				_clientExtensionRepository, this, itemSelectedEventName,
				_itemSelectorReturnTypeResolverHandler, _language,
				portletURL);

		servletRequest.setAttribute(
			ClientExtensionAdminWebKeys.
				CLIENT_EXTENSION_ITEM_SELECTOR_DISPLAY_CONTEXT,
			clientExtensionItemSelectorDisplayContext);

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher(
				"/admin/client_extension_item_selector.jsp");

		requestDispatcher.include(servletRequest, servletResponse);
	}

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.unmodifiableList(
		ListUtil.fromArray(
			new FileEntryItemSelectorReturnType(),
			new URLItemSelectorReturnType()));

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

	@Reference
	private ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;

	@Reference
	private Language _language;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.client.extension.web)"
	)
	private ServletContext _servletContext;

}
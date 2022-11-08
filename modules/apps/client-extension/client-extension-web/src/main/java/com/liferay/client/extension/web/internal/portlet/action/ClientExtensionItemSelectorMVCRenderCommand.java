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

package com.liferay.client.extension.web.internal.portlet.action;

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminWebKeys;
import com.liferay.client.extension.web.internal.display.context.ClientExtensionItemSelectorDisplayContext;
import com.liferay.client.extension.web.internal.item.selector.criterion.ClientExtensionItemSelectorCriterion;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.criteria.FileEntryItemSelectorReturnType;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
		"mvc.command.name=/client_extension_admin/client_extension_item_selector"
	},
	service = MVCRenderCommand.class
)
public class ClientExtensionItemSelectorMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		ClientExtensionItemSelectorCriterion
			clientExtensionItemSelectorCriterion =
				new ClientExtensionItemSelectorCriterion(
					ParamUtil.getLong(renderRequest, "clientExtensionEntryId"));

		clientExtensionItemSelectorCriterion.setDesiredItemSelectorReturnTypes(
			new FileEntryItemSelectorReturnType());

		renderRequest.setAttribute(
			ClientExtensionAdminWebKeys.
				CLIENT_EXTENSION_ITEM_SELECTOR_DISPLAY_CONTEXT,
			new ClientExtensionItemSelectorDisplayContext(
				clientExtensionItemSelectorCriterion,
				_clientExtensionRepository, _itemSelectorView,
				renderResponse.getNamespace() +
					ClientExtensionAdminPortletKeys.ITEM_SELECTED,
				_itemSelectorReturnTypeResolverHandler, _language,
				_itemSelector.getItemSelectorURL(
					RequestBackedPortletURLFactoryUtil.create(
						_portal.getHttpServletRequest(renderRequest)),
					ClientExtensionAdminPortletKeys.ITEM_SELECTED,
					clientExtensionItemSelectorCriterion)));

		return "/admin/client_extension_item_selector.jsp";
	}

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

	@Reference
	private ItemSelector _itemSelector;

	@Reference
	private ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;

	@Reference(
		service = ItemSelectorView.class,
		target = "(item.selector.view.key=" + ClientExtensionAdminWebKeys.ITEM_SELECTOR_VIEW_KEY + ")"
	)
	private ItemSelectorView<ClientExtensionItemSelectorCriterion>
		_itemSelectorView;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}
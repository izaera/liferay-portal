<%--
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
--%>

<%@ include file="/admin/init.jsp" %>

<%
ClientExtensionItemSelectorDisplayContext clientExtensionItemSelectorDisplayContext = (ClientExtensionItemSelectorDisplayContext)renderRequest.getAttribute(ClientExtensionAdminWebKeys.CLIENT_EXTENSION_ITEM_SELECTOR_DISPLAY_CONTEXT);
%>

<liferay-item-selector:repository-entry-browser
	displayStyle="table"
	showSearch="true"
	allowedCreationMenuUIItemKeys="<%= clientExtensionItemSelectorDisplayContext.getAllowedCreationMenuUIItemKeys() %>"
	emptyResultsMessage="<%= clientExtensionItemSelectorDisplayContext.getEmptyResultsMessage(locale) %>"
	extensions="<%= ListUtil.fromArray(clientExtensionItemSelectorDisplayContext.getExtensions()) %>"
	itemSelectedEventName="<%= clientExtensionItemSelectorDisplayContext.getItemSelectedEventName() %>"
	itemSelectorReturnTypeResolver="<%= clientExtensionItemSelectorDisplayContext.getItemSelectorReturnTypeResolver() %>"
	maxFileSize="<%= clientExtensionItemSelectorDisplayContext.getMaxFileSize() %>"
	mimeTypeRestriction="<%= clientExtensionItemSelectorDisplayContext.getMimeTypeRestriction() %>"
	portletURL="<%= clientExtensionItemSelectorDisplayContext.getPortletURL(request, liferayPortletResponse) %>"
	repositoryEntries="<%= clientExtensionItemSelectorDisplayContext.getPortletFileEntries() %>"
	repositoryEntriesCount="<%= clientExtensionItemSelectorDisplayContext.getPortletFileEntriesCount() %>"
	showDragAndDropZone="<%= true %>"
	tabName="<%= clientExtensionItemSelectorDisplayContext.getTitle(locale) %>"
	uploadURL="<%= clientExtensionItemSelectorDisplayContext.getUploadURL(liferayPortletResponse) %>"
/>

<liferay-frontend:component module="admin/js/clientExtensionItemSelector" />
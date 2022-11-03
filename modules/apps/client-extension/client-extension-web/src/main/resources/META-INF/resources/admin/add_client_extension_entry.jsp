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
AddClientExtensionEntryDisplayContext addClientExtensionEntryDisplayContext = (AddClientExtensionEntryDisplayContext)renderRequest.getAttribute(ClientExtensionAdminWebKeys.ADD_CLIENT_EXTENSION_DISPLAY_CONTEXT);

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(addClientExtensionEntryDisplayContext.getRedirect());

renderResponse.setTitle(addClientExtensionEntryDisplayContext.getTitle());
%>

<portlet:resourceURL id="/client_extension_admin/add_client_extension_entry" var="addClientExtensionEntryURL" />

<commerce-ui:modal-content
	submitButtonLabel="submit"
	title='<%= addClientExtensionEntryDisplayContext.getTitle() %>'
>
	<liferay-frontend:edit-form method="post" name="fm">
		<liferay-frontend:edit-form-body>
			<aui:input name="type" type="hidden" value="<%= addClientExtensionEntryDisplayContext.getType() %>" />

			<liferay-frontend:fieldset-group>
				<aui:field-wrapper label="name" name="name">
					<liferay-ui:input-localized
						autoFocus="<%= windowState.equals(WindowState.MAXIMIZED) %>"
						name="name"
						xml="<%= addClientExtensionEntryDisplayContext.getName() %>"
					/>
				</aui:field-wrapper>
			</liferay-frontend:fieldset-group>
		</liferay-frontend:edit-form-body>
	</liferay-frontend:edit-form>
</commerce-ui:modal-content>

<liferay-frontend:component
	context='<%=
			HashMapBuilder.<String, Object>put(
				"addClientExtensionEntryURL", String.valueOf(addClientExtensionEntryURL)
			).put(
				"modalId", "addClientExtensionEntry"
			).build()
		%>'
	module="admin/js/addClientExtensionEntry"
/>

<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/admin/init.jsp" %>

<%
EditClientExtensionEntryDisplayContext<PersonalizationCET> editClientExtensionEntryDisplayContext = (EditClientExtensionEntryDisplayContext)renderRequest.getAttribute(ClientExtensionAdminWebKeys.EDIT_CLIENT_EXTENSION_ENTRY_DISPLAY_CONTEXT);

PersonalizationCET personalizationCET = editClientExtensionEntryDisplayContext.getCET();
%>

<aui:field-wrapper cssClass="form-group">
	<aui:input ignoreRequestValue="<%= true %>" label="rules-url" name="rulesURL" required="<%= true %>" type="text" value="<%= personalizationCET.getRulesURL() %>" />

	<div class="form-text">
		<liferay-ui:message key="specify-the-url-of-the-personalization-rules-json-file" />
	</div>
</aui:field-wrapper>

<aui:field-wrapper cssClass="form-group">
	<aui:input ignoreRequestValue="<%= true %>" label="javascript" name="javaScript" type="textarea" value="<%= personalizationCET.getJavaScript() %>" />

	<div class="form-text">
		<liferay-ui:message key="paste-the-javascript-code-to-run-after-personalization-detection" />
	</div>
</aui:field-wrapper>
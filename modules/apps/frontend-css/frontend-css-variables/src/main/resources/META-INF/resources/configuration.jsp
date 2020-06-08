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

<%@ include file="/init.jsp" %>

<%
Map<String, String> properties = (Map<String, String>)request.getAttribute(CssVariablesWebKeys.PROPERTIES);
%>

<portlet:actionURL name="/css_variables/edit" var="editCssVariablesURL" />

<clay:sheet
	cssClass="css-variables-configuration"
>
	<h2>
		<liferay-ui:message key="css-variables" />
	</h2>

	<aui:form action="<%= editCssVariablesURL %>" method="post" name="fm">
		<aui:input name="redirect" type="hidden" value="<%= currentURL %>" />

		<fieldset>
			<div class="form-text">
				<liferay-ui:message key="css-variables-help" />
			</div>

			<%
			for (Map.Entry<String, String> entry : properties.entrySet()) {
			%>

				<aui:input name="<%= entry.getKey() %>" type="text" value="<%= entry.getValue() %>" />

			<%
			}
			%>

		</fieldset>

		<aui:button-row>
			<aui:button type="submit" value="save" />
		</aui:button-row>
	</aui:form>
</clay:sheet>
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
String eventName = HtmlUtil.escapeJS(ParamUtil.getString(request, "eventName", liferayPortletResponse.getNamespace() + "addDomains"));
%>

<liferay-frontend:edit-form
	action="javascript:void(0);"
	onSubmit='<%= liferayPortletResponse.getNamespace() + "addDomains('" + eventName + "');" %>'
>
	<div class="modal-body">
		<div class="hide" id="<portlet:namespace />domainAlert">
			<clay:alert
				displayType="danger"
				message="please-enter-valid-mail-domains-separated-by-commas"
			/>
		</div>

		<aui:field-wrapper cssClass="form-group">
			<aui:input label="domain" name="domain" />

			<div class="form-text">
				<liferay-ui:message key="for-multiple-domains,-separate-each-domain-by-a-comma" />
			</div>
		</aui:field-wrapper>

		<aui:button-row>
			<aui:button type="submit" value="save" />

			<aui:button type="cancel" />
		</aui:button-row>
	</div>
</liferay-frontend:edit-form>
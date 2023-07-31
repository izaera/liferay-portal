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

<%@ include file="/dynamic_include/init.jsp" %>

<aui:script async="<%= true %>" src="//code.jivosite.com/widget/<%= clickToChatChatProviderAccountId %>"></aui:script>

<c:if test="<%= themeDisplay.isSignedIn() %>">
	<aui:script position="inline">
		function jivo_onOpen() {
			jivo_api.setContactInfo({
				email: '<%= user.getEmailAddress() %>',
				name: '<%= user.getScreenName() %>',
			});
		}
	</aui:script>
</c:if>
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

<aui:script position="inline">
	var _smartsupp = _smartsupp || {};

	_smartsupp.key = '<%= clickToChatChatProviderAccountId %>';

	window.smartsupp ||
		(function (d) {
			var c,
				s,
				o = (smartsupp = function () {
					o._.push(arguments);
				});

			o._ = [];

			c = d.createElement('script');

			c.async = true;
			c.charset = 'utf-8';
			c.src = 'https://www.smartsuppchat.com/loader.js?';
			c.type = 'text/javascript';

			s = d.getElementsByTagName('script')[0];

			s.parentNode.insertBefore(c, s);
		})(document);

	<c:if test="<%= themeDisplay.isSignedIn() %>">
		smartsupp('email', '<%= user.getEmailAddress() %>');
		smartsupp('name', '<%= user.getFirstName() %>');
	</c:if>
</aui:script>
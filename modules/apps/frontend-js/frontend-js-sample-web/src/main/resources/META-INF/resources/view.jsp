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

<%@ taglib uri="http://liferay.com/tld/react" prefix="react" %>

<div class="row">
	<div class="col">
		<react:component
			module="js/App"
		/>
	</div>

	<div class="col">
		<button id="test-button-jsp">Increment jsp</button>

		<h3>JSP Counter: <span id="test-counter-jsp">0</span></h3>

		<h3>Name: <span id="test-name">Initial Name</span></h3>

		<button id="test-button-react">Increment react</button>

		<aui:script require="<%= npmResolvedPackageName %>" />
	</div>
</div>
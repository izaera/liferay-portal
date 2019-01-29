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

<blockquote><p>Multi select is a drop-down list that allows multiple selections.</p></blockquote>

<h3>Liferay Multi Select Library</h3>

<%
String headerCSRFToken = "X-CSRF-Token";
String authToken = "IarWYUxT";

HashMap<String, Object> requestOptions = new HashMap<>();
requestOptions.put("credentials", "include");
requestOptions.put(
	"headers",
	new HashMap<String, Object>() {
		{
			put(headerCSRFToken, authToken);
		}
	});

String locator = "name";
String jsonWebServiceLiferay = "http://localhost:8080/api/jsonws/assetvocabulary/get-group-vocabularies/group-id/20126";
String anotherUrl = "https://jsonplaceholder.typicode.com/users";
%>

<clay:multi-select
	dataSource="<%= anotherUrl %>"
	helpText="Amazing help text"
	labelLocator="<%= locator %>"
	valueLocator="<%= locator %>"
/>

<clay:multi-select
	dataSource="<%= anotherUrl %>"
	helpText="Amazing help text"
	labelLocator="<%= locator %>"
	valueLocator="<%= locator %>"
/>
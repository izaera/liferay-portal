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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<%@ page import="com.liferay.frontend.taglib.clay.data.set.model.ClayPaginationEntry" %><%@
page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu" %><%@
page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem" %><%@
page import="com.liferay.frontend.taglib.clay.servlet.taglib.util.SortItemList" %><%@
page import="com.liferay.petra.string.StringPool" %><%@
page import="com.liferay.portal.kernel.json.JSONFactoryUtil" %><%@
page import="com.liferay.portal.kernel.json.JSONSerializer" %><%@
page import="com.liferay.portal.kernel.util.GetterUtil" %><%@
page import="com.liferay.portal.kernel.util.PortalUtil" %><%@
page import="com.liferay.portal.kernel.util.Validator" %>

<%@ page import="java.util.List" %>

<%@ page import="javax.portlet.PortletURL" %>

<liferay-theme:defineObjects />

<%
String actionParameterName = (String)request.getAttribute("dataset:dataset:actionParameterName");
String activeViewSettingsJSON = GetterUtil.getString(request.getAttribute("dataset:dataset:activeViewSettingsJSON"), "{}");
String apiURL = (String)request.getAttribute("dataset:dataset:apiURL");
String appURL = (String)request.getAttribute("dataset:dataset:appURL");
List<DropdownItem> bulkActionDropdownItems = (List<DropdownItem>)request.getAttribute("dataset:dataset:bulkActionDropdownItems");
Object datasetViewsContext = request.getAttribute("dataset:dataset:datasetViewsContext");
CreationMenu creationMenu = (CreationMenu)request.getAttribute("dataset:dataset:creationMenu");
String dataProviderKey = (String)request.getAttribute("dataset:dataset:dataProviderKey");
String formId = (String)request.getAttribute("dataset:dataset:formId");
String id = (String)request.getAttribute("dataset:dataset:id");
int itemsPerPage = (int)request.getAttribute("dataset:dataset:itemsPerPage");
String module = (String)request.getAttribute("dataset:dataset:module");
String namespace = (String)request.getAttribute("dataset:dataset:namespace");
String nestedItemsKey = (String)request.getAttribute("dataset:dataset:nestedItemsKey");
String nestedItemsReferenceKey = (String)request.getAttribute("dataset:dataset:nestedItemsReferenceKey");
int pageNumber = (int)request.getAttribute("dataset:dataset:pageNumber");
List<ClayPaginationEntry> clayPaginationEntries = (List<ClayPaginationEntry>)request.getAttribute("dataset:dataset:clayPaginationEntries");
PortletURL portletURL = (PortletURL)request.getAttribute("dataset:dataset:portletURL");
List<String> selectedItems = (List<String>)request.getAttribute("dataset:dataset:selectedItems");
String selectedItemsKey = (String)request.getAttribute("dataset:dataset:selectedItemsKey");
String selectionType = (String)request.getAttribute("dataset:dataset:selectionType");
boolean showManagementBar = (boolean)request.getAttribute("dataset:dataset:showManagementBar");
boolean showPagination = (boolean)request.getAttribute("dataset:dataset:showPagination");
boolean showSearch = (boolean)request.getAttribute("dataset:dataset:showSearch");
SortItemList sortItemList = (SortItemList)request.getAttribute("dataset:headless-dataset:sortItemList");
String style = (String)request.getAttribute("dataset:dataset:style");

JSONSerializer jsonSerializer = JSONFactoryUtil.createJSONSerializer();

String randomNamespace = PortalUtil.generateRandomKey(request, "taglib_step_tracker") + StringPool.UNDERLINE;

String containerId = randomNamespace + "table-id";
%>
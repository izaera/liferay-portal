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

<%@ page import="com.liferay.dataset.taglib.servlet.taglib.util.DatasetActionDropdownItem" %><%@
page import="com.liferay.frontend.taglib.clay.data.set.model.ClayPaginationEntry" %><%@
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
String actionParameterName = (String)request.getAttribute("dataset:headless-dataset:actionParameterName");
String activeViewSettingsJSON = GetterUtil.getString(request.getAttribute("dataset:headless-dataset:activeViewSettingsJSON"), "{}");
String apiURL = (String)request.getAttribute("dataset:headless-dataset:apiURL");
String appURL = (String)request.getAttribute("dataset:headless-dataset:appURL");
List<DropdownItem> bulkActionDropdownItems = (List<DropdownItem>)request.getAttribute("dataset:headless-dataset:bulkActionDropdownItems");
Object datasetViewsContext = request.getAttribute("dataset:headless-dataset:datasetViewsContext");
Object datasetFiltersContext = request.getAttribute("dataset:headless-dataset:datasetFiltersContext");
List<DatasetActionDropdownItem> datasetActionDropdownItems = (List<DatasetActionDropdownItem>)request.getAttribute("dataset:headless-dataset:datasetActionDropdownItems");
List<ClayPaginationEntry> clayPaginationEntries = (List<ClayPaginationEntry>)request.getAttribute("dataset:headless-dataset:clayPaginationEntries");
CreationMenu creationMenu = (CreationMenu)request.getAttribute("dataset:headless-dataset:creationMenu");
String formId = (String)request.getAttribute("dataset:headless-dataset:formId");
String id = (String)request.getAttribute("dataset:headless-dataset:id");
int itemsPerPage = (int)request.getAttribute("dataset:headless-dataset:itemsPerPage");
String module = (String)request.getAttribute("dataset:headless-dataset:module");
String namespace = (String)request.getAttribute("dataset:headless-dataset:namespace");
String nestedItemsKey = (String)request.getAttribute("dataset:headless-dataset:nestedItemsKey");
String nestedItemsReferenceKey = (String)request.getAttribute("dataset:headless-dataset:nestedItemsReferenceKey");
int pageNumber = (int)request.getAttribute("dataset:headless-dataset:pageNumber");
PortletURL portletURL = (PortletURL)request.getAttribute("dataset:headless-dataset:portletURL");
List<String> selectedItems = (List<String>)request.getAttribute("dataset:headless-dataset:selectedItems");
String selectedItemsKey = (String)request.getAttribute("dataset:headless-dataset:selectedItemsKey");
String selectionType = (String)request.getAttribute("dataset:headless-dataset:selectionType");
boolean showManagementBar = (boolean)request.getAttribute("dataset:headless-dataset:showManagementBar");
boolean showPagination = (boolean)request.getAttribute("dataset:headless-dataset:showPagination");
boolean showSearch = (boolean)request.getAttribute("dataset:headless-dataset:showSearch");
SortItemList sortItemList = (SortItemList)request.getAttribute("dataset:headless-dataset:sortItemList");
String style = (String)request.getAttribute("dataset:headless-dataset:style");
%>
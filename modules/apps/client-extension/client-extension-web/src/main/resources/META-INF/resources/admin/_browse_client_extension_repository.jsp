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

<%@ page import="com.liferay.portal.kernel.repository.model.FileEntry" %>
<%@ page import="java.util.List" %>
<%@ page import="com.liferay.document.library.kernel.model.DLFolder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.liferay.portal.kernel.repository.model.Folder" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Objects" %>

<%
List<FileEntry> fileEntries = (List)renderRequest.getAttribute("fileEntries");
List<DLFolder> folders = (List)renderRequest.getAttribute("folders");
String title = (String)renderRequest.getAttribute("title");

String baseURL = "http://localhost:8080/group/control_panel/manage?p_p_id=com_liferay_client_extension_web_internal_portlet_ClientExtensionAdminPortlet&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_com_liferay_client_extension_web_internal_portlet_ClientExtensionAdminPortlet_mvcRenderCommandName=%2Fclient_extension_admin%2F_browse_client_extension_repository";
%>

<h2><%= title %></h2>

<hr>

<%
	for (DLFolder folder : folders) {
%>
		<a href="<%= baseURL %>&_com_liferay_client_extension_web_internal_portlet_ClientExtensionAdminPortlet_folderId=<%= folder.getFolderId() %>"
			style="display: block;"
		>
			[<%= folder.getName() %>]
		</a>
<%
	}

	for (FileEntry fileEntry : fileEntries) {
		List<String> parts = new ArrayList<>();

		parts.add(fileEntry.getFileName());

		for(Folder folder = fileEntry.getFolder();
				!Objects.equals(
					folder.getName(),
					"com_liferay_client_extension_web_internal_portlet_ClientExtensionAdminPortlet");
				folder = folder.getParentFolder()) {

			parts.add(folder.getName());
		}

		String draft = "";

		String clientExtensionEntryId = parts.remove(parts.size() - 1);

		if (clientExtensionEntryId.contains(".tmp")) {
			clientExtensionEntryId = clientExtensionEntryId.replace(".tmp", "");
			draft = "?draft=true";
		}

		Collections.reverse(parts);
%>
		<a target="_blank" href="/o/cet-asset/<%= clientExtensionEntryId %>/<%= String.join("/", parts) %><%= draft %>"
			style="display: block;"
		>
			<%= fileEntry.getFileName() %>
		</a>
<%
	}
%>
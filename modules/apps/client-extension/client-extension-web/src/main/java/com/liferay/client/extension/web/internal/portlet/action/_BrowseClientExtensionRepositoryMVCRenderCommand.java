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

package com.liferay.client.extension.web.internal.portlet.action;

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.util.ParamUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.List;

/**
 * @author Iván Zaera
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
		"mvc.command.name=/client_extension_admin/_browse_client_extension_repository"
	},
	service = MVCRenderCommand.class
)
public class _BrowseClientExtensionRepositoryMVCRenderCommand
	implements MVCRenderCommand {

	@Override
	public String render(
			RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		try {
			long folderId = ParamUtil.getLong(renderRequest, "folderId");

			Folder folder = _clientExtensionRepository._getFolder(folderId);

			List<FileEntry> fileEntries =
				_clientExtensionRepository._getFileEntries(folderId);

			List<DLFolder> folders =
				_clientExtensionRepository._getFolders(folderId);

			renderRequest.setAttribute("fileEntries", fileEntries);
			renderRequest.setAttribute("folders", folders);
			renderRequest.setAttribute("title", "[[" + folder.getName() + "]]");

			return "/admin/_browse_client_extension_repository.jsp";
		}
		catch (Exception exception) {
			throw new PortletException(exception);
		}
	}

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

}
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

package com.liferay.client.extension.web.internal.servlet;

import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.util.StreamUtil;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.servlet.name=Serve Client Extension Asset Servlet",
		"osgi.http.whiteboard.servlet.pattern=/cet-asset/*",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 1000)
	},
	service = Servlet.class
)
public class ClientExtensionAssetServlet extends HttpServlet {

	@Override
	protected void service(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		try {
			FileEntry fileEntry = _clientExtensionRepository.getFileEntry(
				httpServletRequest.getPathInfo());

			httpServletResponse.setContentType(fileEntry.getMimeType());

			StreamUtil.transfer(
				fileEntry.getContentStream(),
				httpServletResponse.getOutputStream());
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}

			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClientExtensionAssetServlet.class);

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

}
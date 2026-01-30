/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.resource.handler;

import com.liferay.frontend.js.web.internal.configuration.FrontendCachingConfiguration;
import com.liferay.frontend.js.web.internal.resource.FrontendResource;
import com.liferay.frontend.js.web.internal.resource.VirtualModuleFrontendResource;
import com.liferay.frontend.js.web.internal.util.FrontendJsWebUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesUtil;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.net.URL;

import java.util.Objects;

/**
 * @author Iván Zaera Avellón
 */
public class VirtualModuleFrontendResourceRequestHandler
	implements FrontendResourceRequestHandler {

	public static final String VIRTUAL_MODULE_URI_PREFIX = "/o/js/-/";

	public VirtualModuleFrontendResourceRequestHandler(
		ConfigurationProvider configurationProvider,
		HashedFilesRegistry hashedFilesRegistry, Portal portal) {

		_configurationProvider = configurationProvider;
		_hashedFilesRegistry = hashedFilesRegistry;
		_portal = portal;
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		return requestURI.startsWith(
			FrontendJsWebUtil.getPortalContextPath(_portal) +
				VIRTUAL_MODULE_URI_PREFIX);
	}

	@Override
	public FrontendResource handleRequest(HttpServletRequest httpServletRequest)
		throws IOException, ServletException {

		String requestURI = httpServletRequest.getRequestURI();

		String portalContextPath = FrontendJsWebUtil.getPortalContextPath(
			_portal);

		requestURI = requestURI.substring(
			portalContextPath.length() + VIRTUAL_MODULE_URI_PREFIX.length());

		int openParethensisIndex = requestURI.indexOf(
			StringPool.OPEN_PARENTHESIS);

		if (openParethensisIndex == -1) {
			return null;
		}

		int closeParethensisIndex = requestURI.indexOf(
			StringPool.CLOSE_PARENTHESIS);

		if (closeParethensisIndex == -1) {
			return null;
		}

		String requestHash = requestURI.substring(
			openParethensisIndex + 1, closeParethensisIndex);

		String servletContextName = requestURI.substring(
			0, openParethensisIndex);

		if (!Objects.equals(
				requestHash,
				_hashedFilesRegistry.getServletContextHash(
					servletContextName))) {

			return null;
		}

		String resourceURI = StringBundler.concat(
			portalContextPath, Portal.PATH_MODULE, StringPool.SLASH,
			servletContextName,
			requestURI.substring(closeParethensisIndex + 1));

		URL url = _hashedFilesRegistry.getResource(resourceURI);

		if (url == null) {
			return null;
		}

		boolean immutable = false;

		if (HashedFilesUtil.getHash(requestURI) != null) {
			immutable = true;
		}

		FrontendCachingConfiguration frontendCachingConfiguration =
			FrontendJsWebUtil.getFrontendCachingConfiguration(
				_portal.getCompanyId(httpServletRequest),
				_configurationProvider);

		long maxAge = frontendCachingConfiguration.jsFilesMaxAge();
		boolean sendNoCache =
			frontendCachingConfiguration.sendNoCacheForJSFiles();

		if (immutable) {
			maxAge = 31536000;
			sendNoCache = false;
		}

		return new VirtualModuleFrontendResource(
			HashedFilesUtil.getHash(url.getFile()), immutable, maxAge,
			sendNoCache, url);
	}

	private final ConfigurationProvider _configurationProvider;
	private final HashedFilesRegistry _hashedFilesRegistry;
	private final Portal _portal;

}
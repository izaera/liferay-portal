/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.frontend.resource.handler;

import com.liferay.frontend.js.web.internal.frontend.resource.FrontendResource;
import com.liferay.frontend.js.web.internal.frontend.resource.ThemeFrontendResource;
import com.liferay.frontend.js.web.internal.hashed.files.HashedFilesRegistry;
import com.liferay.frontend.js.web.internal.hashed.files.HashedFilesUtil;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.net.URL;

import java.util.Arrays;
import java.util.List;

/**
 * @author Iván Zaera Avellón
 */
public class ThemeFrontendResourceRequestHandler
	implements FrontendResourceRequestHandler {

	public ThemeFrontendResourceRequestHandler(
		HashedFilesRegistry hashedFilesRegistry,
		ServiceTrackerMap<String, ServletContext> serviceTrackerMap) {

		_hashedFilesRegistry = hashedFilesRegistry;
		_serviceTrackerMap = serviceTrackerMap;
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		if (requestURI.endsWith(".css") &&
			HashedFilesUtil.containsHash(requestURI) &&
			(_hashedFilesRegistry.get(HashedFilesUtil.removeHash(requestURI)) !=
				null)) {

			return true;
		}

		return false;
	}

	@Override
	public FrontendResource handleRequest(HttpServletRequest httpServletRequest)
		throws IOException, ServletException {

		String requestURI = httpServletRequest.getRequestURI();

		List<String> requestURIParts = Arrays.asList(
			requestURI.split(StringPool.SLASH));

		ServletContext servletContext = _serviceTrackerMap.getService(
			StringUtil.merge(requestURIParts.subList(0, 3), StringPool.SLASH));

		if (servletContext == null) {
			return null;
		}

		String resourcePath = StringUtil.merge(
			requestURIParts.subList(3, requestURIParts.size()),
			StringPool.SLASH);

		URL url = servletContext.getResource(resourcePath);

		if (url == null) {
			return null;
		}

		return new ThemeFrontendResource(
			HashedFilesUtil.getHash(requestURI), url);
	}

	private final HashedFilesRegistry _hashedFilesRegistry;
	private final ServiceTrackerMap<String, ServletContext> _serviceTrackerMap;

}
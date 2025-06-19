/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.frontend.resource.handler;

import com.liferay.frontend.js.web.internal.configuration.FrontendCachingConfiguration;
import com.liferay.frontend.js.web.internal.frontend.resource.FrontendResource;
import com.liferay.frontend.js.web.internal.frontend.resource.TokenizedCSSFrontendResource;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.hashed.files.HashedFilesUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.net.URL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class TokenizedCSSFrontendResourceRequestHandler
	implements FrontendResourceRequestHandler {

	public TokenizedCSSFrontendResourceRequestHandler(
		ConfigurationProvider configurationProvider,
		HashedFilesRegistry hashedFilesRegistry, Portal portal,
		ServiceTrackerMap<String, ServletContext> serviceTrackerMap,
		ThemeLocalService themeLocalService) {

		_configurationProvider = configurationProvider;
		_hashedFilesRegistry = hashedFilesRegistry;
		_portal = portal;
		_serviceTrackerMap = serviceTrackerMap;
		_themeLocalService = themeLocalService;
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		if (!requestURI.endsWith(".css")) {
			return false;
		}

		String hashedFileURI;

		if (HashedFilesUtil.containsHash(requestURI)) {
			hashedFileURI = _hashedFilesRegistry.get(
				HashedFilesUtil.removeHash(requestURI));
		}
		else {
			hashedFileURI = _hashedFilesRegistry.get(requestURI);
		}

		if (hashedFileURI == null) {
			return false;
		}

		return true;
	}

	@Override
	public FrontendResource handleRequest(HttpServletRequest httpServletRequest)
		throws IOException, ServletException {

		String requestURI = httpServletRequest.getRequestURI();

		String hash = HashedFilesUtil.getHash(requestURI);

		if (hash != null) {
			return _createFrontendResource(
				httpServletRequest, hash, true, 31536000, requestURI, false);
		}

		long maxAge = 86400;
		boolean sendNoCache = false;

		long companyId = _portal.getCompanyId(httpServletRequest);

		try {
			FrontendCachingConfiguration frontendCachingConfiguration =
				_configurationProvider.getCompanyConfiguration(
					FrontendCachingConfiguration.class, companyId);

			maxAge = frontendCachingConfiguration.cssStyleSheetsMaxAge();
			sendNoCache =
				frontendCachingConfiguration.sendNoCacheForCSSStyleSheets();
		}
		catch (ConfigurationException configurationException) {
			_log.error(
				"Unable to get frontend caching configuration: will use " +
					"reasonable defaults instead",
				configurationException);
		}

		String hashedFileURI = _hashedFilesRegistry.get(requestURI);

		if (hashedFileURI == null) {
			return _createFrontendResource(
				httpServletRequest, null, false, maxAge, requestURI,
				sendNoCache);
		}

		return _createFrontendResource(
			httpServletRequest, HashedFilesUtil.getHash(hashedFileURI), false,
			maxAge, hashedFileURI, sendNoCache);
	}

	private FrontendResource _createFrontendResource(
			HttpServletRequest httpServletRequest, String eTag,
			boolean immutable, long maxAge, String resourceURI,
			boolean sendNoCache)
		throws IOException {

		List<String> resourceURIParts = Arrays.asList(
			resourceURI.split(StringPool.SLASH));

		ServletContext servletContext = _serviceTrackerMap.getService(
			StringUtil.merge(resourceURIParts.subList(0, 3), StringPool.SLASH));

		if (servletContext == null) {
			return null;
		}

		String resourcePath = StringUtil.merge(
			resourceURIParts.subList(3, resourceURIParts.size()),
			StringPool.SLASH);

		resourcePath = StringPool.SLASH + resourcePath;

		URL url = servletContext.getResource(resourcePath);

		if (url == null) {
			return null;
		}

		return new TokenizedCSSFrontendResource(
			eTag, immutable, maxAge, sendNoCache,
			_getTokens(httpServletRequest, servletContext), url);
	}

	private Map<String, String> _getTokens(
			HttpServletRequest httpServletRequest,
			ServletContext servletContext)
		throws IOException {

		try {
			Map<String, String> tokens = HashMapBuilder.put(
				"@base_url@",
				_portal.getPathProxy() + servletContext.getContextPath()
			).put(
				"@portal_ctx@", _portal.getPathContext()
			).build();

			String themeId = httpServletRequest.getParameter("themeId");

			if (Validator.isNotNull(themeId)) {
				String cdnHost = _portal.getCDNHost(httpServletRequest);

				Theme theme = _themeLocalService.getTheme(
					_portal.getCompanyId(httpServletRequest), themeId);

				String themeStaticResourcePath = theme.getStaticResourcePath();

				tokens.put(
					"@theme_image_path@",
					cdnHost + themeStaticResourcePath + theme.getImagesPath());
			}

			return tokens;
		}
		catch (PortalException portalException) {
			throw new IOException(portalException);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		TokenizedCSSFrontendResourceRequestHandler.class);

	private final ConfigurationProvider _configurationProvider;
	private final HashedFilesRegistry _hashedFilesRegistry;
	private final Portal _portal;
	private final ServiceTrackerMap<String, ServletContext> _serviceTrackerMap;
	private final ThemeLocalService _themeLocalService;

}
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.resource.handler;

import com.liferay.frontend.js.web.internal.configuration.FrontendCachingConfiguration;
import com.liferay.frontend.js.web.internal.resource.FrontendResource;
import com.liferay.frontend.js.web.internal.resource.JavaScriptFrontendResource;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletConfigFactory;
import com.liferay.portal.kernel.util.AggregateResourceBundle;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.language.LanguageResources;

import jakarta.portlet.PortletConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.net.URL;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Iván Zaera Avellón
 */
public class JavaScriptFrontendResourceRequestHandler
	implements FrontendResourceRequestHandler {

	public JavaScriptFrontendResourceRequestHandler(
		FrontendCachingConfiguration frontendCachingConfiguration,
		HashedFilesRegistry hashedFilesRegistry, Language language,
		PortletConfigFactory portletConfigFactory) {

		_frontendCachingConfiguration = frontendCachingConfiguration;
		_hashedFilesRegistry = hashedFilesRegistry;
		_language = language;
		_portletConfigFactory = portletConfigFactory;
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();

		if (!requestURI.endsWith(".js")) {
			return false;
		}

		if (HashedFilesUtil.containsHash(requestURI)) {
			return true;
		}

		String hashedFileURI = _hashedFilesRegistry.getHashedFileURI(
			requestURI);

		if (hashedFileURI != null) {
			return true;
		}

		URL resourceURL = _hashedFilesRegistry.getResource(requestURI);

		if (resourceURL != null) {
			return true;
		}

		return false;
	}

	@Override
	public FrontendResource handleRequest(HttpServletRequest httpServletRequest)
		throws IOException, ServletException {

		String requestURI = httpServletRequest.getRequestURI();

		String requestHash = HashedFilesUtil.getHash(requestURI);

		ResourceBundle resourceBundle = _getResourceBundle(httpServletRequest);

		if (requestHash != null) {
			if (_log.isDebugEnabled()) {
				_log.debug("Handling request " + requestURI);
			}

			if (resourceBundle == null) {
				return _createFrontendResource(
					requestHash, true, null, requestURI);
			}

			return _createFrontendResource(
				null, false, resourceBundle, requestURI);
		}

		String hashedFileURI = _hashedFilesRegistry.getHashedFileURI(
			requestURI);

		if (hashedFileURI == null) {
			if (_log.isDebugEnabled()) {
				_log.debug("Handling request " + requestURI);
			}

			return _createFrontendResource(
				null, false, resourceBundle, requestURI);
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Handling request ", requestURI, " with static file ",
					hashedFileURI));
		}

		return _createFrontendResource(
			(resourceBundle == null) ? HashedFilesUtil.getHash(hashedFileURI) :
				null,
			false, resourceBundle, hashedFileURI);
	}

	private FrontendResource _createFrontendResource(
		String eTag, boolean immutable, ResourceBundle resourceBundle,
		String resourceURI) {

		long maxAge = 31536000;
		boolean sendNoCache = false;

		if (!immutable) {
			maxAge = _frontendCachingConfiguration.esModulesMaxAge();
			sendNoCache =
				_frontendCachingConfiguration.sendNoCacheForESModules();
		}

		URL resourceURL = _hashedFilesRegistry.getResource(resourceURI);

		if (resourceURL == null) {
			return null;
		}

		return new JavaScriptFrontendResource(
			eTag, immutable, _language, maxAge, resourceBundle, sendNoCache,
			resourceURL);
	}

	private ResourceBundle _getResourceBundle(
		HttpServletRequest httpServletRequest) {

		if (Boolean.valueOf(httpServletRequest.getParameter("translate"))) {
			Locale locale = LocaleUtil.fromLanguageId(
				_language.getLanguageId(httpServletRequest));

			ResourceBundle resourceBundle = LanguageResources.getResourceBundle(
				locale);

			PortletConfig portletConfig = null;

			Enumeration<String> enumeration =
				httpServletRequest.getParameterNames();

			while (enumeration.hasMoreElements()) {
				String parameterName = enumeration.nextElement();

				int index = parameterName.indexOf(CharPool.COLON);

				if (index > 0) {
					portletConfig = _portletConfigFactory.get(
						parameterName.substring(0, index));
				}
			}

			if (portletConfig != null) {
				resourceBundle = new AggregateResourceBundle(
					portletConfig.getResourceBundle(locale), resourceBundle);
			}

			final ResourceBundle finalResourceBundle = resourceBundle;

			return new ResourceBundle() {

				@Override
				public Enumeration<String> getKeys() {
					return finalResourceBundle.getKeys();
				}

				@Override
				public Locale getLocale() {
					return locale;
				}

				@Override
				protected Object handleGetObject(String s) {
					return finalResourceBundle.getObject(s);
				}

			};
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JavaScriptFrontendResourceRequestHandler.class);

	private final FrontendCachingConfiguration _frontendCachingConfiguration;
	private final HashedFilesRegistry _hashedFilesRegistry;
	private final Language _language;
	private final PortletConfigFactory _portletConfigFactory;

}
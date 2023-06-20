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

package com.liferay.frontend.js.web.internal.servlet;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.URL;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	property = {
		"osgi.http.whiteboard.servlet.name=Language Resources Servlet",
		"osgi.http.whiteboard.servlet.pattern=/js/lang/*",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 1000)
	},
	service = Servlet.class
)
public class FrontendJsWebLangServlet extends HttpServlet {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_eTags.clear();

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ServletContext.class, null,
			(serviceReference, emitter) -> {
				ServletContext servletContext = bundleContext.getService(
					serviceReference);

				try {
					emitter.emit(servletContext.getContextPath());
				}
				finally {
					bundleContext.ungetService(serviceReference);
				}
			});
	}

	@Deactivate
	protected void deactivate() {
		_eTags.clear();

		_serviceTrackerMap.close();

		_serviceTrackerMap = null;
	}

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException, ServletException {

		String pathInfo = httpServletRequest.getPathInfo();

		// Check if path is valid

		String[] parts = pathInfo.split(StringPool.SLASH);

		if (parts.length != 3) {
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);

			return;
		}

		// Check if browser cache can be used

		String ifNoneMatch = httpServletRequest.getHeader(
			HttpHeaders.IF_NONE_MATCH);

		if (ifNoneMatch != null) {
			String eTag = _eTags.get(pathInfo);

			if ((eTag != null) && eTag.equals(ifNoneMatch)) {
				httpServletResponse.setStatus(
					HttpServletResponse.SC_NOT_MODIFIED);
				httpServletResponse.setContentLength(0);

				return;
			}
		}

		// Check if servlet context exists

		String fileName = FileUtil.stripExtension(parts[2]);

		ServletContext servletContext = _serviceTrackerMap.getService(
			Portal.PATH_MODULE + StringPool.SLASH + fileName);

		if (servletContext == null) {
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);

			return;
		}

		// Send response

		Locale locale = LocaleUtil.fromLanguageId(parts[1]);

		String content = _getContent(locale, servletContext);

		String etag =
			StringPool.QUOTE + DigesterUtil.digestBase64("SHA-1", content) +
				StringPool.QUOTE;

		_eTags.put(pathInfo, etag);

		httpServletResponse.setCharacterEncoding(StringPool.UTF8);
		httpServletResponse.setContentType(ContentTypes.TEXT_JAVASCRIPT_UTF8);
		httpServletResponse.setHeader(HttpHeaders.ETAG, etag);

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.write(content);
	}

	private String _getContent(Locale locale, ServletContext servletContext)
		throws IOException {

		StringBuilder sb = new StringBuilder();

		sb.append("Object.assign(Liferay.Language._map,{\n");

		JSONArray jsonArray = _getJSONArray(servletContext);

		for (int i = 0; i < jsonArray.length(); i++) {
			String key = jsonArray.getString(i);

			String label = _language.get(locale, key);

			sb.append(StringPool.APOSTROPHE);
			sb.append(key);
			sb.append("':'");
			sb.append(label.replaceAll("'", "\\'"));
			sb.append("',\n");
		}

		sb.append("});");

		return sb.toString();
	}

	private JSONArray _getJSONArray(ServletContext servletContext)
		throws IOException {

		URL url = servletContext.getResource("/__liferay__/lang.json");

		String json;

		try (InputStream inputStream = url.openStream()) {
			json = StringUtil.read(inputStream);
		}

		JSONObject jsonObject;

		try {
			jsonObject = _jsonFactory.createJSONObject(json);
		}
		catch (JSONException jsonException) {
			throw new IOException(
				"Invalid language JSON file " + url, jsonException);
		}

		return jsonObject.getJSONArray("keys");
	}

	private final ConcurrentHashMap<String, String> _eTags =
		new ConcurrentHashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	private ServiceTrackerMap<String, ServletContext> _serviceTrackerMap;

}
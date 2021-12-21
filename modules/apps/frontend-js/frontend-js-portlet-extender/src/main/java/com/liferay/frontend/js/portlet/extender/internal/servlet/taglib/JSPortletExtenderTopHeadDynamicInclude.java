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

package com.liferay.frontend.js.portlet.extender.internal.servlet.taglib;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	service = {
		DynamicInclude.class, JSPortletExtenderTopHeadDynamicInclude.class
	}
)
public class JSPortletExtenderTopHeadDynamicInclude extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.println("<script type=\"importmap\">");

		printWriter.println(
			StringUtil.replace(
				_TPL_IMPORTMAP_JSON, new String[] {"[$SCOPES$]"},
				new String[] {_scopes}));

		printWriter.println("</script>");

		// TODO: decide what to do with importmap shims, given that they show
		// false positive error messages in Firefox

		printWriter.println(
			"<script src=\"https://unpkg.com/es-module-shims@1.3.2/dist" +
				"/es-module-shims.js\"></script>\n");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	public synchronized void register(
		String webContextPath, JSONObject importmapJSONObject) {

		_importmaps.put(webContextPath, importmapJSONObject);

		_recomputeScopes();
	}

	public synchronized void unregister(String webContextPath) {
		_importmaps.remove(webContextPath);
	}

	private static String _loadTemplate(String name) {
		try (InputStream inputStream =
				JSPortletExtenderTopHeadDynamicInclude.class.
					getResourceAsStream(name)) {

			return StringUtil.read(inputStream);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}

		return StringPool.BLANK;
	}

	private synchronized void _recomputeScopes() {
		StringBundler sb = new StringBundler();

		for (Map.Entry<String, JSONObject> entry : _importmaps.entrySet()) {
			if (sb.length() != 0) {
				sb.append(",\n");
			}

			sb.append("  \"");
			sb.append(entry.getKey());
			sb.append("/\": {\n");

			JSONObject jsonObject = entry.getValue();

			boolean first = true;

			for (String key : jsonObject.keySet()) {
				if (first) {
					first = false;
				}
				else {
					sb.append(",\n");
				}

				sb.append("    \"");
				sb.append(key);
				sb.append("\": \"");

				String value = jsonObject.getString(key);

				if (value.startsWith(StringPool.PERIOD)) {
					sb.append(entry.getKey());
					sb.append(StringPool.SLASH);
				}

				sb.append(value);
				sb.append(StringPool.QUOTE);
			}

			sb.append("\n  }");
		}

		_scopes = sb.toString();
	}

	private static final String _TPL_IMPORTMAP_JSON;

	static {
		_TPL_IMPORTMAP_JSON = _loadTemplate("importmap.json.tpl");
	}

	private final Map<String, JSONObject> _importmaps = new HashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

	private String _scopes = "";

}
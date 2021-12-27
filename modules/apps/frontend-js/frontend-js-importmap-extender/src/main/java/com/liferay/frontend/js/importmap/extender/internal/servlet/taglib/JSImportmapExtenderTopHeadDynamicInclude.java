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

package com.liferay.frontend.js.importmap.extender.internal.servlet.taglib;

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
		DynamicInclude.class, JSImportmapExtenderTopHeadDynamicInclude.class
	}
)
public class JSImportmapExtenderTopHeadDynamicInclude
	extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.println("<script type=\"importmap\">");

		printWriter.println(
			StringUtil.replace(
				_TPL_IMPORTMAP_JSON, new String[] {"[$GLOBAL$]", "[$SCOPES$]"},
				new String[] {_global, _scopes}));

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

	public synchronized JSImportmapRegistration register(
		String contextPath, JSONObject jsonObject) {

		if (contextPath == null) {
			long globalId = _nextGlobalId++;

			_globalImportmaps.put(globalId, jsonObject);

			_recomputeGlobal();

			return new JSImportmapRegistration() {

				@Override
				public void unregister() {
					synchronized (JSImportmapExtenderTopHeadDynamicInclude.
						this) {

						_globalImportmaps.remove(globalId);
					}
				}

			};
		}

		_scopedImportmaps.put(contextPath, jsonObject);

		_recomputeScopes();

		return new JSImportmapRegistration() {

			@Override
			public void unregister() {
				synchronized (JSImportmapExtenderTopHeadDynamicInclude.this) {
					_scopedImportmaps.remove(contextPath);
				}
			}

		};
	}

	private static String _loadTemplate(String name) {
		try (InputStream inputStream =
				JSImportmapExtenderTopHeadDynamicInclude.class.
					getResourceAsStream("dependencies/" + name)) {

			return StringUtil.read(inputStream);
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}

		return StringPool.BLANK;
	}

	private synchronized void _recomputeGlobal() {
		StringBundler sb = new StringBundler();

		for (JSONObject jsonObject : _globalImportmaps.values()) {
			for (String key : jsonObject.keySet()) {
				if (sb.length() > 0) {
					sb.append(",\n");
				}

				sb.append("  \"");
				sb.append(key);
				sb.append("\": \"");
				sb.append(jsonObject.getString(key));
				sb.append(StringPool.QUOTE);
			}
		}

		_global = sb.toString();
	}

	private synchronized void _recomputeScopes() {
		StringBundler sb = new StringBundler();

		for (Map.Entry<String, JSONObject> entry :
				_scopedImportmaps.entrySet()) {

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

	private String _global = "";
	private final Map<Long, JSONObject> _globalImportmaps = new HashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

	private long _nextGlobalId;
	private final Map<String, JSONObject> _scopedImportmaps = new HashMap<>();
	private String _scopes = "";

}
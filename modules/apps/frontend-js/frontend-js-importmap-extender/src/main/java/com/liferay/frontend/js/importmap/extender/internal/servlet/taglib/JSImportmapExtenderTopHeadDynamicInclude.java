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

import com.liferay.frontend.js.importmap.extender.JSImportMapRegistration;
import com.liferay.frontend.js.importmap.extender.JSImportmapRegistry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	service = {DynamicInclude.class, JSImportmapRegistry.class}
)
public class JSImportmapExtenderTopHeadDynamicInclude
	extends BaseDynamicInclude implements JSImportmapRegistry {

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

	@Override
	public synchronized JSImportMapRegistration register(
		JSONObject jsonObject) {

		long globalId = _nextGlobalId++;

		_globalImportmaps.put(globalId, jsonObject);

		_recomputeGlobal();

		return new JSImportMapRegistration() {

			@Override
			public void unregister() {
				synchronized (JSImportmapExtenderTopHeadDynamicInclude.this) {
					_globalImportmaps.remove(globalId);
				}
			}

		};
	}

	@Override
	public synchronized JSImportMapRegistration register(
		String webContextPath, JSONObject jsonObject) {

		_scopedImportmaps.put(webContextPath, jsonObject);

		_recomputeScopes();

		return new JSImportMapRegistration() {

			@Override
			public void unregister() {
				synchronized (JSImportmapExtenderTopHeadDynamicInclude.this) {
					_scopedImportmaps.remove(webContextPath);
				}
			}

		};
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleTracker = new BundleTracker<>(
			bundleContext, Bundle.ACTIVE, _bundleTrackerCustomizer);

		_bundleTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_bundleTracker.close();

		_bundleTracker = null;
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

	private String _getWebContextPath(JSONObject packageJSONObject) {

		// TODO: compute web context path correctly

		return StringBundler.concat(
			"/o/", packageJSONObject.getString("name"), "-",
			packageJSONObject.getString("version"));
	}

	private JSONObject _parse(URL url) {
		if (url == null) {
			return null;
		}

		try (InputStream inputStream = url.openStream()) {
			return _jsonFactory.createJSONObject(StringUtil.read(inputStream));
		}
		catch (Exception exception) {
			_log.error("Unable to parse " + url, exception);

			return null;
		}
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

	private static final Log _log = LogFactoryUtil.getLog(
		JSImportmapExtenderTopHeadDynamicInclude.class);

	static {
		_TPL_IMPORTMAP_JSON = _loadTemplate("importmap.json.tpl");
	}

	private BundleTracker<JSImportMapRegistration> _bundleTracker;

	private final BundleTrackerCustomizer<JSImportMapRegistration>
		_bundleTrackerCustomizer =
			new BundleTrackerCustomizer<JSImportMapRegistration>() {

				@Override
				public JSImportMapRegistration addingBundle(
					Bundle bundle, BundleEvent bundleEvent) {

					JSONObject packageJSONObject = _parse(
						bundle.getEntry("META-INF/resources/package.json"));

					if (packageJSONObject == null) {
						return null;
					}

					// TODO: move importmap.json.tpl to META-INF or root of the
					// JAR (needs tweaking the js-toolkit)

					JSONObject importmapJSONObject = _parse(
						bundle.getEntry("META-INF/resources/importmap.json"));

					if (importmapJSONObject == null) {
						return null;
					}

					String webContextPath = _getWebContextPath(
						packageJSONObject);

					if (webContextPath == null) {
						return null;
					}

					return JSImportmapExtenderTopHeadDynamicInclude.this.
						register(webContextPath, importmapJSONObject);
				}

				@Override
				public void modifiedBundle(
					Bundle bundle, BundleEvent bundleEvent,
					JSImportMapRegistration jsImportMapRegistration) {
				}

				@Override
				public void removedBundle(
					Bundle bundle, BundleEvent bundleEvent,
					JSImportMapRegistration jsImportMapRegistration) {

					jsImportMapRegistration.unregister();
				}

			};

	private String _global = "";
	private final Map<Long, JSONObject> _globalImportmaps = new HashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

	private long _nextGlobalId;
	private final Map<String, JSONObject> _scopedImportmaps = new HashMap<>();
	private String _scopes = "";

}
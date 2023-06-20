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

package com.liferay.frontend.js.importmaps.extender.internal.servlet.taglib;

import com.liferay.frontend.js.importmaps.extender.JSImportMapsContributor;
import com.liferay.frontend.js.importmaps.extender.internal.configuration.JSImportMapsConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.frontend.esm.FrontendESMUtil;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilderFactory;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	configurationPid = "com.liferay.frontend.js.importmaps.extender.internal.configuration.JSImportMapsConfiguration",
	property = "service.ranking:Integer=" + Integer.MAX_VALUE,
	service = {
		DynamicInclude.class, JSImportMapsExtenderTopHeadDynamicInclude.class
	}
)
public class JSImportMapsExtenderTopHeadDynamicInclude
	extends BaseDynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		if (_jsImportMapsConfiguration.enableImportMaps()) {
			printWriter.print("<script type=\"");

			if (_jsImportMapsConfiguration.enableESModuleShims()) {
				printWriter.print("importmap-shim");
			}
			else {
				printWriter.print("importmap");
			}

			printWriter.print("\">");
			printWriter.print(_getImportMap(httpServletRequest));
			printWriter.print("</script>");
		}

		if (_jsImportMapsConfiguration.enableESModuleShims()) {
			printWriter.print("<script type=\"esms-options\">{\"shimMode\": ");
			printWriter.print("true}</script><script src=\"");

			AbsolutePortalURLBuilder absolutePortalURLBuilder =
				_absolutePortalURLBuilderFactory.getAbsolutePortalURLBuilder(
					httpServletRequest);

			printWriter.print(
				absolutePortalURLBuilder.forBundleScript(
					_bundleContext.getBundle(),
					"/es-module-shims/es-module-shims.js"
				).build());

			printWriter.print("\"></script>\n");
		}
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	public JSImportMapsRegistration register(
		JSImportMapsContributor jsImportMapsContributor) {

		_jsImportMapsContributors.put(jsImportMapsContributor, true);

		return new JSImportMapsRegistration() {

			@Override
			public void unregister() {
				_jsImportMapsContributors.remove(jsImportMapsContributor);
			}

		};
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_bundleContext = bundleContext;

		modified(properties);
	}

	@Modified
	protected void modified(Map<String, Object> properties) {

		// See LPS-165021

		_jsImportMapsConfiguration = ConfigurableUtil.createConfigurable(
			JSImportMapsConfiguration.class,
			HashMapBuilder.put(
				"enable-es-module-shims", false
			).put(
				"enable-import-maps", true
			).build());

		FrontendESMUtil.setScriptType(
			_jsImportMapsConfiguration.enableESModuleShims() ? "module-shim" :
				"module");
	}

	private String _buildImportMap(Locale locale) {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		jsonObject.put(
			"imports",
			() -> {
				JSONObject importsJSONObject = _jsonFactory.createJSONObject();

				for (JSImportMapsContributor jsImportMapsContributor :
						_jsImportMapsContributors.keySet()) {

					if (jsImportMapsContributor.getScope() != null) {
						continue;
					}

					JSONObject importMapsJSONObject =
						jsImportMapsContributor.getImportMapsJSONObject(locale);

					for (String key : importMapsJSONObject.keySet()) {
						importsJSONObject.put(
							key, importMapsJSONObject.getString(key));
					}
				}

				return importsJSONObject;
			}
		).put(
			"scopes",
			() -> {
				JSONObject scopesJSONObject = _jsonFactory.createJSONObject();

				for (JSImportMapsContributor jsImportMapsContributor :
						_jsImportMapsContributors.keySet()) {

					if (jsImportMapsContributor.getScope() == null) {
						continue;
					}

					String scope = jsImportMapsContributor.getScope();

					if (scopesJSONObject.has(scope)) {
						throw new IllegalStateException(
							"Found multiple JS Import Maps Contributors for " +
								"the same scope " + scope);
					}

					scopesJSONObject.put(
						scope,
						jsImportMapsContributor.getImportMapsJSONObject(
							locale));
				}

				return scopesJSONObject;
			}
		);

		return jsonObject.toString();
	}

	private String _getImportMap(HttpServletRequest httpServletRequest) {
		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Locale locale = LocaleUtil.fromLanguageId(themeDisplay.getLanguageId());

		String importMap = _importMaps.get(locale);

		if (importMap == null) {
			_importMaps.putIfAbsent(locale, _buildImportMap(locale));

			importMap = _importMaps.get(locale);
		}

		return importMap;
	}

	@Reference
	private AbsolutePortalURLBuilderFactory _absolutePortalURLBuilderFactory;

	private volatile BundleContext _bundleContext;
	private final ConcurrentHashMap<Locale, String> _importMaps =
		new ConcurrentHashMap<>();
	private volatile JSImportMapsConfiguration _jsImportMapsConfiguration;
	private final ConcurrentHashMap<JSImportMapsContributor, Boolean>
		_jsImportMapsContributors = new ConcurrentHashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

}
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

package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.frontend.js.loader.modules.extender.npm.JSBundle;
import com.liferay.frontend.js.loader.modules.extender.npm.JSModule;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackageDependency;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.ContentTypes;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
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
		"osgi.http.whiteboard.servlet.name=com.liferay.frontend.js.loader.modules.extender.internal.JSNPMRegistryDebugServlet",
		"osgi.http.whiteboard.servlet.pattern=/js_npm_registry_debug",
		"service.ranking:Integer=" + Details.MAX_VALUE_LESS_1K
	},
	service = {JSNPMRegistryDebugServlet.class, Servlet.class}
)
public class JSNPMRegistryDebugServlet extends HttpServlet {

	@Override
	protected void service(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		response.setContentType(ContentTypes.APPLICATION_JSON);

		ServletOutputStream servletOutputStream = response.getOutputStream();

		PrintWriter printWriter = new PrintWriter(servletOutputStream, true);

		printWriter.write(
			_jsonFactory.looseSerializeDeep(_getJSBundlesObject()));

		printWriter.close();
	}

	private Object _getJSBundleObject(JSBundle jsBundle) {
		Map<String, Object> jsBundleMap = new HashMap<>();

		for (JSPackage jsPackage : jsBundle.getJSPackages()) {
			jsBundleMap.put(
				jsPackage.getName() + StringPool.AT + jsPackage.getVersion(),
				_getJsPackageObject(jsPackage));
		}

		return jsBundleMap;
	}

	private Object _getJSBundlesObject() {
		Map<String, Object> jsBundlesMap = new HashMap<>();

		Set<JSBundle> processedBundles = new HashSet<>();

		for (JSPackage jsPackage : _npmRegistry.getJSPackages()) {
			JSBundle jsBundle = jsPackage.getJSBundle();

			if (processedBundles.contains(jsBundle)) {
				continue;
			}

			processedBundles.add(jsBundle);

			jsBundlesMap.put(
				jsBundle.getName() + StringPool.AT + jsBundle.getVersion(),
				_getJSBundleObject(jsBundle));
		}

		return jsBundlesMap;
	}

	private Object _getJSModuleObject(JSModule jsModule) {
		List<String> dependencies = new ArrayList<>();

		for (String dependency : jsModule.getDependencies()) {
			if (!ModuleNameUtil.isReservedModuleName(dependency)) {
				dependencies.add(dependency);
			}
		}

		return dependencies;
	}

	private Object _getJsPackageObject(JSPackage jsPackage) {
		Map<String, Object> jsPackageMap = new HashMap<>();

		Map<String, String> jsPackageDependenciesMap = new HashMap<>();

		for (JSPackageDependency jsPackageDependency :
				jsPackage.getJSPackageDependencies()) {

			jsPackageDependenciesMap.put(
				jsPackageDependency.getPackageName(),
				jsPackageDependency.getVersionConstraints());
		}

		jsPackageMap.put("dependencies", jsPackageDependenciesMap);

		Map<String, Object> jsModulesMap = new HashMap<>();

		for (JSModule jsModule : jsPackage.getJSModules()) {
			jsModulesMap.put(jsModule.getName(), _getJSModuleObject(jsModule));
		}

		jsPackageMap.put("modules", jsModulesMap);

		return jsPackageMap;
	}

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private NPMRegistry _npmRegistry;

}
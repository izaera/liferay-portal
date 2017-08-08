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

import aQute.bnd.osgi.Constants;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;

import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

/**
 * @author Carlos Sierra Andrés
 */
public class JSLoaderModule {

	public JSLoaderModule(
		boolean applyVersioning, Bundle bundle, String contextPath,
		boolean disableVersioning, String pathProxy) {

		_applyVersioning = applyVersioning;
		_bundle = bundle;
		_contextPath = contextPath;
		_disableVersioning = disableVersioning;
		_pathProxy = pathProxy;

		Version version = _bundle.getVersion();

		_version = version.toString();

		BundleWiring bundleWiring = _bundle.adapt(BundleWiring.class);

		List<BundleCapability> bundleCapabilities =
			bundleWiring.getCapabilities(Details.OSGI_WEBRESOURCE);

		if (bundleCapabilities.isEmpty()) {
			_name = _bundle.getSymbolicName();

			return;
		}

		BundleCapability bundleCapability = bundleCapabilities.get(0);

		Map<String, Object> attributes = bundleCapability.getAttributes();

		_name = (String)attributes.get(Details.OSGI_WEBRESOURCE);

		URL url = _bundle.getEntry(Details.CONFIG_JSON);

		urlToConfiguration(url, bundleWiring);
	}

	public String getContextPath() {
		return _contextPath;
	}

	public String getMapsConfiguration() {
		return _mapsConfiguration;
	}

	public String getName() {
		return _name;
	}

	public String getUnversionedConfiguration() {
		return _unversionedConfiguration;
	}

	public String getUnversionedPathsConfiguration() {
		return _unversionedPathsConfiguration;
	}

	public String getVersion() {
		return _version;
	}

	public String getVersionedConfiguration() {
		return _versionedConfiguration;
	}

	public String getVersionedPathsConfiguration() {
		return _versionedPathsConfiguration;
	}

	protected String generateConfiguration(
		JSONObject jsonObject, BundleWiring bundleWiring, boolean versioned) {

		if (_disableVersioning) {
			JSONObject jsonObject2 = new JSONObject();

			JSONArray namesJSONArray = jsonObject.names();

			for (int i = 0; i < namesJSONArray.length(); i++) {
				String name = (String)namesJSONArray.get(i);

				int x = name.indexOf('/');

				if (x == -1) {
					continue;
				}

				JSONObject nameJSONObject = jsonObject.getJSONObject(name);

				String packageName = name.substring(0, x);

				String[] packageNameParts = packageName.split("@");

				String moduleName = packageNameParts[0] + name.substring(x);

				jsonObject2.put(moduleName, nameJSONObject);
			}

			return jsonObject2.toString();
		}
		else {
			if (!_applyVersioning) {
				if (versioned) {
					return "";
				}

				return jsonObject.toString();
			}

			List<BundleWire> bundleWires = bundleWiring.getRequiredWires(
				Details.OSGI_WEBRESOURCE);

			JSONArray namesJSONArray = jsonObject.names();

			if (namesJSONArray == null) {
				return jsonObject.toString();
			}

			for (int i = 0; i < namesJSONArray.length(); i++) {
				String name = (String)namesJSONArray.get(i);

				int x = name.indexOf('/');

				if (x == -1) {
					continue;
				}

				String moduleName = name.substring(0, x);

				if (!moduleName.equals(getName())) {
					continue;
				}

				String modulePath = name.substring(x);

				moduleName = getName() + "@" + getVersion() + modulePath;

				JSONObject nameJSONObject = jsonObject.getJSONObject(name);

				JSONArray dependenciesJSONArray = nameJSONObject.getJSONArray(
					"dependencies");

				for (int j = 0; j < dependenciesJSONArray.length(); j++) {
					String dependency = dependenciesJSONArray.getString(j);

					int y = dependency.indexOf('/');

					if (y == -1) {
						continue;
					}

					String dependencyName = dependency.substring(0, y);
					String dependencyPath = dependency.substring(y);

					if (dependencyName.equals(getName())) {
						dependencyName =
							getName() + "@" + getVersion() + dependencyPath;

						dependenciesJSONArray.put(j, dependencyName);
					}
					else {
						normalizeDependencies(
							dependencyName, dependencyPath,
							dependenciesJSONArray, j, bundleWires);
					}
				}

				if (versioned) {
					jsonObject.remove(name);

					jsonObject.put(moduleName, nameJSONObject);
				}
				else {
					jsonObject.put(name, nameJSONObject);
				}
			}

			return jsonObject.toString();
		}
	}

	protected String generateMapsConfiguration(
		String configuration, String[] exportJSSubmodules) {

		boolean exportAll = ArrayUtil.contains(
			exportJSSubmodules, StringPool.STAR);

		JSONObject mapsConfigurationJSONObject = new JSONObject();

		mapsConfigurationJSONObject.put(
			getName(), getName() + StringPool.AT + getVersion());

		JSONObject configurationJSONObject = new JSONObject(
			"{" + configuration + "}");

		JSONArray namesJSONArray = configurationJSONObject.names();

		for (int i = 0; i < namesJSONArray.length(); i++) {
			String name = (String)namesJSONArray.get(i);

			int x = name.indexOf('/');

			String moduleRootPath = name.substring(0, x + 1);

			String submodulePath = name.substring(x + 1);

			int y = submodulePath.indexOf('/');

			if (y == -1) {
				continue;
			}

			String submoduleName = submodulePath.substring(0, y);

			if (exportAll ||
				ArrayUtil.contains(exportJSSubmodules, submoduleName)) {

				mapsConfigurationJSONObject.put(
					submoduleName, moduleRootPath.concat(submoduleName));
			}
		}

		return mapsConfigurationJSONObject.toString();
	}

	protected String generatePathsConfiguration(boolean versioned) {
		StringBundler sb = new StringBundler();

		if (versioned) {
			sb.append("\"");
			sb.append(getName());
			sb.append("@");
			sb.append(getVersion());
			sb.append("\": \"");
			sb.append(_pathProxy);
			sb.append(getContextPath());
			sb.append("\"");
		}
		else {
			sb.append("\"");
			sb.append(getName());
			sb.append("\": \"");
			sb.append(_pathProxy);
			sb.append(getContextPath());
			sb.append("\"");
		}

		return sb.toString();
	}

	protected String normalize(String jsonString) {
		if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
			jsonString = jsonString.substring(1, jsonString.length() - 1);
		}

		return jsonString;
	}

	protected void normalizeDependencies(
		String dependencyName, String dependencyPath, JSONArray jsonArray,
		int index, List<BundleWire> bundleWires) {

		for (BundleWire bundleWire : bundleWires) {
			BundleCapability bundleCapability = bundleWire.getCapability();

			Map<String, Object> attributes = bundleCapability.getAttributes();

			String attributesDependencyName = (String)attributes.get(
				Details.OSGI_WEBRESOURCE);

			if (!attributesDependencyName.equals(dependencyName)) {
				continue;
			}

			Version version = (Version)attributes.get(
				Constants.VERSION_ATTRIBUTE);

			dependencyName =
				dependencyName + "@" + version.toString() + dependencyPath;

			jsonArray.put(index, dependencyName);

			return;
		}
	}

	protected void urlToConfiguration(URL url, BundleWiring bundleWiring) {
		if (url == null) {
			return;
		}

		try (Reader reader = new InputStreamReader(url.openStream())) {
			_unversionedPathsConfiguration = generatePathsConfiguration(false);
			_versionedPathsConfiguration = generatePathsConfiguration(true);

			JSONTokener jsonTokener = new JSONTokener(reader);

			JSONObject jsonObject = new JSONObject(jsonTokener);

			_unversionedConfiguration = normalize(
				generateConfiguration(jsonObject, bundleWiring, false));
			_versionedConfiguration = normalize(
				generateConfiguration(jsonObject, bundleWiring, true));

			Dictionary<String, String> headers = _bundle.getHeaders();

			String exportJSSubmodules = GetterUtil.getString(
				headers.get("Liferay-Export-JS-Submodules"));

			_mapsConfiguration = normalize(
				generateMapsConfiguration(
					_unversionedConfiguration,
					StringUtil.split(exportJSSubmodules)));
		}
		catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private final boolean _applyVersioning;
	private final Bundle _bundle;
	private final String _contextPath;
	private final boolean _disableVersioning;
	private String _mapsConfiguration = "";
	private final String _name;
	private final String _pathProxy;
	private String _unversionedConfiguration = "";
	private String _unversionedPathsConfiguration = "";
	private final String _version;
	private String _versionedConfiguration = "";
	private String _versionedPathsConfiguration = "";

}
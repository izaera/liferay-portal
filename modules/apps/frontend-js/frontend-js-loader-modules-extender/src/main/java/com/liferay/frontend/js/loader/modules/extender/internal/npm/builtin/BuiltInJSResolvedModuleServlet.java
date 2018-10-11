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

package com.liferay.frontend.js.loader.modules.extender.internal.npm.builtin;

import com.liferay.frontend.js.loader.modules.extender.npm.JSBundle;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MimeTypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.Servlet;

import com.liferay.portal.kernel.util.ResourceBundleLoader;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.registry.collections.ServiceTrackerCollections;
import com.liferay.registry.collections.ServiceTrackerMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.servlet.name=Serve Package Servlet",
		"osgi.http.whiteboard.servlet.pattern=/js/resolved-module/*",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 1000)
	},
	service = {BuiltInJSResolvedModuleServlet.class, Servlet.class}
)
public class BuiltInJSResolvedModuleServlet extends BaseBuiltInJSModuleServlet {

	@Override
    public void destroy() {
		_bundleSymbolicNameServiceTrackerMap.close();
	}

	@Override
	public void init() {
		_bundleSymbolicNameServiceTrackerMap =
			ServiceTrackerCollections.openSingleValueMap(
				ResourceBundleLoader.class, "bundle.symbolic.name");
	}

	@Override
	protected MimeTypes getMimeTypes() {
		return _mimeTypes;
	}

	@Override
	protected InputStream getResource(String pathInfo) {
		String identifier = pathInfo.substring(1);

		String packageName = ModuleNameUtil.getPackageName(identifier);

		JSPackage jsPackage = _getJSPackage(packageName);

		if (jsPackage == null) {
			return null;
		}

		String packagePath = ModuleNameUtil.getPackagePath(identifier);

		if(packagePath.equals("content/Language.properties.js")) {
			JSBundle jsBundle = jsPackage.getJSBundle();

			ResourceBundleLoader resourceBundleLoader =
				_bundleSymbolicNameServiceTrackerMap.getService(
					jsBundle.getName());

			if(resourceBundleLoader == null) {
				return null;
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter printWriter = new PrintWriter(
				new OutputStreamWriter(baos, Charset.forName("UTF-8")));

			printWriter.print("Liferay.Loader.define('");
			printWriter.print(jsPackage.getResolvedId());
			printWriter.println(
				"/content/Language.properties', ['module'], function (module) {");

			_writeResourceBundle(
				printWriter,
				resourceBundleLoader.loadResourceBundle(
					LocaleUtil.getDefault()),
				"_lang",
				null);

			for (Locale locale : _language.getAvailableLocales()) {
				String language = locale.getLanguage();

				ResourceBundle resourceBundle =
					resourceBundleLoader.loadResourceBundle(
						new Locale(language));

				_writeResourceBundle(
					printWriter, resourceBundle, "_lang_" + language,
					"_lang");

				resourceBundle =
					resourceBundleLoader.loadResourceBundle(locale);

				_writeResourceBundle(
					printWriter, resourceBundle, "_lang_" + locale,
					"_lang");
			}

			printWriter.println("var lang = {");

			for (Locale locale : _language.getAvailableLocales()) {
				String language = locale.getLanguage();

				printWriter.print("  '");
				printWriter.print(language);
				printWriter.print("': _lang_");
				printWriter.print(language);
				printWriter.println(",");

				printWriter.print("  '");
				printWriter.print(locale.toString());
				printWriter.print("': _lang_");
				printWriter.print(locale.toString());
				printWriter.println(",");
			}

			printWriter.println("};");

			printWriter.println("module.exports = function(key) {");
			printWriter.println(
				"  var keys = lang[Liferay.ThemeDisplay.getLanguageId()];");
			printWriter.println("");
			printWriter.println("  if (!keys) {");
			printWriter.println("  	 keys = _lang;");
			printWriter.println("  }");
			printWriter.println("");
			printWriter.println("  var value = keys[key];");
			printWriter.println("");
			printWriter.println("  if (!value) {");
			printWriter.println("    value = key;");
			printWriter.println("  }");
			printWriter.println("");
			printWriter.println("  return value;");
			printWriter.println("};");

			printWriter.println("});");

			printWriter.close();

			return new ByteArrayInputStream(baos.toByteArray());
		}

		URL url = jsPackage.getResourceURL(packagePath);

		try {
			return url.openStream();
		}
		catch (IOException ioe) {
			return null;
		}
	}

	private void _writeResourceBundle(
		PrintWriter printWriter, ResourceBundle resourceBundle, String varName,
		String extendVarName) {

		printWriter.print("var ");
		printWriter.print(varName);
		printWriter.print(" = Object.assign({}, ");

		if (Validator.isNotNull(extendVarName)) {
			printWriter.print(extendVarName);
			printWriter.print(", ");
		}

		printWriter.println("{");

		Enumeration<String> keys = resourceBundle.getKeys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();

			printWriter.print("  '");
			printWriter.print(key.replaceAll("'", "\\\\'"));
			printWriter.print("': '");

			String value = resourceBundle.getString(key);

			printWriter.print(value.replaceAll("'", "\\\\'"));

			printWriter.println("',");
		}

		printWriter.println("});");
	}

	private JSPackage _getJSPackage(String packageName) {
		String jsPackageId = _jsPackageIdsCache.get(packageName);

		if (jsPackageId != null) {
			JSPackage jsPackage = _npmRegistry.getJSPackage(jsPackageId);

			if (jsPackage != null) {
				return jsPackage;
			}

			_jsPackageIdsCache.remove(packageName);
		}

		Collection<JSPackage> jsPackages = _npmRegistry.getResolvedJSPackages();

		for (JSPackage jsPackage : jsPackages) {
			if (packageName.equals(jsPackage.getResolvedId())) {
				_jsPackageIdsCache.put(packageName, jsPackage.getId());

				return jsPackage;
			}
		}

		return null;
	}

	private static final long serialVersionUID = 2647715401054034600L;

	private LinkedHashMap<String, String> _jsPackageIdsCache =
		new LinkedHashMap<String, String>() {

			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				Collection<JSPackage> jsPackages =
					_npmRegistry.getResolvedJSPackages();

				if (size() > jsPackages.size()) {
					return true;
				}

				return false;
			}

		};

	@Reference
	private MimeTypes _mimeTypes;

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private Language _language;

	private ServiceTrackerMap<String, ResourceBundleLoader>
        _bundleSymbolicNameServiceTrackerMap;
}
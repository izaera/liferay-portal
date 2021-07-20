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

package com.liferay.remote.app.admin.web.internal.portlet;

import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoader;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.remote.app.model.RemoteAppEntry;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.Portlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Iván Zaera Avellón
 */
public class JSPortlet extends MVCPortlet {

	public JSPortlet(
		JSPackage jsPackage, String moduleName, RemoteAppEntry remoteAppEntry) {

		_jsPackage = jsPackage;
		_moduleName = moduleName;
		_remoteAppEntry = remoteAppEntry;
	}

	public String getName() {
		return _remoteAppEntry.getNameCurrentLanguageId();
	}

	public synchronized void register(BundleContext bundleContext) {
		if (_serviceRegistration != null) {
			throw new IllegalStateException("Portlet is already registered");
		}

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put("com.liferay.portlet.css-class-wrapper", "js-portlet");
		properties.put(
			"com.liferay.portlet.display-category", "category.sample");
		properties.put("com.liferay.portlet.instanceable", true);
		properties.put("javax.portlet.name", _getPortletName());
		properties.put("javax.portlet.security-role-ref", "power-user,user");
		properties.put(
			"javax.portlet.resource-bundle", _getResourceBundleName());

		_serviceRegistration = bundleContext.registerService(
			Portlet.class, this, properties);

		properties = new Hashtable<>();

		properties.put("resource.bundle.base.name", _getResourceBundleName());
		properties.put("servlet.context.name", "remote-app-admin-web");

		_resourceBundleLoaderServiceRegistration =
			bundleContext.registerService(
				ResourceBundleLoader.class,
				locale -> _getResourceBundle(locale), properties);
	}

	@Override
	public void render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		try {
			PrintWriter printWriter = renderResponse.getWriter();

			String portletElementId =
				"js-portlet-" + renderResponse.getNamespace();

			printWriter.print(
				StringUtil.replace(
					_TPL_HTML, new String[] {"[$PORTLET_ELEMENT_ID$]"},
					new String[] {portletElementId}));

			printWriter.print(
				StringUtil.replace(
					_TPL_JAVA_SCRIPT,
					new String[] {
						"[$CONTEXT_PATH$]", "[$MODULE$]", "[$PACKAGE$]",
						"[$PORTLET_ELEMENT_ID$]", "[$PORTLET_NAMESPACE$]"
					},
					new String[] {
						renderRequest.getContextPath(), _moduleName,
						_jsPackage.getResolvedId(), portletElementId,
						renderResponse.getNamespace()
					}));

			printWriter.flush();
		}
		catch (IOException ioException) {
			_log.error("Unable to render HTML output", ioException);
		}
	}

	public synchronized void unregister() {
		if (_serviceRegistration == null) {
			throw new IllegalStateException("Portlet is not registered");
		}

		_resourceBundleLoaderServiceRegistration.unregister();
		_serviceRegistration.unregister();

		_resourceBundleLoaderServiceRegistration = null;
		_serviceRegistration = null;
	}

	private static String _loadTemplate(String name) {
		try (InputStream inputStream = JSPortlet.class.getResourceAsStream(
				"dependencies/" + name)) {

			return StringUtil.read(inputStream);
		}
		catch (Exception exception) {
			_log.error("Unable to read template " + name, exception);
		}

		return StringPool.BLANK;
	}

	private String _getPortletName() {
		return "js_portlet_" + _remoteAppEntry.getRemoteAppEntryId();
	}

	private ResourceBundle _getResourceBundle(Locale locale) {
		return new ResourceBundle() {

			@Override
			public Enumeration<String> getKeys() {
				return Collections.enumeration(_labels.keySet());
			}

			@Override
			protected Object handleGetObject(String key) {
				return _labels.get(key);
			}

			private final Map<String, String> _labels = HashMapBuilder.put(
				"javax.portlet.title." + _getPortletName(),
				_remoteAppEntry.getName(locale)
			).build();

		};
	}

	private String _getResourceBundleName() {
		return _getPortletName() + ".Language";
	}

	private static final String _TPL_HTML;

	private static final String _TPL_JAVA_SCRIPT;

	private static final Log _log = LogFactoryUtil.getLog(JSPortlet.class);

	static {
		_TPL_HTML = _loadTemplate("bootstrap.html.tpl");
		_TPL_JAVA_SCRIPT = _loadTemplate("bootstrap.js.tpl");
	}

	private final JSPackage _jsPackage;
	private final String _moduleName;
	private final RemoteAppEntry _remoteAppEntry;
	private ServiceRegistration<ResourceBundleLoader>
		_resourceBundleLoaderServiceRegistration;
	private ServiceRegistration<Portlet> _serviceRegistration;

}
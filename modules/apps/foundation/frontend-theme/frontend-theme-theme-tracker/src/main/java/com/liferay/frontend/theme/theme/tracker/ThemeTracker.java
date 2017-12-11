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

package com.liferay.frontend.theme.theme.tracker;

import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.plugin.PluginPackage;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.plugin.PluginPackageUtil;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.ServletContext;

import org.apache.felix.utils.log.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true)
public class ThemeTracker implements
	ServiceTrackerCustomizer<ServletContext, ServiceReference<ServletContext>> {

	@Activate
	@Modified
	public void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		_bundleContext = componentContext.getBundleContext();

		_logger = new Logger(componentContext.getBundleContext());

		if (_serviceTracker != null) {
			_serviceTracker.close();
		}

		_serviceTracker = ServiceTrackerFactory.open(
			componentContext.getBundleContext(),
			"(&(objectClass=" + ServletContext.class.getName() +
				")(osgi.web.contextpath=*))",
			this);
	}

	@Override
	public ServiceReference<ServletContext> addingService(
		ServiceReference<ServletContext> serviceReference) {

		ServletContext servletContext = _bundleContext.getService(
			serviceReference);

		String servletContextName = servletContext.getServletContextName();

		try {
			_logger.log(
				Logger.LOG_DEBUG, "Invoking deploy for " + servletContextName);

			String[] xmls = {
				_http.URLtoString(
					servletContext.getResource(
						"/WEB-INF/liferay-look-and-feel.xml"))
			};

			if (xmls[0] != null) {
				_logger.log(
					Logger.LOG_INFO,
					"Registering themes for " + servletContextName);

				PluginPackage pluginPackage =
					PluginPackageUtil.readPluginPackageServletContext(
						servletContext);

				List<Theme> themes = _themeLocalService.init(
					servletContextName, servletContext, null, true, xmls,
					pluginPackage);

				_themes.put(serviceReference, themes);

				servletContext.setAttribute(WebKeys.PLUGIN_THEMES, themes);

				if (themes.size() == 1) {
					_logger.log(
						Logger.LOG_INFO,
						"1 theme for " + servletContextName +
							" is available for use");
				}
				else {
					StringBundler sb = new StringBundler();

					sb.append(String.valueOf(themes.size()));
					sb.append(" themes for ");
					sb.append(servletContextName);
					sb.append(" are available for use");

					_logger.log(Logger.LOG_INFO, sb.toString());
				}
			}
		}
		catch (DocumentException | IOException e) {
			_logger.log(
				Logger.LOG_ERROR,
				"Unable to look for themes in " + servletContextName, e);
		}

		return serviceReference;
	}

	@Override
	public void modifiedService(
		ServiceReference<ServletContext> serviceReference,
		ServiceReference<ServletContext> trackedServiceReference) {
	}

	@Override
	public void removedService(
		ServiceReference<ServletContext> serviceReference,
		ServiceReference<ServletContext> trackedServiceReference) {

		_themes.remove(serviceReference);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();

		_serviceTracker = null;
	}

	private BundleContext _bundleContext;

	@Reference
	private Http _http;

	private Logger _logger;
	private ServiceTracker<ServletContext, ServiceReference<ServletContext>>
		_serviceTracker;

	@Reference
	private ThemeLocalService _themeLocalService;

	private final Map<ServiceReference<ServletContext>, List<Theme>> _themes =
		new ConcurrentSkipListMap<>();

}
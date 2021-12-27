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

package com.liferay.frontend.js.importmap.extender.internal;

import com.liferay.frontend.js.importmap.extender.JSImportmap;
import com.liferay.frontend.js.importmap.extender.internal.servlet.taglib.JSImportmapExtenderTopHeadDynamicInclude;
import com.liferay.frontend.js.importmap.extender.internal.servlet.taglib.JSImportmapRegistration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = {})
public class JSImportmapExtender {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_bundleTracker = new BundleTracker<>(
			bundleContext, Bundle.ACTIVE, _bundleTrackerCustomizer);

		_bundleTracker.open();

		_serviceTracker = new ServiceTracker<>(
			bundleContext, JSImportmap.class, _serviceTrackerCustomizer);

		_serviceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();

		_serviceTracker = null;

		_bundleTracker.close();

		_bundleTracker = null;

		_bundleContext = null;
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

	private static final Log _log = LogFactoryUtil.getLog(
		JSImportmapExtender.class);

	private BundleContext _bundleContext;
	private BundleTracker<JSImportmapRegistration> _bundleTracker;

	private final BundleTrackerCustomizer<JSImportmapRegistration>
		_bundleTrackerCustomizer =
			new BundleTrackerCustomizer<JSImportmapRegistration>() {

				@Override
				public JSImportmapRegistration addingBundle(
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

					String contextPath = _getWebContextPath(packageJSONObject);

					if (contextPath == null) {
						return null;
					}

					return _jsImportmapExtenderTopHeadDynamicInclude.register(
						contextPath, importmapJSONObject);
				}

				@Override
				public void modifiedBundle(
					Bundle bundle, BundleEvent bundleEvent,
					JSImportmapRegistration jsImportmapRegistration) {
				}

				@Override
				public void removedBundle(
					Bundle bundle, BundleEvent bundleEvent,
					JSImportmapRegistration jsImportmapRegistration) {

					jsImportmapRegistration.unregister();
				}

			};

	@Reference
	private JSImportmapExtenderTopHeadDynamicInclude
		_jsImportmapExtenderTopHeadDynamicInclude;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceTracker<JSImportmap, JSImportmapRegistration>
		_serviceTracker;

	private final ServiceTrackerCustomizer<JSImportmap, JSImportmapRegistration>
		_serviceTrackerCustomizer =
			new ServiceTrackerCustomizer
				<JSImportmap, JSImportmapRegistration>() {

				@Override
				public JSImportmapRegistration addingService(
					ServiceReference<JSImportmap> serviceReference) {

					JSImportmap jsImportmap = _bundleContext.getService(
						serviceReference);

					return _jsImportmapExtenderTopHeadDynamicInclude.register(
						jsImportmap.getContextPath(),
						jsImportmap.getImportmap());
				}

				@Override
				public void modifiedService(
					ServiceReference serviceReference,
					JSImportmapRegistration jsImportmapRegistration) {
				}

				@Override
				public void removedService(
					ServiceReference serviceReference,
					JSImportmapRegistration jsImportmapRegistration) {

					jsImportmapRegistration.unregister();
				}

			};

}
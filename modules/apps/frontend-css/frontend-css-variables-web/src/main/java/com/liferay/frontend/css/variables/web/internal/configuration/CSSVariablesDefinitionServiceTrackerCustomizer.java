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

package com.liferay.frontend.css.variables.web.internal.configuration;

import com.liferay.frontend.css.variables.CSSVariablesDefinition;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Iván Zaera Avellón
 */
public class CSSVariablesDefinitionServiceTrackerCustomizer
	implements ServiceTrackerCustomizer
		<ServletContext, CSSVariablesDefinition> {

	public CSSVariablesDefinitionServiceTrackerCustomizer(
		BundleContext bundleContext, JSONFactory jsonFactory) {

		_bundleContext = bundleContext;
		_jsonFactory = jsonFactory;
	}

	@Override
	public CSSVariablesDefinition addingService(
		ServiceReference<ServletContext> serviceReference) {

		CSSVariablesDefinition cssVariablesDefinition =
			new CSSVariablesDefinitionImpl(_jsonFactory);

		modifiedService(serviceReference, cssVariablesDefinition);

		return cssVariablesDefinition;
	}

	@Override
	public void modifiedService(
		ServiceReference<ServletContext> serviceReference,
		CSSVariablesDefinition cssVariablesDefinition) {

		ServletContext servletContext = _bundleContext.getService(
			serviceReference);

		try {
			InputStream is = servletContext.getResourceAsStream(
				"/WEB-INF/css-variables.json");

			if (is == null) {
				return;
			}

			try {
				CSSVariablesDefinitionImpl cssVariablesDefinitionImpl =
					(CSSVariablesDefinitionImpl)cssVariablesDefinition;

				cssVariablesDefinitionImpl.parse(StringUtil.read(is));
			}
			catch (IllegalArgumentException illegalArgumentException) {
				_log.error(
					StringBundler.concat(
						"Unable to parse css-variables.json of servlet ",
						"context ", servletContext.getServletContextName()),
					illegalArgumentException);
			}
			catch (IOException ioException) {
				_log.error(
					"Unable to read css-variables.json of servlet context" +
						servletContext.getServletContextName(),
					ioException);
			}
			catch (JSONException jsonException) {
				_log.error(
					"Unable to parse css-variables.json of servlet context" +
						servletContext.getServletContextName(),
					jsonException);
			}
		}
		finally {
			_bundleContext.ungetService(serviceReference);
		}
	}

	@Override
	public void removedService(
		ServiceReference<ServletContext> serviceReference,
		CSSVariablesDefinition cssVariablesDefinition) {
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CSSVariablesDefinitionServiceTrackerCustomizer.class);

	private final BundleContext _bundleContext;
	private final JSONFactory _jsonFactory;

}
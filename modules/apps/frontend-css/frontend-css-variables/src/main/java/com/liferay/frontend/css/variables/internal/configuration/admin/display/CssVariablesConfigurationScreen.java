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

package com.liferay.frontend.css.variables.internal.configuration.admin.display;

import com.liferay.configuration.admin.display.ConfigurationScreen;
import com.liferay.frontend.css.variables.internal.configuration.CssVariablesConfiguration;
import com.liferay.frontend.css.variables.internal.constants.CssVariablesWebKeys;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.io.IOException;

import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = ConfigurationScreen.class)
public class CssVariablesConfigurationScreen implements ConfigurationScreen {

	@Override
	public String getCategoryKey() {
		return "instance-configuration";
	}

	@Override
	public String getKey() {
		return "css-variables";
	}

	@Override
	public String getName(Locale locale) {
		return LanguageUtil.get(
			ResourceBundleUtil.getBundle(locale, getClass()), getKey());
	}

	@Override
	public String getScope() {
		return "company";
	}

	@Override
	public void render(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException {

		httpServletRequest.setAttribute(
			CssVariablesWebKeys.PROPERTIES,
			_cssVariablesConfiguration.getProperties(httpServletRequest));

		RequestDispatcher requestDispatcher =
			_servletContext.getRequestDispatcher("/configuration.jsp");

		try {
			requestDispatcher.include(httpServletRequest, httpServletResponse);
		}
		catch (ServletException servletException) {
			throw new IOException(servletException);
		}
	}

	@Reference
	private CssVariablesConfiguration _cssVariablesConfiguration;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.frontend.css.variables)",
		unbind = "-"
	)
	private ServletContext _servletContext;

}
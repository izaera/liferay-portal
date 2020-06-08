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

package com.liferay.frontend.css.variables.internal.configuration;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ValidatorException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = CssVariablesConfiguration.class)
public class CssVariablesConfiguration {

	public Map<String, String> getProperties(
		HttpServletRequest httpServletRequest) {

		Map<String, String> properties = new HashMap<>();

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Theme theme = themeDisplay.getTheme();

		CssVariablesDefinition cssVariablesDefinition =
			_serviceTrackerMap.getService(theme.getServletContextName());

		if (cssVariablesDefinition == null) {
			return properties;
		}

		Settings settings = _getSettings(themeDisplay.getCompanyId());

		for (String cssVariableName :
				cssVariablesDefinition.getCssVariableNames()) {

			properties.put(
				cssVariableName, settings.getValue(cssVariableName, null));
		}

		return properties;
	}

	public void setProperties(ActionRequest actionRequest)
		throws ValidatorException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Theme theme = themeDisplay.getTheme();

		CssVariablesDefinition cssVariablesDefinition =
			_serviceTrackerMap.getService(theme.getServletContextName());

		Settings settings = _getSettings(themeDisplay.getCompanyId());

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		modifiableSettings.reset();

		for (String cssVariableName :
				cssVariablesDefinition.getCssVariableNames()) {

			String value = ParamUtil.getString(actionRequest, cssVariableName);

			if (Validator.isNotNull(value)) {
				modifiableSettings.setValue(cssVariableName, value);
			}
			else {
				modifiableSettings.reset(cssVariableName);
			}
		}

		try {
			modifiableSettings.store();
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ServletContext.class, "osgi.web.symbolicname",
			new CssVariablesDefinitionServiceTrackerCustomizer(
				_bundleContext, _jsonFactory));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	@Reference
	protected SettingsFactory settingsFactory;

	private Settings _getSettings(long companyId) {
		try {
			return settingsFactory.getSettings(
				new CompanyServiceSettingsLocator(companyId, _SETTINGS_ID));
		}
		catch (SettingsException settingsException) {
			throw new RuntimeException(settingsException);
		}
	}

	private static final String _SETTINGS_ID =
		CssVariablesConfiguration.class.getName();

	private BundleContext _bundleContext;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceTrackerMap<String, CssVariablesDefinition>
		_serviceTrackerMap;

}
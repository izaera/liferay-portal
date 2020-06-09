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

import com.liferay.frontend.css.variables.CSSVariablesConfiguration;
import com.liferay.frontend.css.variables.CSSVariablesDefinition;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.ModifiableSettings;
import com.liferay.portal.kernel.settings.Settings;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsFactory;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.ValidatorException;

import javax.servlet.ServletContext;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = CSSVariablesConfiguration.class)
public class CSSVariablesConfigurationImpl
	implements CSSVariablesConfiguration {

	@Override
	public Map<String, String> getCSSVariables(Theme theme, long companyId) {
		Map<String, String> cssVariables = new HashMap<>();

		CSSVariablesDefinition cssVariablesDefinition =
			getCSSVariablesDefinition(theme);

		if (cssVariablesDefinition == null) {
			return cssVariables;
		}

		Settings settings = _getSettings(theme, companyId);

		for (String cssVariableName :
				cssVariablesDefinition.getCSSVariableNames()) {

			cssVariables.put(
				cssVariableName, settings.getValue(cssVariableName, null));
		}

		return cssVariables;
	}

	@Override
	public CSSVariablesDefinition getCSSVariablesDefinition(Theme theme) {
		return _serviceTrackerMap.getService(theme.getServletContextName());
	}

	@Override
	public void setCSSVariables(
		Theme theme, long companyId, Map<String, String> cssVariables) {

		CSSVariablesDefinition cssVariablesDefinition =
			getCSSVariablesDefinition(theme);

		Settings settings = _getSettings(theme, companyId);

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		modifiableSettings.reset();

		for (String cssVariableName :
				cssVariablesDefinition.getCSSVariableNames()) {

			String value = cssVariables.get(cssVariableName);

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
		catch (IOException | ValidatorException exception) {
			throw new RuntimeException(exception);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ServletContext.class, "osgi.web.symbolicname",
			new CSSVariablesDefinitionServiceTrackerCustomizer(
				_bundleContext, _jsonFactory));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	@Reference
	protected SettingsFactory settingsFactory;

	private Settings _getSettings(Theme theme, long companyId) {
		try {
			return settingsFactory.getSettings(
				new CompanyServiceSettingsLocator(
					companyId,
					_SETTINGS_ID + StringPool.POUND + theme.getThemeId()));
		}
		catch (SettingsException settingsException) {
			throw new RuntimeException(settingsException);
		}
	}

	private static final String _SETTINGS_ID =
		CSSVariablesConfiguration.class.getName();

	private BundleContext _bundleContext;

	@Reference
	private JSONFactory _jsonFactory;

	private ServiceTrackerMap<String, CSSVariablesDefinition>
		_serviceTrackerMap;

}
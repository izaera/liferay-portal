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

package com.liferay.layout.admin.web.internal.css.variables;

import com.liferay.frontend.css.variables.CSSVariableDescription;
import com.liferay.frontend.css.variables.theme.ThemeCSSVariableDescriptionsRegistry;
import com.liferay.layout.admin.css.variables.LayoutSetCSSVariablesConfiguration;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.LayoutSet;
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = LayoutSetCSSVariablesConfiguration.class)
public class LayoutSetCSSVariablesConfigurationImpl
	implements LayoutSetCSSVariablesConfiguration {

	@Override
	public Map<String, String> getCSSVariables(LayoutSet layoutSet) {
		Map<String, String> cssVariables = new HashMap<>();

		Map<String, CSSVariableDescription> cssVariableDescriptions =
			_themeCSSVariableDescriptionsRegistry.getCSSVariableDescriptions(
				layoutSet.getTheme());

		if (cssVariableDescriptions == null) {
			return cssVariables;
		}

		Settings settings = _getSettings(layoutSet);

		for (String cssVariableName : cssVariableDescriptions.keySet()) {
			cssVariables.put(
				cssVariableName, settings.getValue(cssVariableName, null));
		}

		return cssVariables;
	}

	@Override
	public void setCSSVariables(
		LayoutSet layoutSet, Map<String, String> cssVariables) {

		Map<String, CSSVariableDescription> cssVariableDescriptions =
			_themeCSSVariableDescriptionsRegistry.getCSSVariableDescriptions(
				layoutSet.getTheme());

		Settings settings = _getSettings(layoutSet);

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		modifiableSettings.reset();

		for (String cssVariableName : cssVariableDescriptions.keySet()) {
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

	private Settings _getSettings(LayoutSet layoutSet) {
		try {
			return _settingsFactory.getSettings(
				new CompanyServiceSettingsLocator(
					layoutSet.getCompanyId(),
					_SETTINGS_ID + StringPool.POUND +
						layoutSet.getLayoutSetId()));
		}
		catch (SettingsException settingsException) {
			throw new RuntimeException(settingsException);
		}
	}

	private static final String _SETTINGS_ID = "layout-css-variables";

	@Reference
	private SettingsFactory _settingsFactory;

	@Reference
	private ThemeCSSVariableDescriptionsRegistry
		_themeCSSVariableDescriptionsRegistry;

}
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ValidatorException;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = CssVariablesConfiguration.class)
public class CssVariablesConfiguration {

	public static final List<String> keys = Collections.unmodifiableList(
		Arrays.asList("body-bg"));

	public Map<String, String> getProperties(
		HttpServletRequest httpServletRequest) {

		Map<String, String> properties = new HashMap<>();

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Settings settings = _getSettings(themeDisplay.getCompanyId());

		for (String key : keys) {
			String value = settings.getValue(key, null);

			if (Validator.isNotNull(value)) {
				properties.put(key, value);
			}
		}

		return properties;
	}

	public void setProperties(ActionRequest actionRequest)
		throws ValidatorException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		Settings settings = _getSettings(themeDisplay.getCompanyId());

		ModifiableSettings modifiableSettings =
			settings.getModifiableSettings();

		for (String key : keys) {
			String value = ParamUtil.getString(actionRequest, key);

			if (Validator.isNotNull(value)) {
				modifiableSettings.setValue(key, value);
			}
			else {
				modifiableSettings.reset(key);
			}
		}

		try {
			modifiableSettings.store();
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
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

}
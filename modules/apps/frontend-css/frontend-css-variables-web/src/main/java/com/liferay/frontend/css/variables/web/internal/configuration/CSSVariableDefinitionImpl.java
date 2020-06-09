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

import com.liferay.frontend.css.variables.CSSVariableDefinition;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class CSSVariableDefinitionImpl implements CSSVariableDefinition {

	public CSSVariableDefinitionImpl() {
		this(StringPool.BLANK);
	}

	public CSSVariableDefinitionImpl(String defaultLabel) {
		_labelsMap.put(StringPool.BLANK, defaultLabel);
	}

	public void addLabel(String localeKey, String label) {
		_labelsMap.put(localeKey, label);
	}

	public String getLabel(Locale locale) {
		String languageCountryKey = _getLanguageCountryKey(locale);

		if ((languageCountryKey != null) &&
			_labelsMap.containsKey(languageCountryKey)) {

			return _labelsMap.get(languageCountryKey);
		}

		if (_labelsMap.containsKey(locale.getLanguage())) {
			return _labelsMap.get(locale.getLanguage());
		}

		return _labelsMap.get(StringPool.BLANK);
	}

	private String _getLanguageCountryKey(Locale locale) {
		if (Validator.isNull(locale.getCountry())) {
			return null;
		}

		return locale.getLanguage() + StringPool.UNDERLINE +
			locale.getCountry();
	}

	private final Map<String, String> _labelsMap = new HashMap<>();

}
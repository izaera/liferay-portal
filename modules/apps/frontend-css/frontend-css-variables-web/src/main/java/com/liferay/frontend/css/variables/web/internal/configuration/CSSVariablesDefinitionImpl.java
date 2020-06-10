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
import com.liferay.frontend.css.variables.CSSVariableType;
import com.liferay.frontend.css.variables.CSSVariablesDefinition;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Iván Zaera Avellón
 */
public class CSSVariablesDefinitionImpl implements CSSVariablesDefinition {

	public CSSVariablesDefinitionImpl(JSONFactory jsonFactory) {
		_jsonFactory = jsonFactory;
	}

	@Override
	public CSSVariableDefinition getCSSVariableDefinition(String name) {
		return _cssVariableDefinitions.get(name);
	}

	@Override
	public Set<String> getCSSVariableNames() {
		return Collections.unmodifiableSet(_cssVariableDefinitions.keySet());
	}

	protected void parse(String json) throws JSONException {
		JSONObject jsonObject = _jsonFactory.createJSONObject(json);

		JSONObject variablesJSONObject = jsonObject.getJSONObject("variables");

		if (variablesJSONObject == null) {
			throw new IllegalArgumentException(
				"Unable to read variables field");
		}

		for (String name : variablesJSONObject.keySet()) {
			JSONObject cssVariableDefinitionJSONObject =
				variablesJSONObject.getJSONObject(name);

			_cssVariableDefinitions.put(
				name,
				new CSSVariableDefinitionImpl(
					_getCSSVariableType(cssVariableDefinitionJSONObject),
					_getLabelsMap(cssVariableDefinitionJSONObject, name)));
		}
	}

	private CSSVariableType _getCSSVariableType(JSONObject jsonObject) {
		String type = jsonObject.getString("type");

		if (type.equals("color")) {
			return CSSVariableType.COLOR;
		}

		return CSSVariableType.STRING;
	}

	private Map<String, String> _getLabelsMap(
		JSONObject cssVariableDefinitionJSONObject, String defaultLabel) {

		Map<String, String> labelsMap = new HashMap<>();

		JSONObject labelsMapJSONObject =
			cssVariableDefinitionJSONObject.getJSONObject("label");

		if (labelsMapJSONObject != null) {
			for (String localeKey : labelsMapJSONObject.keySet()) {
				labelsMap.put(
					localeKey, labelsMapJSONObject.getString(localeKey));
			}
		}
		else {
			String label = cssVariableDefinitionJSONObject.getString("label");

			if (label == null) {
				label = defaultLabel;
			}

			labelsMap.put(StringPool.BLANK, label);
		}

		return labelsMap;
	}

	private final Map<String, CSSVariableDefinition> _cssVariableDefinitions =
		new HashMap<>();
	private final JSONFactory _jsonFactory;

}
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
import com.liferay.frontend.css.variables.CSSVariablesDefinition;
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

			JSONObject labelsMapJSONObject =
				cssVariableDefinitionJSONObject.getJSONObject("label");

			if (labelsMapJSONObject != null) {
				CSSVariableDefinitionImpl cssVariableDefinitionImpl =
					new CSSVariableDefinitionImpl();

				for (String localeKey : labelsMapJSONObject.keySet()) {
					cssVariableDefinitionImpl.addLabel(
						localeKey, labelsMapJSONObject.getString(localeKey));
				}

				_cssVariableDefinitions.put(name, cssVariableDefinitionImpl);

				continue;
			}

			String label = cssVariableDefinitionJSONObject.getString("label");

			if (label != null) {
				_cssVariableDefinitions.put(
					name, new CSSVariableDefinitionImpl(label));

				continue;
			}

			_cssVariableDefinitions.put(
				name, new CSSVariableDefinitionImpl(name));
		}
	}

	private final Map<String, CSSVariableDefinition> _cssVariableDefinitions =
		new HashMap<>();
	private final JSONFactory _jsonFactory;

}
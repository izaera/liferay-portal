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

package com.liferay.frontend.js.loader.modules.extender.internal.resolution.descriptor;

import com.liferay.frontend.js.loader.modules.extender.internal.cfggen.JSConfigGeneratorModule;
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.JSModuleDescriptor;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Rodolfo Roza Miranda
 */
public class ConfigGeneratorModuleDescriptor implements JSModuleDescriptor {

	public ConfigGeneratorModuleDescriptor(
		JSConfigGeneratorModule jsConfigGeneratorModule, Portal portal) {

		_jsConfigGeneratorModule = jsConfigGeneratorModule;
		_portal = portal;

		_initialize();
	}

	@Override
	public String getAlias() {
		return _alias;
	}

	@Override
	public Collection<String> getDependencies() {
		return _dependencies;
	}

	@Override
	public Map<String, String> getMap() {
		return null;
	}

	@Override
	public String getPath() {
		return _portal.getPathProxy() +
			_jsConfigGeneratorModule.getContextPath();
	}

	private void _initialize() {
		String unversionedConfiguration =
			_jsConfigGeneratorModule.getUnversionedConfiguration();

		if (Validator.isNotNull(unversionedConfiguration)) {
			try {
				String jsonString = String.format(
					"{%s}", unversionedConfiguration);

				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
					jsonString);

				Iterator<String> keys = jsonObject.keys();

				_alias = keys.next();

				JSONObject aliasConfig = jsonObject.getJSONObject(_alias);

				JSONArray dependencies = aliasConfig.getJSONArray(
					"dependencies");

				dependencies.forEach(
					dependency -> _dependencies.add((String)dependency));
			}
			catch (JSONException jsone) {
				throw new RuntimeException(jsone);
			}
		}
	}

	private String _alias = StringPool.BLANK;
	private Set<String> _dependencies = new HashSet<>();
	private final JSConfigGeneratorModule _jsConfigGeneratorModule;
	private final Portal _portal;

}
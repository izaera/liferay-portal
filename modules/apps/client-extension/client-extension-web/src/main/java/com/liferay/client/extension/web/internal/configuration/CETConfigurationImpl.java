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

package com.liferay.client.extension.web.internal.configuration;

import com.liferay.client.extension.type.configuration.CETConfiguration;

/**
 * @author Iván Zaera Avellón
 */
public class CETConfigurationImpl implements CETConfiguration {

	public void addTypeSetting(String typeSetting) {
		String[] typeSettings = new String[_typeSettings.length + 1];

		System.arraycopy(
			_typeSettings, 0, typeSettings, 0, _typeSettings.length);

		typeSettings[typeSettings.length - 1] = typeSetting;

		_typeSettings = typeSettings;
	}

	@Override
	public String baseURL() {
		return _baseURL;
	}

	@Override
	public String description() {
		return _description;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public String[] properties() {
		return _PROPERTIES;
	}

	public void setBaseURL(String baseURL) {
		_baseURL = baseURL;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setSourceCodeURL(String sourceCodeURL) {
		_sourceCodeURL = sourceCodeURL;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String sourceCodeURL() {
		return _sourceCodeURL;
	}

	@Override
	public String type() {
		return _type;
	}

	@Override
	public String[] typeSettings() {
		return _typeSettings;
	}

	private static final String[] _PROPERTIES = {};

	private String _baseURL = "";
	private String _description = "";
	private String _name = "";
	private String _sourceCodeURL = "";
	private String _type = "";
	private String[] _typeSettings = {};

}
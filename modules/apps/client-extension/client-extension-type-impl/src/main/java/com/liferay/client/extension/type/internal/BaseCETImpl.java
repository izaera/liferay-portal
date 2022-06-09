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

package com.liferay.client.extension.type.internal;

import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.type.CET;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Locale;

/**
 * @author Brian Wing Shun Chan
 */
public abstract class BaseCETImpl implements CET {

	public BaseCETImpl(ClientExtensionEntry clientExtensionEntry) {
		_clientExtensionEntry = clientExtensionEntry;

		if (clientExtensionEntry != null) {
			_companyId = clientExtensionEntry.getCompanyId();
			_description = clientExtensionEntry.getDescription();
			_externalReferenceCode =
				clientExtensionEntry.getExternalReferenceCode();
			_sourceCodeURL = clientExtensionEntry.getSourceCodeURL();
			_status = clientExtensionEntry.getStatus();
			_typeSettingsUnicodeProperties = UnicodePropertiesBuilder.create(
				true
			).load(
				clientExtensionEntry.getTypeSettings()
			).build();
		}
		else {
			_typeSettingsUnicodeProperties = UnicodePropertiesBuilder.create(
				true
			).build();
		}
	}

	public BaseCETImpl(
		String baseURL, long companyId, String description,
		String externalReferenceCode, String name, String sourceCodeURL,
		UnicodeProperties typeSettingsUnicodeProperties) {

		this(typeSettingsUnicodeProperties);

		_baseURL = baseURL;
		_companyId = companyId;
		_description = description;
		_externalReferenceCode = externalReferenceCode;
		_name = name;
		_sourceCodeURL = sourceCodeURL;

		_readOnly = true;
	}

	public BaseCETImpl(UnicodeProperties typeSettingsUnicodeProperties) {
		_typeSettingsUnicodeProperties = typeSettingsUnicodeProperties;
	}

	@Override
	public String getBaseURL() {
		return _baseURL;
	}

	@Override
	public long getCompanyId() {
		return _companyId;
	}

	@Override
	public String getDescription() {
		return _description;
	}

	@Override
	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	@Override
	public String getName(Locale locale) {
		if (_clientExtensionEntry != null) {
			return _clientExtensionEntry.getName(locale);
		}

		return _name;
	}

	@Override
	public String getSourceCodeURL() {
		return _sourceCodeURL;
	}

	@Override
	public int getStatus() {
		return _status;
	}

	@Override
	public boolean isReadOnly() {
		return _readOnly;
	}

	@Override
	public String toString() {
		return _typeSettingsUnicodeProperties.toString();
	}

	protected boolean getBoolean(String key) {
		return GetterUtil.getBoolean(
			_typeSettingsUnicodeProperties.getProperty(key));
	}

	protected String getString(String key) {
		return GetterUtil.getString(
			_typeSettingsUnicodeProperties.getProperty(key));
	}

	private String _baseURL = StringPool.BLANK;
	private ClientExtensionEntry _clientExtensionEntry;
	private long _companyId;
	private String _description = StringPool.BLANK;
	private String _externalReferenceCode = StringPool.BLANK;
	private String _name = StringPool.BLANK;
	private boolean _readOnly;
	private String _sourceCodeURL = StringPool.BLANK;
	private int _status = WorkflowConstants.STATUS_APPROVED;
	private final UnicodeProperties _typeSettingsUnicodeProperties;

}
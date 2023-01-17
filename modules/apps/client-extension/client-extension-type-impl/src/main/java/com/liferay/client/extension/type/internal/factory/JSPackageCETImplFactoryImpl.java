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

package com.liferay.client.extension.type.internal.factory;

import com.liferay.client.extension.exception.ClientExtensionEntryTypeSettingsException;
import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.type.JSPackageCET;
import com.liferay.client.extension.type.factory.CETImplFactory;
import com.liferay.client.extension.type.internal.JSPackageCETImpl;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;

import java.util.Properties;

import javax.portlet.PortletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class JSPackageCETImplFactoryImpl
	implements CETImplFactory<JSPackageCET> {

	@Override
	public JSPackageCET create(ClientExtensionEntry clientExtensionEntry)
		throws PortalException {

		return new JSPackageCETImpl(clientExtensionEntry);
	}

	@Override
	public JSPackageCET create(PortletRequest portletRequest)
		throws PortalException {

		return new JSPackageCETImpl(portletRequest);
	}

	@Override
	public JSPackageCET create(
			String baseURL, long companyId, String description,
			String externalReferenceCode, String name, Properties properties,
			String sourceCodeURL, UnicodeProperties unicodeProperties)
		throws PortalException {

		return new JSPackageCETImpl(
			baseURL, companyId, description, externalReferenceCode, name,
			properties, sourceCodeURL, unicodeProperties);
	}

	@Override
	public void validate(
			UnicodeProperties newTypeSettingsUnicodeProperties,
			UnicodeProperties oldTypeSettingsUnicodeProperties)
		throws PortalException {

		JSPackageCET newJSPackageCET = new JSPackageCETImpl(
			StringPool.NEW_LINE, newTypeSettingsUnicodeProperties);

		if (Validator.isNull(newJSPackageCET.getIdentifier())) {
			throw new ClientExtensionEntryTypeSettingsException(
				"identifier-is-empty");
		}

		if (!Validator.isUrl(newJSPackageCET.getURL())) {
			throw new ClientExtensionEntryTypeSettingsException(
				"please-enter-a-valid-url");
		}
	}

}
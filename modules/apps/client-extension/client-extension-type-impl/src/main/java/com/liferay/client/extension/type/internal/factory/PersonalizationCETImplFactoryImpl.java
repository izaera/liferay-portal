/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.client.extension.type.internal.factory;

import com.liferay.client.extension.exception.ClientExtensionEntryTypeSettingsException;
import com.liferay.client.extension.type.PersonalizationCET;
import com.liferay.client.extension.type.internal.PersonalizationCETImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;

import jakarta.portlet.PortletRequest;

import java.util.Date;
import java.util.Properties;

/**
 * @author Iván Zaera Avellón
 */
public class PersonalizationCETImplFactoryImpl
	extends BaseCETImplFactoryImpl<PersonalizationCET> {

	public PersonalizationCETImplFactoryImpl() {
		super(PersonalizationCET.class);
	}

	@Override
	public PersonalizationCET create(
		String baseURL, long companyId, Date createDate, String description,
		String externalReferenceCode, Date modifiedDate, String name,
		Properties properties, boolean readOnly, String sourceCodeURL,
		int status, UnicodeProperties typeSettingsUnicodeProperties) {

		return new PersonalizationCETImpl(
			baseURL, companyId, createDate, description, externalReferenceCode,
			modifiedDate, name, properties, readOnly, sourceCodeURL, status,
			typeSettingsUnicodeProperties);
	}

	@Override
	public UnicodeProperties getUnicodeProperties(
		PortletRequest portletRequest) {

		return UnicodePropertiesBuilder.create(
			true
		).put(
			"javaScript", ParamUtil.getString(portletRequest, "javaScript")
		).put(
			"rulesURL", ParamUtil.getString(portletRequest, "rulesURL")
		).build();
	}

	@Override
	public void validate(
			PersonalizationCET newPersonalizationCET,
			PersonalizationCET oldPersonalizationCET)
		throws PortalException {

		String rulesURL = newPersonalizationCET.getRulesURL();

		if (!Validator.isUrl(rulesURL)) {
			throw new ClientExtensionEntryTypeSettingsException(
				"Invalid rules URL: " + rulesURL, "rules-url-x-is-invalid",
				rulesURL);
		}
	}

}
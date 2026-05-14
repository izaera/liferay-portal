/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.personalization.web.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Iván Zaera Avellón
 */
@ExtendedObjectClassDefinition(
	category = "infrastructure",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY, strictScope = true
)
@Meta.OCD(
	id = "com.liferay.frontend.js.personalization.web.internal.configuration.FrontendJSPersonalizationConfiguration",
	localization = "content/Language",
	name = "frontend-js-personalization-configuration-name"
)
public interface FrontendJSPersonalizationConfiguration {

	@Meta.AD(
		deflt = "https://izaera.github.io/audiences-poc/handlers.js",
		description = "handlers-url-help", name = "handlers-url"
	)
	public String handlersURL();

	@Meta.AD(
		deflt = "https://izaera.github.io/audiences-poc/rules.json",
		description = "rules-url-help", name = "rules-url"
	)
	public String rulesURL();

}
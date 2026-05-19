/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.personalization.web.internal.servlet.taglib;

import com.liferay.frontend.js.personalization.web.internal.configuration.FrontendJSPersonalizationConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = DynamicInclude.class)
public class FrontendJSPersonalizationWebTopHeadDynamicInclude
	implements DynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (!FeatureFlagManagerUtil.isEnabled(
				themeDisplay.getCompanyId(), "LPD-83647")) {

			return;
		}

		FrontendJSPersonalizationConfiguration
			frontendJSPersonalizationConfiguration;

		try {
			frontendJSPersonalizationConfiguration =
				_configurationProvider.getCompanyConfiguration(
					FrontendJSPersonalizationConfiguration.class,
					themeDisplay.getCompanyId());
		}
		catch (ConfigurationException configurationException) {
			throw new IOException(configurationException);
		}

		String handlersURL =
			frontendJSPersonalizationConfiguration.handlersURL();
		String rulesURL = frontendJSPersonalizationConfiguration.rulesURL();

		if (Validator.isBlank(handlersURL) || Validator.isBlank(rulesURL)) {
			return;
		}

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.println(
			"<script data-senna-track=\"temporary\" type=\"module\">");
		printWriter.println(
			"import {personalization} from '@liferay/personalization';");
		printWriter.println("personalization.clear('PAGE');");
		printWriter.print("await personalization.runDetection('");
		printWriter.print(rulesURL);
		printWriter.println("');");
		printWriter.print("await import('");
		printWriter.print(handlersURL);
		printWriter.println("');");
		printWriter.println("await personalization.runHandlers();");
		printWriter.print("</script>");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register(
			"/html/common/themes/top_head.jsp#post");
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

}
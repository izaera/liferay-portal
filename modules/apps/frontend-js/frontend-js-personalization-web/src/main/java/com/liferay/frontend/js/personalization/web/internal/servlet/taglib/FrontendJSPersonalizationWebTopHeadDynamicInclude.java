/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.personalization.web.internal.servlet.taglib;

import com.liferay.client.extension.constants.ClientExtensionEntryConstants;
import com.liferay.client.extension.type.CET;
import com.liferay.client.extension.type.PersonalizationCET;
import com.liferay.client.extension.type.manager.CETManager;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

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

		List<CET> cets;

		try {
			cets = _cetManager.getCETs(
				themeDisplay.getCompanyId(), null,
				ClientExtensionEntryConstants.TYPE_PERSONALIZATION,
				Pagination.of(QueryUtil.ALL_POS, QueryUtil.ALL_POS), null);
		}
		catch (PortalException portalException) {
			throw new IOException(portalException);
		}

		if (cets.isEmpty()) {
			return;
		}

		PersonalizationCET personalizationCET = (PersonalizationCET)cets.get(0);

		if (Validator.isBlank(personalizationCET.getRulesURL())) {
			return;
		}

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.println(
			"<script data-senna-track=\"temporary\" type=\"module\">");
		printWriter.println(
			"import {personalization} from '@liferay/personalization';");
		printWriter.println("personalization.clear('PAGE');");
		printWriter.print("await personalization.runDetection('");
		printWriter.print(personalizationCET.getRulesURL());
		printWriter.println("');");
		printWriter.println(personalizationCET.getJavaScript());
		printWriter.println("await personalization.runHandlers();");
		printWriter.print("</script>");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register(
			"/html/common/themes/top_head.jsp#post");
	}

	@Reference
	private CETManager _cetManager;

}
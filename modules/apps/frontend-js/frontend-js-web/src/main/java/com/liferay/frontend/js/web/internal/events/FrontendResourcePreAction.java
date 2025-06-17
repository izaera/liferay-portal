/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.events;

import com.liferay.frontend.js.web.internal.hashed.files.HashedFilesRegistry;
import com.liferay.frontend.js.web.internal.hashed.files.HashedFilesUtil;
import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.LifecycleAction;
import com.liferay.portal.kernel.model.Theme;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	property = "key=servlet.service.events.pre", service = LifecycleAction.class
)
public class FrontendResourcePreAction extends Action {

	@Override
	public void run(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws ActionException {

		HashedFilesRegistry hashedFilesRegistry =
			HashedFilesRegistry.getHashedFilesRegistry();

		if (hashedFilesRegistry == null) {
			return;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (themeDisplay == null) {
			return;
		}

		Theme theme = themeDisplay.getTheme();

		boolean rtl = _portal.isRightToLeft(httpServletRequest);

		String contextPath = theme.getContextPath();

		String clayCSSFileName = rtl ? "/clay_rtl.css" : "/clay.css";

		String clayCSSHashedURI = hashedFilesRegistry.get(
			contextPath + "/__liferay__/internal/css" + clayCSSFileName);

		if (clayCSSHashedURI != null) {
			String hashedClayCSSFileName = HashedFilesUtil.addHash(
				clayCSSFileName, HashedFilesUtil.getHash(clayCSSHashedURI));

			themeDisplay.setDefaultClayCSSURL(
				themeDisplay.getPathThemeCss() + hashedClayCSSFileName);
		}

		String mainCSSFileName = rtl ? "/main_rtl.css" : "/main.css";

		String mainCSSHashedURI = hashedFilesRegistry.get(
			contextPath + "/__liferay__/internal/css" + mainCSSFileName);

		if (mainCSSHashedURI != null) {
			String hashedMainCSSHashedURI = HashedFilesUtil.addHash(
				mainCSSFileName, HashedFilesUtil.getHash(mainCSSHashedURI));

			themeDisplay.setDefaultMainCSSURL(
				themeDisplay.getPathThemeCss() + hashedMainCSSHashedURI);
		}
	}

	@Reference
	private Portal _portal;

}
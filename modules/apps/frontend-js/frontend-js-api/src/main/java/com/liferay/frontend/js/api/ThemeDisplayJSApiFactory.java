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

package com.liferay.frontend.js.api;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	property = {
		"domain=ThemeDisplay"
	}
)
public class ThemeDisplayJSApiFactory implements JSApiFactory<ThemeDisplayJSApi> {

	@Override
	public ThemeDisplayJSApi createInstance(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay) httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

		return new ThemeDisplayJSApi() {

			@Override
			public long getLayoutId() {
				return layout.getParentLayoutId();
			}

			@Override
			public String getLayoutURL() {
				try {
					return _portal.getLayoutURL(layout, themeDisplay);
				}
				catch (PortalException portalException) {
					throw new RuntimeException(portalException);
				}
			}

		};
	}

	@Reference
	private Portal _portal;

}

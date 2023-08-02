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

package com.liferay.cookies.banner.web.internal.servlet.taglib;

import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.aui.ScriptData;
import com.liferay.portal.kernel.servlet.taglib.aui.JSFragment;
import com.liferay.frontend.js.loader.modules.extender.esm.ESImportUtil;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilderFactory;
import com.liferay.portal.kernel.util.Portal;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;

/**
 * @author Bryce Osterhaus
 */
@Component(service = DynamicInclude.class)
public class EnableThirdPartyCookiesBottomJSDynamicInclude
	implements DynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		AbsolutePortalURLBuilder absolutePortalURLBuilder =
			_absolutePortalURLBuilderFactory.getAbsolutePortalURLBuilder(
				httpServletRequest);

		ScriptData scriptData = new ScriptData();

		scriptData.append(
			_portal.getPortletId(httpServletRequest),
			new JSFragment(
				"toggleThirdPartyCookies();",
				Arrays.asList(
					ESImportUtil.getESImport(
						absolutePortalURLBuilder,
						"{toggleThirdPartyCookies} from cookies-banner-web/index.js"
					)
				)
			)
		);

		scriptData.writeTo(httpServletResponse.getWriter());
	}

	@Override
	public void register(
		DynamicInclude.DynamicIncludeRegistry dynamicIncludeRegistry) {

		dynamicIncludeRegistry.register("/html/common/themes/bottom.jsp#post");
	}

	@Reference
	private NPMResolver _npmResolver;

	@Reference
	private Portal _portal;

		@Reference
	private AbsolutePortalURLBuilderFactory _absolutePortalURLBuilderFactory;

}
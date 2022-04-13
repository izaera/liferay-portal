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

package com.liferay.frontend.js.web.internal.servlet.taglib;

import com.liferay.frontend.js.web.internal.script.DeferredScriptsImpl;
import com.liferay.frontend.js.web.internal.script.DeferredScriptsManagerImpl;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = DynamicInclude.class)
public class DeferredScriptsBottomDynamicInclude implements DynamicInclude {

	@Override
	public void include(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, String key)
		throws IOException {

		DeferredScriptsImpl deferredScriptsImpl =
			_deferredScriptsManagerImpl.getDeferredScripts();

		if (deferredScriptsImpl != null) {
			PrintWriter printWriter = httpServletResponse.getWriter();

			for (String script : deferredScriptsImpl.getScripts()) {
				printWriter.print("<script type=\"module\">");
				printWriter.print(script);
				printWriter.print("</script>");
			}
		}
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/bottom.jsp#post");
	}

	@Reference
	private DeferredScriptsManagerImpl _deferredScriptsManagerImpl;

}
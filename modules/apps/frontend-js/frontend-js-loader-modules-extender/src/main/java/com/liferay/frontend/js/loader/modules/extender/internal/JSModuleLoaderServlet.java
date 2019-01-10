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

package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.ParamUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	configurationPid = "com.liferay.frontend.js.loader.modules.extender.internal.Details",
	immediate = true,
	property = {
		"osgi.http.whiteboard.servlet.name=com.liferay.frontend.js.loader.modules.extender.internal.JSModuleLoaderServlet",
		"osgi.http.whiteboard.servlet.pattern=/js_module_loader",
		"service.ranking:Integer=" + Details.MAX_VALUE_LESS_1K
	},
	service = {JSModuleLoaderServlet.class, Servlet.class}
)
public class JSModuleLoaderServlet extends HttpServlet {

	@Reference(unbind = "-")
	public void setJsModulesContextResolver(
		JSModulesContextResolver jsModulesContextResolver) {

		_jsModulesContextResolver = jsModulesContextResolver;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
		throws IOException {

		List<String> reqModules = _getRequestModules(req);

		JSModuleContext context = _jsModulesContextResolver.resolve(reqModules);

		StringWriter stringWriter = new StringWriter();

		PrintWriter printWriter = new PrintWriter(stringWriter);

		printWriter.write(_jsonFactory.looseSerializeDeep(context));

		printWriter.close();

		String content = stringWriter.toString();

		_writeResponse(resp, content);
	}

	private List<String> _getRequestModules(HttpServletRequest req) {
		String[] modulesParam = ParamUtil.getStringValues(req, "modules");

		if (modulesParam != null) {
			return Arrays.asList(modulesParam);
		}

		return Collections.emptyList();
	}

	private void _writeResponse(HttpServletResponse response, String content)
		throws IOException {

		response.setContentType(Details.CONTENT_TYPE);

		ServletOutputStream servletOutputStream = response.getOutputStream();

		PrintWriter printWriter = new PrintWriter(servletOutputStream, true);

		printWriter.write(content);

		printWriter.close();
	}

	@Reference
	private JSModulesContextResolver _jsModulesContextResolver;

	@Reference
	private JSONFactory _jsonFactory;

}
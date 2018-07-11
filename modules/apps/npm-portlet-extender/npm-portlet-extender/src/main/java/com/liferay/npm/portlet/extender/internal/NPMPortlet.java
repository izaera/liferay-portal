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

package com.liferay.npm.portlet.extender.internal;

import com.liferay.frontend.taglib.util.PortletScriptHandler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ray Augé
 * @author Iván Zaera Avellón
 */
public class NPMPortlet extends MVCPortlet {

	public NPMPortlet(String name, String version) {
		_name = name;
		_version = version;
	}

	@Override
	public void render(RenderRequest request, RenderResponse response) {
		try {
			PrintWriter writer = response.getWriter();

			String portletElementId = "npm-portlet-" + response.getNamespace();

			String contentHtml = StringUtil.replace(
				_HTML_TPL, new String[] {"$PORTLET_ELEMENT_ID"},
				new String[] {portletElementId});

			writer.write(contentHtml);

			StringBundler sb = new StringBundler(4);

			sb.append(_name);
			sb.append("@");
			sb.append(_version);
			sb.append(" as module");

			String componentJavaScript = StringUtil.replace(
				_JAVA_SCRIPT_TPL,
				new String[] {
					"$CONTEXT_PATH", "$PORTLET_ELEMENT_ID", "$PORTLET_NAMESPACE"
				},
				new String[] {
					request.getContextPath(), portletElementId,
					response.getNamespace()
				});

			_portletScriptHandler.outputJavascript(
				request, response, sb.toString(), componentJavaScript);

			writer.flush();
		}
		catch (IOException ioe) {
			_logger.error("Unable to render HTML output", ioe);
		}
	}

	private static String _loadTemplate(String name) {
		InputStream inputStream = NPMPortlet.class.getResourceAsStream(
			"dependencies/" + name);

		String template = StringPool.BLANK;

		try {
			template = StringUtil.read(inputStream);
		}
		catch (Exception e) {
			_logger.error("Unable to read template " + name, e);
		}

		return template;
	}

	private static final String _HTML_TPL;

	private static final String _JAVA_SCRIPT_TPL;

	private static final Logger _logger = LoggerFactory.getLogger(
		NPMPortlet.class);

	static {
		_JAVA_SCRIPT_TPL = _loadTemplate("bootstrap.js.tpl");
		_HTML_TPL = _loadTemplate("content.html.tpl");
	}

	private final String _name;
	private final PortletScriptHandler _portletScriptHandler =
		new PortletScriptHandler(NPMPortlet.class);
	private final String _version;

}
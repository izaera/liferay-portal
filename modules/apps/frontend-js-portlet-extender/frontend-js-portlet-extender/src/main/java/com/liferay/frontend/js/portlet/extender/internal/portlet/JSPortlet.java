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

package com.liferay.frontend.js.portlet.extender.internal.portlet;

import aQute.configurable.Configurable;
import com.liferay.frontend.js.portlet.extender.configuration.PortletExtenderConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Ray Augé
 * @author Iván Zaera Avellón
 */
public class JSPortlet extends MVCPortlet implements ManagedService {

	public JSPortlet(String name, String version, Dictionary<String, Object> configuration) {
		_name = name;
		_version = version;
		_configuration = configuration;

	}

	@Override
	public void render(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		try {

			PrintWriter printWriter = renderResponse.getWriter();

			String portletElementId =
				"js-portlet-" + renderResponse.getNamespace();

			printWriter.print(
				StringUtil.replace(
					_HTML_TPL, new String[] {"[$PORTLET_ELEMENT_ID$]"},
					new String[] {portletElementId}));

			printWriter.print(
				StringUtil.replace(
					_JAVA_SCRIPT_TPL,
					new String[] {
						"[$CONFIGURATION]", "[$CONTEXT_PATH$]",
						"[$PORTLET_ELEMENT_ID$]", "[$PORTLET_NAMESPACE$]",
						"[$PACKAGE_NAME$]", "[$PACKAGE_VERSION$]"
					},
					new String[] {
						_getConfiguration(), renderRequest.getContextPath(),
						portletElementId, renderResponse.getNamespace(), _name,
						_version
					}));

			printWriter.flush();
		}
		catch (IOException ioe) {
			_log.error("Unable to render HTML output", ioe);
		}
	}

	@Override
	public void updated(Dictionary<String, ?> properties)
		throws ConfigurationException {

//
//		if (properties == null) {
//			_configuration.set(Collections.emptyMap());
//
//			return;
//		}
//
//		Map<String, String> configuration = new HashMap<>();
//
//		Enumeration<String> keys = properties.keys();
//
//		while (keys.hasMoreElements()) {
//			String key = keys.nextElement();
//
//			if (key.equals("service.pid")) {
//				continue;
//			}
//
//			configuration.put(key, String.valueOf(properties.get(key)));
//		}
//
//		_configuration.set(configuration);
	}

	private static String _loadTemplate(String name) {
		InputStream inputStream = JSPortlet.class.getResourceAsStream(
			"dependencies/" + name);

		try {
			return StringUtil.read(inputStream);
		}
		catch (Exception e) {
			_log.error("Unable to read template " + name, e);
		}

		return StringPool.BLANK;
	}

	private String _escapeQuotes(String value) {
		return value.replaceAll("'", "\\'");
	}

	private String _getConfiguration() {
		Enumeration<String> keys = _configuration.keys();
		Enumeration<Object> values = _configuration.elements();

		StringBundler sb = new StringBundler();

		sb.append("{");
		String delimiter = "";

		while (keys.hasMoreElements()){
			String key = keys.nextElement();
			sb.append(delimiter);
			sb.append("'");
			sb.append(_escapeQuotes(key));
			sb.append("':'");
			sb.append(_escapeQuotes(values.nextElement().toString()));
			sb.append("'");

			delimiter = ", ";
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String _HTML_TPL;

	private static final String _JAVA_SCRIPT_TPL;

	private static final Log _log = LogFactoryUtil.getLog(JSPortlet.class);

	static {
		_HTML_TPL = _loadTemplate("bootstrap.html.tpl");
		_JAVA_SCRIPT_TPL = _loadTemplate("bootstrap.js.tpl");
	}

	private volatile PortletExtenderConfiguration _portletExtenderConfiguration;
	private final Dictionary<String, Object> _configuration;
	private final String _name;
	private final String _version;



}
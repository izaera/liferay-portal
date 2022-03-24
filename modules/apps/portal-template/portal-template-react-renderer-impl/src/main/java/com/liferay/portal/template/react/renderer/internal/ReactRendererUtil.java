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

package com.liferay.portal.template.react.renderer.internal;

import com.liferay.frontend.js.script.DeferredScripts;
import com.liferay.frontend.js.script.DeferredScriptsManager;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONSerializer;
import com.liferay.portal.kernel.servlet.taglib.aui.ScriptData;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.template.react.renderer.ComponentDescriptor;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilderFactory;

import java.io.IOException;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chema Balsas
 */
public class ReactRendererUtil {

	public static void renderReact(
			ComponentDescriptor componentDescriptor, Map<String, Object> props,
			AbsolutePortalURLBuilderFactory absolutePortalURLBuilder,
			DeferredScriptsManager deferredScriptsManager,
			HttpServletRequest httpServletRequest,
			String npmResolvedPackageName, Portal portal, Writer writer)
		throws IOException {

		String placeholderId = StringUtil.randomId();

		_renderPlaceholder(writer, placeholderId);

		String module = componentDescriptor.getModule();

		if (module.contains(" from ")) {
			_renderEcmaScript(
				componentDescriptor, props,
				absolutePortalURLBuilder.getAbsolutePortalURLBuilder(
					httpServletRequest),
				deferredScriptsManager, httpServletRequest, placeholderId,
				portal, writer);
		}
		else {
			_renderJavaScript(
				componentDescriptor, props, httpServletRequest,
				npmResolvedPackageName, placeholderId, portal, writer);
		}
	}

	private static String _getSymbolName(String importedSymbol) {
		importedSymbol = importedSymbol.trim();

		if ((importedSymbol.charAt(0) != CharPool.OPEN_CURLY_BRACE) ||
			(importedSymbol.charAt(importedSymbol.length() - 1) !=
				CharPool.CLOSE_CURLY_BRACE)) {

			throw new IllegalArgumentException(
				"Invalid import syntax: " + importedSymbol);
		}

		importedSymbol = importedSymbol.substring(
			1, importedSymbol.length() - 1);

		return importedSymbol.trim();
	}

	private static Map<String, Object> _prepareProps(
		ComponentDescriptor componentDescriptor, Map<String, Object> props,
		HttpServletRequest httpServletRequest, Portal portal) {

		Map<String, Object> modifiedProps = null;

		if (!props.containsKey("componentId")) {
			if (modifiedProps == null) {
				modifiedProps = new HashMap<>(props);
			}

			modifiedProps.put(
				"componentId", componentDescriptor.getComponentId());
		}

		if (!props.containsKey("locale")) {
			if (modifiedProps == null) {
				modifiedProps = new HashMap<>(props);
			}

			modifiedProps.put("locale", LocaleUtil.getMostRelevantLocale());
		}

		String portletId = (String)props.get("portletId");

		if (portletId == null) {
			if (modifiedProps == null) {
				modifiedProps = new HashMap<>(props);
			}

			portletId = portal.getPortletId(httpServletRequest);

			modifiedProps.put("portletId", portletId);
		}

		if ((portletId != null) && !props.containsKey("portletNamespace")) {
			if (modifiedProps == null) {
				modifiedProps = new HashMap<>(props);
			}

			modifiedProps.put(
				"portletNamespace", portal.getPortletNamespace(portletId));
		}

		if (modifiedProps == null) {
			return props;
		}

		return modifiedProps;
	}

	private static void _renderEcmaScript(
			ComponentDescriptor componentDescriptor, Map<String, Object> props,
			AbsolutePortalURLBuilder absolutePortalURLBuilder,
			DeferredScriptsManager deferredScriptsManager,
			HttpServletRequest httpServletRequest, String placeholderId,
			Portal portal, Writer writer)
		throws IOException {

		StringBundler javascriptSB = new StringBundler(21);

		javascriptSB.append("import {render} from '");
		javascriptSB.append(
			absolutePortalURLBuilder.forESModule(
				"portal-template-react-renderer-impl", "/index.js"
			).build());
		javascriptSB.append("';\n");

		String module = componentDescriptor.getModule();

		String[] parts = module.split(" from ");

		javascriptSB.append("import {");
		javascriptSB.append(_getSymbolName(parts[0]));
		javascriptSB.append(" as componentModule} from '");
		javascriptSB.append(
			absolutePortalURLBuilder.forESModule(
				parts[1], "/index.js"
			).build());
		javascriptSB.append("';\n");

		String propsTransformer = componentDescriptor.getPropsTransformer();

		boolean amdPropsTransformer = false;

		if (Validator.isNotNull(propsTransformer)) {
			if (propsTransformer.contains(" from ")) {
				parts = propsTransformer.split(" from ");

				javascriptSB.append("import {");
				javascriptSB.append(_getSymbolName(parts[0]));
				javascriptSB.append(" as propsTransformer} from '");
				javascriptSB.append(
					absolutePortalURLBuilder.forESModule(
						parts[1], "/index.js"
					).build());
				javascriptSB.append("';\n");
			}
			else {
				amdPropsTransformer = true;

				javascriptSB.append("Liferay.Loader.require('");
				javascriptSB.append(propsTransformer);
				javascriptSB.append(
					"', function({default: propsTransformer}) {\n");
			}
		}

		JSONSerializer jsonSerializer = JSONFactoryUtil.createJSONSerializer();

		javascriptSB.append("render(componentModule, ");

		if (Validator.isNotNull(propsTransformer)) {
			javascriptSB.append("propsTransformer(");
			javascriptSB.append(
				jsonSerializer.serializeDeep(
					_prepareProps(
						componentDescriptor, props, httpServletRequest,
						portal)));
			javascriptSB.append(")");
		}
		else {
			javascriptSB.append(
				jsonSerializer.serializeDeep(
					_prepareProps(
						componentDescriptor, props, httpServletRequest,
						portal)));
		}

		javascriptSB.append(", '");
		javascriptSB.append(placeholderId);
		javascriptSB.append("');\n");

		if (amdPropsTransformer) {
			javascriptSB.append("});\n");
		}

		if (componentDescriptor.isPositionInLine()) {
			writer.write("<script type=\"module\">\n");
			writer.write(javascriptSB.toString());
			writer.write("</script>\n");
		}
		else {
			DeferredScripts deferredScripts =
				deferredScriptsManager.getDeferredScripts();

			deferredScripts.addScript(javascriptSB.toString());
		}
	}

	private static void _renderJavaScript(
			ComponentDescriptor componentDescriptor, Map<String, Object> props,
			HttpServletRequest httpServletRequest,
			String npmResolvedPackageName, String placeholderId, Portal portal,
			Writer writer)
		throws IOException {

		StringBundler dependenciesSB = new StringBundler(11);

		dependenciesSB.append(npmResolvedPackageName);
		dependenciesSB.append(" as index");
		dependenciesSB.append(placeholderId);
		dependenciesSB.append(", ");
		dependenciesSB.append(componentDescriptor.getModule());
		dependenciesSB.append(" as renderFunction");
		dependenciesSB.append(placeholderId);

		String propsTransformer = componentDescriptor.getPropsTransformer();

		if (Validator.isNotNull(propsTransformer)) {
			dependenciesSB.append(", ");
			dependenciesSB.append(propsTransformer);
			dependenciesSB.append(" as propsTransformer");
			dependenciesSB.append(placeholderId);
		}

		JSONSerializer jsonSerializer = JSONFactoryUtil.createJSONSerializer();

		StringBundler javascriptSB = new StringBundler(13);

		javascriptSB.append("index");
		javascriptSB.append(placeholderId);
		javascriptSB.append(".render(renderFunction");
		javascriptSB.append(placeholderId);
		javascriptSB.append(".default, ");

		if (Validator.isNotNull(propsTransformer)) {
			javascriptSB.append("propsTransformer");
			javascriptSB.append(placeholderId);
			javascriptSB.append(".default(");
			javascriptSB.append(
				jsonSerializer.serializeDeep(
					_prepareProps(
						componentDescriptor, props, httpServletRequest,
						portal)));
			javascriptSB.append(")");
		}
		else {
			javascriptSB.append(
				jsonSerializer.serializeDeep(
					_prepareProps(
						componentDescriptor, props, httpServletRequest,
						portal)));
		}

		javascriptSB.append(", '");
		javascriptSB.append(placeholderId);
		javascriptSB.append("');");

		if (componentDescriptor.isPositionInLine()) {
			ScriptData scriptData = new ScriptData();

			scriptData.append(
				portal.getPortletId(httpServletRequest),
				javascriptSB.toString(), dependenciesSB.toString(),
				ScriptData.ModulesType.ES6);

			scriptData.writeTo(writer);
		}
		else {
			ScriptData scriptData = (ScriptData)httpServletRequest.getAttribute(
				WebKeys.AUI_SCRIPT_DATA);

			if (scriptData == null) {
				scriptData = new ScriptData();

				httpServletRequest.setAttribute(
					WebKeys.AUI_SCRIPT_DATA, scriptData);
			}

			scriptData.append(
				portal.getPortletId(httpServletRequest),
				javascriptSB.toString(), dependenciesSB.toString(),
				ScriptData.ModulesType.ES6);
		}
	}

	private static void _renderPlaceholder(Writer writer, String placeholderId)
		throws IOException {

		writer.append("<div id=\"");
		writer.append(placeholderId);
		writer.append("\"></div>");
	}

}
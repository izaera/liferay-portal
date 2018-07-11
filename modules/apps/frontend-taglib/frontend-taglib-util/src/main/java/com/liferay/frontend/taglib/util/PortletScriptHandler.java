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

package com.liferay.frontend.taglib.util;

import com.liferay.frontend.taglib.util.internal.PartialRequestUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.servlet.taglib.aui.ScriptData;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class PortletScriptHandler {

	public PortletScriptHandler(Class<?> portletClass) {
		_log = LogFactoryUtil.getLog(portletClass);
	}

	public void outputJavascript(
		RenderRequest renderRequest, RenderResponse renderResponse,
		String require, String script) {

		HttpServletRequest request = PortalUtil.getHttpServletRequest(
			renderRequest);

		Portlet portlet = (Portlet)request.getAttribute(WebKeys.RENDER_PORTLET);

		if (portlet == null) {
			throw new IllegalArgumentException(
				"Unable to get portlet from given request");
		}

		try {
			PrintWriter writer = renderResponse.getWriter();

			if (PartialRequestUtil.isPartialRequest(request)) {
				ScriptData scriptData = new ScriptData();

				scriptData.append(
					portlet.getPortletId(), script, require,
					ScriptData.ModulesType.ES6);

				scriptData.writeTo(writer);
			}
			else {
				ScriptData scriptData = (ScriptData)renderRequest.getAttribute(
					WebKeys.AUI_SCRIPT_DATA);

				if (scriptData == null) {
					scriptData = new ScriptData();

					renderRequest.setAttribute(
						WebKeys.AUI_SCRIPT_DATA, scriptData);
				}

				scriptData.append(
					portlet.getPortletId(), script, require,
					ScriptData.ModulesType.ES6);
			}
		}
		catch (IOException ioe) {
			_log.error("Unable to output script", ioe);
		}
	}

	private final Log _log;

}
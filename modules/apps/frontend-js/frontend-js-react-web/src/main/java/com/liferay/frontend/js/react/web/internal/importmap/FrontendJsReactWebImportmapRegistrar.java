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

package com.liferay.frontend.js.react.web.internal.importmap;

import com.liferay.frontend.js.importmap.extender.JSImportmapRegistry;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;

import javax.servlet.ServletContext;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = {})
public class FrontendJsReactWebImportmapRegistrar {

	@Activate
	protected void activate() {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		String contextPath = _servletContext.getContextPath();

		jsonObject.put(
			"react", contextPath + "/__liferay__/amd2esm/react.js"
		).put(
			"react-dom", contextPath + "/__liferay__/amd2esm/react-dom.js"
		);

		_jsImportmapRegistry.register(jsonObject);
	}

	@Reference
	private JSImportmapRegistry _jsImportmapRegistry;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.frontend.js.react.web)",
		unbind = "-"
	)
	private ServletContext _servletContext;

}
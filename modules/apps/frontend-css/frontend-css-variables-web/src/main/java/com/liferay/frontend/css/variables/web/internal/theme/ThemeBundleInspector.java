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

package com.liferay.frontend.css.variables.web.internal.theme;

import com.liferay.portal.kernel.util.StringUtil;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Iván Zaera Avellón
 */
public class ThemeBundleInspector {

	public ThemeBundleInspector(Bundle bundle) {
		_bundle = bundle;
	}

	public boolean isTheme() {
		URL url = _bundle.getResource("WEB-INF/liferay-look-and-feel.xml");

		if (url == null) {
			return false;
		}

		try(InputStream inputStream = url.openStream()) {
			String xml = StringUtil.read(inputStream);

			if(xml.contains("</theme>")) {

			}
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}


	private final Bundle _bundle;

}

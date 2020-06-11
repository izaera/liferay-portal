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

package com.liferay.layout.admin.web.internal.css.variables;

import com.liferay.frontend.css.variables.ScopedCSSVariables;

import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class LayoutScopedCSSVariables implements ScopedCSSVariables {

	public LayoutScopedCSSVariables(Map<String, String> cssVariables) {
		_cssVariables = cssVariables;
	}

	@Override
	public Map<String, String> getCSSVariables() {
		return _cssVariables;
	}

	@Override
	public String getScope() {
		return ":root";
	}

	private final Map<String, String> _cssVariables;

}
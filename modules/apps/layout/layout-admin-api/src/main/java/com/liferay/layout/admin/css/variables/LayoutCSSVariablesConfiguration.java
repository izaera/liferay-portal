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

package com.liferay.layout.admin.css.variables;

import com.liferay.portal.kernel.model.Theme;

import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public interface LayoutCSSVariablesConfiguration {

	public Map<String, String> getCSSVariables(Theme theme, long companyId);

	public void setCSSVariables(
		Theme theme, long companyId, Map<String, String> cssVariables);

}
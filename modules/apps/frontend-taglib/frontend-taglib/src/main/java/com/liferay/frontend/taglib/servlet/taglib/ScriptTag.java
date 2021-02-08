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

package com.liferay.frontend.taglib.servlet.taglib;

import com.liferay.frontend.js.module.launcher.JSModuleDependency;
import com.liferay.frontend.js.module.launcher.JSModuleLauncher;
import com.liferay.frontend.taglib.internal.util.ServicesProvider;
import com.liferay.petra.string.StringPool;
import com.liferay.taglib.BaseBodyTagSupport;

import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Iván Zaera Avellón
 */
public class ScriptTag extends BaseBodyTagSupport {

	@Override
	public int doEndTag() throws JspException {
		JSModuleLauncher jsModuleLauncher =
			ServicesProvider.getJSModuleLauncher();

		jsModuleLauncher.writeScript(
			pageContext.getOut(), _getJSModuleDependencies(_require),
			getBodyContent().getString());

		return super.doEndTag();
	}

	private Collection<JSModuleDependency> _getJSModuleDependencies(
		String require) {

		List<JSModuleDependency> jsModuleDependencies = new ArrayList<>();
		
		String[] parts = require.split(StringPool.COMMA);

		for (String part : parts) {
			String[] moduleVariableNames = part.split(" as ");

			if (moduleVariableNames.length != 2) {
				throw new IllegalArgumentException(
					"Invalid require syntax: " + part);
			}

			jsModuleDependencies.add(
				new JSModuleDependency(
					moduleVariableNames[0], moduleVariableNames[1]));
		}

		return jsModuleDependencies;
	}

	public void setRequire(String require) {
		_require = require;
	}
	
	private String _require;

}
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

package com.liferay.frontend.js.loader.modules.extender.internal.servlet.filter;

import com.liferay.petra.string.StringPool;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	property = {
		"servlet-context-name=",
		"servlet-filter-name=Frontend JS Loader Modules Extender Filter",
		"url-pattern=*.json"
	},
	service = Filter.class
)
public class FrontendJsLoaderModulesExtenderFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(
			ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain)
		throws IOException, ServletException {

		if (!(servletRequest instanceof HttpServletRequest)) {
			filterChain.doFilter(servletRequest, servletResponse);

			return;
		}

		HttpServletRequest httpServletRequest =
			(HttpServletRequest)servletRequest;

		String uri = httpServletRequest.getRequestURI();

		if (!uri.startsWith("/o/")) {
			filterChain.doFilter(servletRequest, servletResponse);

			return;
		}

		int thirdSlashIndex = uri.indexOf(StringPool.SLASH, 3);

		int lastSlashIndex = uri.lastIndexOf(StringPool.SLASH);

		if ((uri.indexOf("/package.json", lastSlashIndex) == -1) &&
			(uri.indexOf("/manifest.json", lastSlashIndex) == -1)) {

			filterChain.doFilter(servletRequest, servletResponse);

			return;
		}

		if (thirdSlashIndex != lastSlashIndex) {
			filterChain.doFilter(servletRequest, servletResponse);

			return;
		}

		HttpServletResponse httpServletResponse =
			(HttpServletResponse)servletResponse;

		httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

}
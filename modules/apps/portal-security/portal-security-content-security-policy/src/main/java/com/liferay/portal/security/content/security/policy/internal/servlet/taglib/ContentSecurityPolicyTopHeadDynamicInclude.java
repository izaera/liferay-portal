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

package com.liferay.portal.security.content.security.policy.internal.servlet.taglib;

import com.liferay.portal.kernel.security.csp.CSPNonceProvider;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	service = DynamicInclude.class
)
public class ContentSecurityPolicyTopHeadDynamicInclude
	implements DynamicInclude {

	@Override
	public void include(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String key)
		throws IOException {

		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.print("<script data-senna-track=\"permanent\" nonce=\"");

		String nonce = _cspNonceProvider.getCSPNonce(httpServletRequest);

		printWriter.print(nonce);

		printWriter.print("\" type=\"text/javascript\">window.Liferay = window.Liferay || ");
		printWriter.print("{};window.Liferay.CSP={nonce:'");
		printWriter.print(nonce);
		printWriter.println("'};</script>");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	@Reference
	private CSPNonceProvider _cspNonceProvider;

}

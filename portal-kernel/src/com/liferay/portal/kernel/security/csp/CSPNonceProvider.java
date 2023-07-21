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

package com.liferay.portal.kernel.security.csp;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public interface CSPNonceProvider {

	/**
	 * Get the nonce associated to a request.
	 *
	 * If the request is not available a fallback ThreadLocal managed by the
	 * ContentSecurityPolicyFilter is used instead.
	 *
	 * It is strongly discouraged to make the provider use the ThreadLocal if
	 * the request can be obtained by any mean in the calling code.
	 *
	 * @param httpServletRequest the current request or null if not available
	 * @return the nonce or "" if CSP is not active
	 * @review
	 */
	public String getCSPNonce(HttpServletRequest httpServletRequest);

}

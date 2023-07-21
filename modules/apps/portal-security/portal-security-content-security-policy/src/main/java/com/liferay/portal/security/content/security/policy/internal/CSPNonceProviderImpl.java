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

package com.liferay.portal.security.content.security.policy.internal;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.SecureRandom;
import com.liferay.portal.kernel.security.csp.CSPNonceProvider;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.util.PropsValues;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	service = {CSPNonceProvider.class, CSPNonceProviderImpl.class }
)
public class CSPNonceProviderImpl implements CSPNonceProvider {

	private static final String _NONCE_ATTR =
		CSPNonceProviderImpl.class.getName() + ":nonce";

	public String ensureNonce(HttpServletRequest httpServletRequest) {
		// Unwrap request as some wrappers hide attributes from request/session

		httpServletRequest = _portal.getOriginalServletRequest(
			httpServletRequest);

		String nonce;

		if (PropsValues.JAVASCRIPT_SINGLE_PAGE_APPLICATION_ENABLED) {
			HttpSession httpSession = httpServletRequest.getSession();

			nonce = (String)httpSession.getAttribute(_NONCE_ATTR);

			if (nonce == null) {
				synchronized (httpSession) {
					nonce = (String)httpSession.getAttribute(_NONCE_ATTR);

					if (nonce == null) {
						nonce = _generateNonce();

						httpSession.setAttribute(_NONCE_ATTR, nonce);
					}
				}
			}
		}
		else {
			nonce = (String)httpServletRequest.getAttribute(_NONCE_ATTR);

			if (nonce == null) {
				nonce = _generateNonce();

				httpServletRequest.setAttribute(_NONCE_ATTR, nonce);
			}
		}

		System.err.println("NONCE: " + nonce + " FOR: " + httpServletRequest.getRequestURI());

		return nonce;
	}

	@Override
	public String getCSPNonce(HttpServletRequest httpServletRequest) {
		// Unwrap request as some wrappers hide attributes from request/session

		httpServletRequest = _portal.getOriginalServletRequest(
			httpServletRequest);

		String nonce;

		if (httpServletRequest == null) {
			nonce =  _threadLocal.get();
		}
		else if (PropsValues.JAVASCRIPT_SINGLE_PAGE_APPLICATION_ENABLED) {
			HttpSession httpSession = httpServletRequest.getSession();

			nonce = (String)httpSession.getAttribute(_NONCE_ATTR);
		}
		else {
			nonce = (String)httpServletRequest.getAttribute(_NONCE_ATTR);
		}

		if (nonce == null) {
			nonce = StringPool.BLANK;
		}

		return nonce;
	}

	public void removeTLSNonce() {
		_threadLocal.remove();
	}

	public void setTLSNonce(String nonce) {
		_threadLocal.set(nonce);
	}

	private String _generateNonce() {
		SecureRandom secureRandom = new SecureRandom();

		byte[] bytes = new byte[16];

		secureRandom.nextBytes(bytes);

		return Base64.encode(bytes);
	}

	@Reference
	private Portal _portal;

	private ThreadLocal<String> _threadLocal = new ThreadLocal<>();

}

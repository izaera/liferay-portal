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

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import org.osgi.util.tracker.ServiceTracker;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class CSPNonceProviderUtil {

	/**
	 * @see CSPNonceProvider#getCSPNonce(HttpServletRequest)
	 */
	public static String getCSPNonce(HttpServletRequest httpServletRequest) {
		CSPNonceProvider cspNonceProvider = getCSPNonceProvider();

		if (cspNonceProvider == null) {
			_log.error("Using empty CSP nonce because provider is not present");

			return StringPool.BLANK;
		}

		return cspNonceProvider.getCSPNonce(httpServletRequest);
	}

	public static CSPNonceProvider getCSPNonceProvider() {
		return _serviceTracker.getService();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CSPNonceProviderUtil.class);

	private static final ServiceTracker<CSPNonceProvider, CSPNonceProvider>
		_serviceTracker = new ServiceTracker<>(
			SystemBundleUtil.getBundleContext(), CSPNonceProvider.class, null);

	static {
		_serviceTracker.open();
	}

}

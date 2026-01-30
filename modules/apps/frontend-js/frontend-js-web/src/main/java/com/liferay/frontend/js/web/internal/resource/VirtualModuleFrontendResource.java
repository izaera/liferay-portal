/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.resource;

import com.liferay.portal.kernel.util.ContentTypes;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

/**
 * @author Iván Zaera Avellón
 */
public class VirtualModuleFrontendResource implements FrontendResource {

	public VirtualModuleFrontendResource(
		String eTag, boolean immutable, long maxAge, boolean sendNoCache,
		URL url) {

		_eTag = eTag;
		_immutable = immutable;
		_maxAge = maxAge;
		_sendNoCache = sendNoCache;
		_url = url;
	}

	@Override
	public String getContentType() {
		return ContentTypes.APPLICATION_JAVASCRIPT;
	}

	@Override
	public String getETag() {
		return _eTag;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return _url.openStream();
	}

	@Override
	public long getMaxAge() {
		return _maxAge;
	}

	@Override
	public boolean isImmutable() {
		return _immutable;
	}

	@Override
	public boolean isPrivate() {
		return false;
	}

	@Override
	public boolean isSendNoCache() {
		return _sendNoCache;
	}

	private final String _eTag;
	private final boolean _immutable;
	private final long _maxAge;
	private final boolean _sendNoCache;
	private final URL _url;

}
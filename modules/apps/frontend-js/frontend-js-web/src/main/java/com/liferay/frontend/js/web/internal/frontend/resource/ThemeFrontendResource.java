/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.frontend.resource;

import com.liferay.portal.kernel.util.ContentTypes;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

/**
 * @author Iván Zaera Avellón
 */
public class ThemeFrontendResource implements FrontendResource {

	public ThemeFrontendResource(String eTag, URL url) {
		_eTag = eTag;
		_url = url;
	}

	@Override
	public String getContentType() {
		return ContentTypes.TEXT_CSS;
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
		return 31536000L;
	}

	@Override
	public boolean isImmutable() {
		return true;
	}

	@Override
	public boolean isSendNoCache() {
		return false;
	}

	private final String _eTag;
	private final URL _url;

}
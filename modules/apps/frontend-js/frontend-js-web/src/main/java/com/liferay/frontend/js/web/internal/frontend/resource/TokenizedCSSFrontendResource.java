/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.frontend.resource;

import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.util.ContentTypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class TokenizedCSSFrontendResource implements FrontendResource {

	public TokenizedCSSFrontendResource(
		String eTag, boolean immutable, long maxAge, boolean sendNoCache,
		Map<String, String> tokens, URL url) {

		_eTag = eTag;
		_immutable = immutable;
		_maxAge = maxAge;
		_sendNoCache = sendNoCache;
		_tokens = tokens;
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
		String content = StreamUtil.toString(_url.openStream());

		for (Map.Entry<String, String> entry : _tokens.entrySet()) {
			content = StringUtil.replace(
				content, entry.getKey(), entry.getValue());
		}

		return new ByteArrayInputStream(
			content.getBytes(StandardCharsets.UTF_8));
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
	public boolean isSendNoCache() {
		return _sendNoCache;
	}

	private final String _eTag;
	private final boolean _immutable;
	private final long _maxAge;
	private final boolean _sendNoCache;
	private final Map<String, String> _tokens;
	private final URL _url;

}
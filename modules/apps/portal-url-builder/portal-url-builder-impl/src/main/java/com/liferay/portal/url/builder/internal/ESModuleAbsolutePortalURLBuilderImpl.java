/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.url.builder.internal;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.url.builder.ESModuleAbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.internal.util.URLUtil;

/**
 * @author Iván Zaera Avellón
 */
public class ESModuleAbsolutePortalURLBuilderImpl
	implements ESModuleAbsolutePortalURLBuilder {

	public ESModuleAbsolutePortalURLBuilderImpl(
		String cdnHost, String esModulePath, String pathModule,
		String pathProxy, String webContextHash, String webContextPath) {

		String resourcePath = "/__liferay__/" + esModulePath;

		if (!resourcePath.startsWith(StringPool.SLASH)) {
			resourcePath = StringPool.SLASH + resourcePath;
		}

		if (!webContextPath.startsWith(StringPool.SLASH)) {
			webContextPath = StringPool.SLASH + webContextPath;
		}

		_cdnHost = cdnHost;
		_pathModule = pathModule;
		_pathProxy = pathProxy;
		_webContextHash = webContextHash;
		_webContextPath = webContextPath;

		_resourcePath = resourcePath;
	}

	@Override
	public String build() {
		String pathModuleSuffix = _webContextPath;

		if (_webContextHash != null) {
			pathModuleSuffix = StringBundler.concat(
				"/js/-", _webContextPath, StringPool.OPEN_PARENTHESIS,
				_webContextHash, StringPool.CLOSE_PARENTHESIS);
		}

		StringBundler sb = new StringBundler();

		URLUtil.appendURL(
			sb, _cdnHost, _ignoreCDNHost, _ignorePathProxy,
			_pathModule + pathModuleSuffix, _pathProxy, _resourcePath);

		return sb.toString();
	}

	@Override
	public ESModuleAbsolutePortalURLBuilder ignoreCDNHost() {
		_ignoreCDNHost = true;

		return this;
	}

	@Override
	public ESModuleAbsolutePortalURLBuilder ignorePathProxy() {
		_ignorePathProxy = true;

		return this;
	}

	private final String _cdnHost;
	private boolean _ignoreCDNHost;
	private boolean _ignorePathProxy;
	private final String _pathModule;
	private final String _pathProxy;
	private final String _resourcePath;
	private final String _webContextHash;
	private final String _webContextPath;

}
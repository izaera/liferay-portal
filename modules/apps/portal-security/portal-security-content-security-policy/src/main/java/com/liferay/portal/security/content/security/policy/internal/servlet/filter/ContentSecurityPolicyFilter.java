/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.content.security.policy.internal.servlet.filter;

import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.content.security.policy.internal.ContentSecurityPolicyNonceManager;
import com.liferay.portal.security.content.security.policy.internal.configuration.ContentSecurityPolicyConfiguration;
import com.liferay.portal.security.content.security.policy.internal.configuration.ContentSecurityPolicyConfigurationUtil;
import com.liferay.portal.servlet.filters.BasePortalFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Olivér Kecskeméty
 */
@Component(
	property = {
		"after-filter=Portal CORS Servlet Filter", "dispatcher=FORWARD",
		"dispatcher=REQUEST", "servlet-context-name=",
		"servlet-filter-name=Content Security Policy Filter", "url-pattern=/*"
	},
	service = Filter.class
)
public class ContentSecurityPolicyFilter extends BasePortalFilter {

	@Override
	public boolean isFilterEnabled(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {

		if (CompanyThreadLocal.getCompanyId() == 0) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Content security policy will not be applied to this " +
						"request for company ID 0");
			}

			return false;
		}

		ContentSecurityPolicyConfiguration contentSecurityPolicyConfiguration =
			ContentSecurityPolicyConfigurationUtil.
				setContentSecurityPolicyConfiguration(
					_configurationProvider, httpServletRequest, _portal);

		if (!contentSecurityPolicyConfiguration.enabled() ||
			Validator.isNull(contentSecurityPolicyConfiguration.policy()) ||
			_isExcludedURIPath(
				contentSecurityPolicyConfiguration, httpServletRequest)) {

			return false;
		}

		return true;
	}

	@Override
	protected void processFilter(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse, FilterChain filterChain)
		throws Exception {

		String nonce = _contentSecurityPolicyNonceManager.setNonce(
			httpServletRequest);

		try {
			httpServletResponse.setContentType("text/html; charset=UTF-8");

			ContentSecurityPolicyConfiguration
				contentSecurityPolicyConfiguration =
					ContentSecurityPolicyConfigurationUtil.
						getContentSecurityPolicyConfiguration(
							httpServletRequest);

			String policy = contentSecurityPolicyConfiguration.policy();

			policy = StringUtil.replace(policy, "[$NONCE$]", "nonce-" + nonce);

			httpServletResponse.setHeader("Content-Security-Policy", policy);

			filterChain.doFilter(httpServletRequest, httpServletResponse);
		}
		finally {
			_contentSecurityPolicyNonceManager.cleanUpNonce(httpServletRequest);
		}
	}

	private boolean _isExcludedURIPath(
		ContentSecurityPolicyConfiguration contentSecurityPolicyConfiguration,
		HttpServletRequest httpServletRequest) {

		String requestURI = httpServletRequest.getRequestURI();

		if (Validator.isNull(requestURI)) {
			return false;
		}

		for (String internallyExcludedPath : _INTERNALLY_EXCLUDED_PATHS) {
			if (Validator.isNotNull(internallyExcludedPath) &&
				requestURI.startsWith(
					StringUtil.toLowerCase(internallyExcludedPath))) {

				return true;
			}
		}

		requestURI = StringUtil.toLowerCase(requestURI);

		for (String excludedPath :
				contentSecurityPolicyConfiguration.excludedPaths()) {

			if (Validator.isNotNull(excludedPath) &&
				requestURI.startsWith(StringUtil.toLowerCase(excludedPath))) {

				return true;
			}
		}

		return false;
	}

	private static final String[] _INTERNALLY_EXCLUDED_PATHS = {
		"/group/", "/user/", "/web/"
	};

	private static final Log _log = LogFactoryUtil.getLog(
		ContentSecurityPolicyFilter.class);

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private ContentSecurityPolicyNonceManager
		_contentSecurityPolicyNonceManager;

	@Reference
	private Portal _portal;

}
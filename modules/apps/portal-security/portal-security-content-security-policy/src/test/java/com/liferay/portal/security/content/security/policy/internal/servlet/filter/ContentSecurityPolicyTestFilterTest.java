/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.content.security.policy.internal.servlet.filter;

import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Iván Zaera Avellón
 */
public class ContentSecurityPolicyTestFilterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testForbiddenAttributes() {
		ContentSecurityPolicyTestFilter contentSecurityPolicyTestFilter =
			new ContentSecurityPolicyTestFilter();

		for (String forbiddenAttribute :
				ContentSecurityPolicyTestFilter.FORBIDDEN_ATTRIBUTES) {

			_assertCheckContentDoesNotPass(
				"<br><div " + forbiddenAttribute + "='...'>hi</div><br>",
				contentSecurityPolicyTestFilter,
				"checkContent passed for forbidden attribute " +
					forbiddenAttribute);
		}
	}

	@Test
	public void testLinkRelStylesheetTag() {
		ContentSecurityPolicyTestFilter contentSecurityPolicyTestFilter =
			new ContentSecurityPolicyTestFilter();

		_assertCheckContentDoesNotPass(
			"<div>hi</div><link rel='stylesheet' href='styles.css'><br>",
			contentSecurityPolicyTestFilter,
			"checkContent passed for link tag without nonce");

		contentSecurityPolicyTestFilter.checkContent(
			"<div>hi</div><link nonce='" + _NONCE +
				"' rel='stylesheet' href='styles.css'><br>",
			_NONCE);
	}

	@Test
	public void testScriptTag() {
		ContentSecurityPolicyTestFilter contentSecurityPolicyTestFilter =
			new ContentSecurityPolicyTestFilter();

		_assertCheckContentDoesNotPass(
			"<br><script>alert(location.href)</script><br>",
			contentSecurityPolicyTestFilter,
			"checkContent passed for script tag without nonce");

		contentSecurityPolicyTestFilter.checkContent(
			"<br><script nonce='" + _NONCE +
				"'>alert(location.href)</script><br>",
			_NONCE);
	}

	@Test
	public void testStyleTag() {
		ContentSecurityPolicyTestFilter contentSecurityPolicyTestFilter =
			new ContentSecurityPolicyTestFilter();

		_assertCheckContentDoesNotPass(
			"<div>hi</div><style>.x {color: red;}</style>",
			contentSecurityPolicyTestFilter,
			"checkContent passed for style tag without nonce");

		contentSecurityPolicyTestFilter.checkContent(
			"<div>hi</div><style nonce='" + _NONCE +
				"'>.x {color: red;}</style>",
			_NONCE);
	}

	private void _assertCheckContentDoesNotPass(
		String content,
		ContentSecurityPolicyTestFilter contentSecurityPolicyTestFilter,
		String failMessage) {

		try {
			contentSecurityPolicyTestFilter.checkContent(content, _NONCE);

			Assert.fail(failMessage);
		}
		catch (IllegalStateException illegalStateException) {
		}
	}

	private static final String _NONCE = "@nonce@";

}
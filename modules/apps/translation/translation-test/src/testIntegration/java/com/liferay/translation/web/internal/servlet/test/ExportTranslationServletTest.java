/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.translation.web.internal.servlet.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.journal.constants.JournalFolderConstants;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.test.util.JournalTestUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.servlet.HttpMethods;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.translation.exception.XLIFFFileException;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Balázs Sáfrány-Kovalik
 */
@RunWith(Arquillian.class)
public class ExportTranslationServletTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_journalArticle = JournalTestUtil.addArticle(
			_group.getGroupId(),
			JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);
	}

	@Test(expected = XLIFFFileException.MustBeSupportedLanguage.class)
	@TestInfo("LPD-85963")
	public void testDoGetFailsWithInvalidSourceLanguageId() throws Throwable {
		_doGet(
			_createHttpServletRequest(
				_INVALID_LANGUAGE_ID, new String[] {"es_ES"}));
	}

	@Test(expected = XLIFFFileException.MustBeSupportedLanguage.class)
	@TestInfo("LPD-85963")
	public void testDoGetFailsWithInvalidTargetLanguageId() throws Throwable {
		_doGet(
			_createHttpServletRequest(
				"en_US", new String[] {_INVALID_LANGUAGE_ID}));
	}

	private HttpServletRequest _createHttpServletRequest(
			String sourceLanguageId, String[] targetLanguageIds)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			WebKeys.USER, TestPropsValues.getUser());
		mockHttpServletRequest.setMethod(HttpMethods.GET);
		mockHttpServletRequest.setParameter(
			"classNameId",
			String.valueOf(
				_portal.getClassNameId(JournalArticle.class.getName())));
		mockHttpServletRequest.setParameter(
			"groupId", String.valueOf(_group.getGroupId()));
		mockHttpServletRequest.setParameter(
			"key", String.valueOf(_journalArticle.getResourcePrimKey()));
		mockHttpServletRequest.setParameter(
			"sourceLanguageId", sourceLanguageId);
		mockHttpServletRequest.setParameter(
			"targetLanguageIds", targetLanguageIds);

		return mockHttpServletRequest;
	}

	private void _doGet(HttpServletRequest httpServletRequest)
		throws Throwable {

		try {
			_servlet.service(
				httpServletRequest, new MockHttpServletResponse());
		}
		catch (IOException ioException) {
			throw ioException.getCause();
		}
	}

	private static final String _INVALID_LANGUAGE_ID = "xx_XX";

	@DeleteAfterTestRun
	private Group _group;

	private JournalArticle _journalArticle;

	@Inject
	private Portal _portal;

	@Inject(
		filter = "osgi.http.whiteboard.servlet.name=com.liferay.translation.web.internal.servlet.ExportTranslationServlet"
	)
	private Servlet _servlet;

}

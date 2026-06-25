/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.style.book.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.style.book.exception.DuplicateStyleBookTokenSetNameException;
import com.liferay.style.book.exception.StyleBookTokenSetFrontendTokenCategoryNameException;
import com.liferay.style.book.exception.StyleBookTokenSetNameException;
import com.liferay.style.book.model.StyleBookEntry;
import com.liferay.style.book.model.StyleBookTokenSet;
import com.liferay.style.book.service.StyleBookEntryLocalService;
import com.liferay.style.book.service.StyleBookTokenSetLocalService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gabriel Lima
 */
@RunWith(Arquillian.class)
public class StyleBookTokenSetLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group, TestPropsValues.getUserId());

		_styleBookEntry = _styleBookEntryLocalService.addStyleBookEntry(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			_group.getGroupId(), false, null, RandomTestUtil.randomString(),
			null, RandomTestUtil.randomString(), _serviceContext);
	}

	@Test
	public void testAddStyleBookTokenSet() throws Exception {
		AssertUtils.assertFailure(
			StyleBookTokenSetNameException.class, "Name is null",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, StringPool.BLANK,
				_THEME_ID_CLASSIC));
		AssertUtils.assertFailure(
			StyleBookTokenSetNameException.class, "Name is null",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, null, _THEME_ID_CLASSIC));
		AssertUtils.assertFailure(
			StyleBookTokenSetNameException.class,
			"Name exceeds the maximum length",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, RandomTestUtil.randomString(76),
				_THEME_ID_CLASSIC));

		String frontendTokenCategoryName = RandomTestUtil.randomString();

		AssertUtils.assertFailure(
			StyleBookTokenSetFrontendTokenCategoryNameException.class,
			"Frontend token category \"" + frontendTokenCategoryName +
				"\" does not exist",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				frontendTokenCategoryName, RandomTestUtil.randomString(),
				_THEME_ID_CLASSIC));

		AssertUtils.assertFailure(
			DuplicateStyleBookTokenSetNameException.class,
			"Style book token set name \"brandColors\" already exists",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, "brandColors",
				_THEME_ID_CLASSIC));

		String description = RandomTestUtil.randomString();
		String externalReferenceCode = RandomTestUtil.randomString();
		String name = RandomTestUtil.randomString();

		StyleBookTokenSet styleBookTokenSet =
			_styleBookTokenSetLocalService.addStyleBookTokenSet(
				externalReferenceCode, TestPropsValues.getUserId(),
				_styleBookEntry.getStyleBookEntryId(), description,
				_FRONTEND_TOKEN_CATEGORY_NAME, name, _THEME_ID_CLASSIC);

		Assert.assertEquals(description, styleBookTokenSet.getDescription());
		Assert.assertEquals(
			externalReferenceCode,
			styleBookTokenSet.getExternalReferenceCode());
		Assert.assertEquals(
			_FRONTEND_TOKEN_CATEGORY_NAME,
			styleBookTokenSet.getFrontendTokenCategoryName());
		Assert.assertEquals(
			_group.getGroupId(), styleBookTokenSet.getGroupId());
		Assert.assertEquals(name, styleBookTokenSet.getName());
		Assert.assertEquals(
			_styleBookEntry.getStyleBookEntryId(),
			styleBookTokenSet.getStyleBookEntryId());
		Assert.assertEquals(_THEME_ID_CLASSIC, styleBookTokenSet.getThemeId());
		Assert.assertEquals(
			TestPropsValues.getUserId(), styleBookTokenSet.getUserId());

		AssertUtils.assertFailure(
			DuplicateStyleBookTokenSetNameException.class,
			"Style book token set name \"" + name + "\" already exists",
			() -> _addStyleBookTokenSet(
				_styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, name, _THEME_ID_CLASSIC));

		StyleBookTokenSet cmsThemeStyleBookTokenSet = _addStyleBookTokenSet(
			_styleBookEntry.getStyleBookEntryId(),
			_FRONTEND_TOKEN_CATEGORY_NAME, name, "cms_WAR_cmstheme");

		Assert.assertNotEquals(
			styleBookTokenSet.getStyleBookTokenSetId(),
			cmsThemeStyleBookTokenSet.getStyleBookTokenSetId());

		StyleBookTokenSet spacingStyleBookTokenSet = _addStyleBookTokenSet(
			_styleBookEntry.getStyleBookEntryId(), "spacing", name,
			_THEME_ID_CLASSIC);

		Assert.assertNotEquals(
			styleBookTokenSet.getStyleBookTokenSetId(),
			spacingStyleBookTokenSet.getStyleBookTokenSetId());

		StyleBookEntry styleBookEntry =
			_styleBookEntryLocalService.addStyleBookEntry(
				RandomTestUtil.randomString(), TestPropsValues.getUserId(),
				_group.getGroupId(), false, null, RandomTestUtil.randomString(),
				null, RandomTestUtil.randomString(), _serviceContext);

		StyleBookTokenSet styleBookEntryStyleBookTokenSet =
			_addStyleBookTokenSet(
				styleBookEntry.getStyleBookEntryId(),
				_FRONTEND_TOKEN_CATEGORY_NAME, name, _THEME_ID_CLASSIC);

		Assert.assertNotEquals(
			styleBookTokenSet.getStyleBookTokenSetId(),
			styleBookEntryStyleBookTokenSet.getStyleBookTokenSetId());
	}

	private StyleBookTokenSet _addStyleBookTokenSet(
			long styleBookEntryId, String frontendTokenCategoryName,
			String name, String themeId)
		throws Exception {

		return _styleBookTokenSetLocalService.addStyleBookTokenSet(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			styleBookEntryId, RandomTestUtil.randomString(),
			frontendTokenCategoryName, name, themeId);
	}

	private static final String _FRONTEND_TOKEN_CATEGORY_NAME = "colorSystem";

	private static final String _THEME_ID_CLASSIC = "classic_WAR_classictheme";

	@DeleteAfterTestRun
	private Group _group;

	private ServiceContext _serviceContext;
	private StyleBookEntry _styleBookEntry;

	@Inject
	private StyleBookEntryLocalService _styleBookEntryLocalService;

	@Inject
	private StyleBookTokenSetLocalService _styleBookTokenSetLocalService;

}
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.constants.FragmentConstants;
import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.service.FragmentEntryLinkLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Javier Moral
 */
@RunWith(Arquillian.class)
public class FragmentEntryLinkModelListenerTest {

	public static final String RANDOM_STRING = RandomTestUtil.randomString();

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_serviceContext = ServiceContextTestUtil.getServiceContext(
			TestPropsValues.getGroupId());
	}

	@Test
	public void testAddFragmentEntryLinkWithEscapedTextField()
		throws Exception {

		String editableValues = _createEditableValues(
			"element-text",
			HtmlUtil.escape("<script>alert('xss');</script>Heading Example"));

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				"BASIC_COMPONENT-heading"),
			editableValues, _serviceContext);

		Assert.assertEquals(
			editableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testAddFragmentEntryLinkWithHtmlField() throws Exception {
		String editableValues = _createEditableValues(
			"element-html", "<script>alert('xss');</script>HTML Example");

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				"BASIC_COMPONENT-html"),
			editableValues, _serviceContext);

		Assert.assertEquals(
			editableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testAddFragmentEntryLinkWithMappedTextField() throws Exception {
		long classPK = RandomTestUtil.randomLong();
		String editableFieldValue =
			"<script>alert('xss');</script>Heading Example";

		String editableValues = JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			JSONUtil.put(
				"element-text",
				JSONUtil.put(
					"className", JournalArticle.class.getName()
				).put(
					"classNameId", _portal.getClassNameId(JournalArticle.class)
				).put(
					"classPK", classPK
				).put(
					"config", StringPool.BLANK
				).put(
					"defaultValue", editableFieldValue
				).put(
					"fieldId", "JournalArticle_title"
				).put(
					"itemType", "Web Content Article"
				))
		).toString();

		String expectedEditableValues = JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			JSONUtil.put(
				"element-text",
				JSONUtil.put(
					"className", JournalArticle.class.getName()
				).put(
					"classNameId", _portal.getClassNameId(JournalArticle.class)
				).put(
					"classPK", classPK
				).put(
					"config", StringPool.BLANK
				).put(
					"defaultValue", HtmlUtil.escape(editableFieldValue)
				).put(
					"fieldId", "JournalArticle_title"
				).put(
					"itemType", "Web Content Article"
				))
		).toString();

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				"BASIC_COMPONENT-heading"),
			editableValues, _serviceContext);

		Assert.assertEquals(
			expectedEditableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testAddFragmentEntryLinkWithRichTextField() throws Exception {
		String editableFieldValue = "<script>alert('xss');</script>Example";

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				"BASIC_COMPONENT-paragraph"),
			_createEditableValues("element-text", editableFieldValue),
			_serviceContext);

		Assert.assertEquals(
			_createEditableValues("element-text", "Example"),
			fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testAddFragmentEntryLinkWithTextField() throws Exception {
		String editableFieldValue =
			"<script>alert('xss');</script>Heading Example";

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				"BASIC_COMPONENT-heading"),
			_createEditableValues("element-text", editableFieldValue),
			_serviceContext);

		Assert.assertEquals(
			_createEditableValues(
				"element-text", HtmlUtil.escape(editableFieldValue)),
			fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testUpdateFragmentEntryLinkWithEscapedTextField()
		throws Exception {

		String editableValues = _createEditableValues(
			"element-text",
			HtmlUtil.escape("<script>alert('xss');</script>Heading Example"));

		FragmentEntryLink fragmentEntryLink = _getUpdatedFragmentEntryLink(
			"BASIC_COMPONENT-heading", editableValues);

		Assert.assertEquals(
			editableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testUpdateFragmentEntryLinkWithHTMLField() throws Exception {
		String editableValues = _createEditableValues(
			"element-html", "<script>alert('xss');</script>HTML Example");

		FragmentEntryLink fragmentEntryLink = _getUpdatedFragmentEntryLink(
			"BASIC_COMPONENT-html", editableValues);

		Assert.assertEquals(
			editableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testUpdateFragmentEntryLinkWithMappedTextField()
		throws Exception {

		long classPK = RandomTestUtil.randomLong();

		String editableFieldValue =
			"<script>alert('xss');</script>Heading Example";

		String editableValues = JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			JSONUtil.put(
				"element-text",
				JSONUtil.put(
					"className", JournalArticle.class.getName()
				).put(
					"classNameId", _portal.getClassNameId(JournalArticle.class)
				).put(
					"classPK", classPK
				).put(
					"config", StringPool.BLANK
				).put(
					"defaultValue", editableFieldValue
				).put(
					"fieldId", "JournalArticle_title"
				).put(
					"itemType", "Web Content Article"
				))
		).toString();

		String expectedEditableValues = JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			JSONUtil.put(
				"element-text",
				JSONUtil.put(
					"className", JournalArticle.class.getName()
				).put(
					"classNameId", _portal.getClassNameId(JournalArticle.class)
				).put(
					"classPK", classPK
				).put(
					"config", StringPool.BLANK
				).put(
					"defaultValue", HtmlUtil.escape(editableFieldValue)
				).put(
					"fieldId", "JournalArticle_title"
				).put(
					"itemType", "Web Content Article"
				))
		).toString();

		FragmentEntryLink fragmentEntryLink = _getUpdatedFragmentEntryLink(
			"BASIC_COMPONENT-heading", editableValues);

		Assert.assertEquals(
			expectedEditableValues, fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testUpdateFragmentEntryLinkWithRichTextField()
		throws Exception {

		FragmentEntryLink fragmentEntryLink = _getUpdatedFragmentEntryLink(
			"BASIC_COMPONENT-paragraph",
			_createEditableValues(
				"element-text", "<script>alert('xss');</script>Example"));

		Assert.assertEquals(
			_createEditableValues("element-text", "Example"),
			fragmentEntryLink.getEditableValues());
	}

	@Test
	public void testUpdateFragmentEntryLinkWithTextField() throws Exception {
		String editableValue = "<script>alert('xss');</script>Example";

		FragmentEntryLink fragmentEntryLink = _getUpdatedFragmentEntryLink(
			"BASIC_COMPONENT-heading",
			_createEditableValues("element-text", editableValue));

		Assert.assertEquals(
			_createEditableValues(
				"element-text", HtmlUtil.escape(editableValue)),
			fragmentEntryLink.getEditableValues());
	}

	private FragmentEntryLink _addFragmentEntryLink(
			FragmentEntry fragmentEntry, String editableValues,
			ServiceContext serviceContext)
		throws Exception {

		return _fragmentEntryLinkLocalService.addFragmentEntryLink(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			TestPropsValues.getGroupId(), 0, 0, 0, 0, fragmentEntry.getCss(),
			fragmentEntry.getHtml(), fragmentEntry.getJs(),
			fragmentEntry.getConfiguration(), editableValues,
			RandomTestUtil.randomString(), 0, RandomTestUtil.randomString(),
			FragmentConstants.TYPE_COMPONENT, serviceContext);
	}

	private String _createEditableValues(String key, String value) {
		return JSONUtil.put(
			FragmentEntryProcessorConstants.
				KEY_EDITABLE_FRAGMENT_ENTRY_PROCESSOR,
			JSONUtil.put(
				key,
				JSONUtil.put(
					"config",
					JSONUtil.put(
						"href", JSONUtil.put("en_US", RANDOM_STRING)
					).put(
						"mapperType", "link"
					).put(
						"target", ""
					)
				).put(
					"defaultValue", value
				).put(
					"en_US", value
				))
		).toString();
	}

	private FragmentEntryLink _getUpdatedFragmentEntryLink(
			String fragmentEntryKey, String editableValues)
		throws Exception {

		FragmentEntryLink fragmentEntryLink = _addFragmentEntryLink(
			_fragmentCollectionContributorRegistry.getFragmentEntry(
				fragmentEntryKey),
			StringPool.BLANK, _serviceContext);

		return _fragmentEntryLinkLocalService.updateFragmentEntryLink(
			TestPropsValues.getUserId(),
			fragmentEntryLink.getFragmentEntryLinkId(), editableValues);
	}

	private void _pushServiceContext(
			FragmentEntryLink fragmentEntryLink, Layout layout)
		throws Exception {

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLayout(layout);
		themeDisplay.setLookAndFeel(layout.getTheme(), layout.getColorScheme());
		themeDisplay.setMainCSSURL("http://test.com");
		themeDisplay.setMainJSURL("http://test.com");
		themeDisplay.setPermissionChecker(
			PermissionCheckerFactoryUtil.create(TestPropsValues.getUser()));
		themeDisplay.setPlid(layout.getPlid());
		themeDisplay.setRealUser(TestPropsValues.getUser());
		themeDisplay.setUser(TestPropsValues.getUser());

		MockLiferayPortletActionRequest mockLiferayPortletActionRequest =
			new MockLiferayPortletActionRequest();

		mockLiferayPortletActionRequest.setAttribute(
			JavaConstants.JAKARTA_PORTLET_RESPONSE,
			new MockLiferayPortletActionResponse());
		mockLiferayPortletActionRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(fragmentEntryLink.getCompanyId());
		serviceContext.setRequest(
			PortalUtil.getHttpServletRequest(mockLiferayPortletActionRequest));
		serviceContext.setScopeGroupId(TestPropsValues.getGroupId());
		serviceContext.setUserId(TestPropsValues.getUserId());

		ServiceContextThreadLocal.pushServiceContext(serviceContext);
	}

	@Inject
	private CompanyLocalService _companyLocalService;

	private Layout _draftLayout;

	@Inject
	private FragmentCollectionContributorRegistry
		_fragmentCollectionContributorRegistry;

	@Inject
	private FragmentEntryLinkLocalService _fragmentEntryLinkLocalService;

	@Inject
	private Portal _portal;

	private ServiceContext _serviceContext;

}
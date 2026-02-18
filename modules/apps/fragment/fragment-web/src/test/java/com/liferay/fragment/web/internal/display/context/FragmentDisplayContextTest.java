/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.web.internal.display.context;

import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.service.FragmentCollectionLocalServiceUtil;
import com.liferay.portal.kernel.test.TestInfo;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Bárbara Cabrera
 */
public class FragmentDisplayContextTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_setUpHttpServletRequest();
	}

	@Test
	@TestInfo("LPD-79101")
	public void testGetFragmentCollectionFromDifferentGroup() {
		FragmentCollection fragmentCollection = Mockito.mock(
			FragmentCollection.class);

		Mockito.when(
			fragmentCollection.getGroupId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		long fragmentCollectionId = RandomTestUtil.randomLong();

		Mockito.when(
			_httpServletRequest.getParameter("fragmentCollectionId")
		).thenReturn(
			String.valueOf(fragmentCollectionId)
		);

		Mockito.when(
			_httpServletRequest.getParameter("fragmentCollectionKey")
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_themeDisplay.getScopeGroupId()
		).thenReturn(
			RandomTestUtil.randomLong()
		);

		try (MockedStatic<FragmentCollectionLocalServiceUtil>
				fragmentCollectionLocalServiceUtilMockedStatic =
					Mockito.mockStatic(
						FragmentCollectionLocalServiceUtil.class)) {

			fragmentCollectionLocalServiceUtilMockedStatic.when(
				() ->
					FragmentCollectionLocalServiceUtil.fetchFragmentCollection(
						fragmentCollectionId)
			).thenReturn(
				fragmentCollection
			);

			FragmentDisplayContext fragmentDisplayContext =
				new FragmentDisplayContext(
					_httpServletRequest, _renderRequest, _renderResponse);

			Assert.assertNull(fragmentDisplayContext.getFragmentCollection());
		}
	}

	@Test
	@TestInfo("LPD-79101")
	public void testGetFragmentCollectionFromSameGroup() {
		long fragmentCollectionId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		FragmentCollection fragmentCollection = Mockito.mock(
			FragmentCollection.class);

		Mockito.when(
			fragmentCollection.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_httpServletRequest.getParameter("fragmentCollectionId")
		).thenReturn(
			String.valueOf(fragmentCollectionId)
		);

		Mockito.when(
			_httpServletRequest.getParameter("fragmentCollectionKey")
		).thenReturn(
			RandomTestUtil.randomString()
		);

		Mockito.when(
			_themeDisplay.getScopeGroupId()
		).thenReturn(
			groupId
		);

		try (MockedStatic<FragmentCollectionLocalServiceUtil>
				fragmentCollectionLocalServiceUtilMockedStatic =
					Mockito.mockStatic(
						FragmentCollectionLocalServiceUtil.class)) {

			fragmentCollectionLocalServiceUtilMockedStatic.when(
				() ->
					FragmentCollectionLocalServiceUtil.fetchFragmentCollection(
						fragmentCollectionId)
			).thenReturn(
				fragmentCollection
			);

			FragmentDisplayContext fragmentDisplayContext =
				new FragmentDisplayContext(
					_httpServletRequest, _renderRequest, _renderResponse);

			Assert.assertNotNull(
				fragmentDisplayContext.getFragmentCollection());
		}
	}

	private void _setUpHttpServletRequest() {
		Mockito.when(
			_httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			_themeDisplay
		);
	}

	private final HttpServletRequest _httpServletRequest = Mockito.mock(
		HttpServletRequest.class);
	private final RenderRequest _renderRequest = Mockito.mock(
		RenderRequest.class);
	private final RenderResponse _renderResponse = Mockito.mock(
		RenderResponse.class);
	private final ThemeDisplay _themeDisplay = Mockito.mock(ThemeDisplay.class);

}
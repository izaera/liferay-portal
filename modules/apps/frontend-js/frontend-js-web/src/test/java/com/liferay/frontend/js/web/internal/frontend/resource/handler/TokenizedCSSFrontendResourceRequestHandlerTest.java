/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.frontend.resource.handler;

import com.liferay.frontend.js.web.internal.configuration.FrontendCachingConfiguration;
import com.liferay.frontend.js.web.internal.frontend.resource.FrontendResource;
import com.liferay.frontend.js.web.test.util.FrontendJSWebTestUtil;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.io.StreamUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.service.ThemeLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Iván Zaera Avellón
 */
public class TokenizedCSSFrontendResourceRequestHandlerTest {

	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_hashedFilePath = StringUtil.replace(
			_UNHASHED_FILE_PATH, ".js", ".(" + _HASH + ").js");
	}

	@Test
	public void testCanHandleRequest() throws Exception {
		TokenizedCSSFrontendResourceRequestHandler
			tokenizedCSSFrontendResourceRequestHandler =
				new TokenizedCSSFrontendResourceRequestHandler(
					_mockConfigurationProvider(
						RandomTestUtil.randomLong(), false),
					_mockHashedFilesRegistry(), _mockPortal(),
					_mockServiceTrackerMap(
						_mockServletContext(_hashedFilePath)),
					_mockThemeLocalService());

		Assert.assertTrue(
			tokenizedCSSFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH)));

		Assert.assertTrue(
			tokenizedCSSFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _hashedFilePath)));

		Assert.assertFalse(
			tokenizedCSSFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest("/nonsense/request/index.js")));
	}

	@Test
	public void testHandleRequestWithHash() throws Exception {
		TokenizedCSSFrontendResourceRequestHandler
			tokenizedCSSFrontendResourceRequestHandler =
				new TokenizedCSSFrontendResourceRequestHandler(
					_mockConfigurationProvider(
						RandomTestUtil.randomLong(), false),
					_mockHashedFilesRegistry(), _mockPortal(),
					_mockServiceTrackerMap(
						_mockServletContext(_hashedFilePath)),
					_mockThemeLocalService());

		FrontendResource frontendResource =
			tokenizedCSSFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _hashedFilePath));

		Assert.assertEquals(
			ContentTypes.TEXT_JAVASCRIPT, frontendResource.getContentType());
		Assert.assertEquals(_HASH, frontendResource.getETag());
		Assert.assertEquals(
			"export default x;",
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(31536000L, frontendResource.getMaxAge());
		Assert.assertTrue(frontendResource.isImmutable());
		Assert.assertFalse(frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithNoConfiguration() throws Exception {
		TokenizedCSSFrontendResourceRequestHandler
			tokenizedCSSFrontendResourceRequestHandler =
				new TokenizedCSSFrontendResourceRequestHandler(
					Mockito.mock(ConfigurationProvider.class),
					_mockHashedFilesRegistry(), _mockPortal(),
					_mockServiceTrackerMap(
						_mockServletContext(_hashedFilePath)),
					_mockThemeLocalService());

		FrontendResource frontendResource =
			tokenizedCSSFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH));

		Assert.assertEquals(86400, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithoutHashForHashedFile() throws Exception {
		long maxAge = RandomTestUtil.randomLong();

		TokenizedCSSFrontendResourceRequestHandler
			tokenizedCSSFrontendResourceRequestHandler =
				new TokenizedCSSFrontendResourceRequestHandler(
					_mockConfigurationProvider(maxAge, false),
					_mockHashedFilesRegistry(), _mockPortal(),
					_mockServiceTrackerMap(
						_mockServletContext(_hashedFilePath)),
					_mockThemeLocalService());

		FrontendResource frontendResource =
			tokenizedCSSFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH));

		Assert.assertEquals(
			ContentTypes.TEXT_JAVASCRIPT, frontendResource.getContentType());
		Assert.assertEquals(_HASH, frontendResource.getETag());
		Assert.assertEquals(
			"export default x;",
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(maxAge, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isImmutable());
		Assert.assertFalse(frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithoutHashForUnhashedFile() throws Exception {
		long maxAge = RandomTestUtil.randomLong();

		TokenizedCSSFrontendResourceRequestHandler
			tokenizedCSSFrontendResourceRequestHandler =
				new TokenizedCSSFrontendResourceRequestHandler(
					_mockConfigurationProvider(maxAge, false),
					_mockHashedFilesRegistry(), _mockPortal(),
					_mockServiceTrackerMap(
						_mockServletContext(_hashedFilePath)),
					_mockThemeLocalService());

		FrontendResource frontendResource =
			tokenizedCSSFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH));

		Assert.assertEquals(
			ContentTypes.TEXT_JAVASCRIPT, frontendResource.getContentType());
		Assert.assertNull(frontendResource.getETag());
		Assert.assertEquals(
			"export default x;",
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(maxAge, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isImmutable());
		Assert.assertFalse(frontendResource.isSendNoCache());
	}

	private ConfigurationProvider _mockConfigurationProvider(
			long maxAge, boolean sendNoCache)
		throws Exception {

		ConfigurationProvider configurationProvider = Mockito.mock(
			ConfigurationProvider.class);

		FrontendCachingConfiguration frontendCachingConfiguration =
			Mockito.mock(FrontendCachingConfiguration.class);

		Mockito.when(
			frontendCachingConfiguration.labelsModulesMaxAge()
		).thenReturn(
			maxAge
		);

		Mockito.when(
			frontendCachingConfiguration.sendNoCacheForLabelsModules()
		).thenReturn(
			sendNoCache
		);

		Mockito.when(
			configurationProvider.getCompanyConfiguration(
				FrontendCachingConfiguration.class, _COMPANY_ID)
		).thenReturn(
			frontendCachingConfiguration
		);

		return configurationProvider;
	}

	private HashedFilesRegistry _mockHashedFilesRegistry() {
		HashedFilesRegistry hashedFilesRegistry = Mockito.mock(
			HashedFilesRegistry.class);

		Mockito.when(
			hashedFilesRegistry.get(
				Mockito.eq("/o/frontend-js-web" + _UNHASHED_FILE_PATH))
		).thenReturn(
			"/o/frontend-js-web" + _hashedFilePath
		);

		return hashedFilesRegistry;
	}

	private MockHttpServletRequest _mockHttpServletRequest(String requestURI) {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setRequestURI(requestURI);

		return mockHttpServletRequest;
	}

	private Portal _mockPortal() {
		Portal portal = Mockito.mock(Portal.class);

		Mockito.when(
			portal.getCompanyId(Mockito.any(HttpServletRequest.class))
		).thenReturn(
			_COMPANY_ID
		);

		return portal;
	}

	private ServiceTrackerMap<String, ServletContext> _mockServiceTrackerMap(
		ServletContext servletContext) {

		ServiceTrackerMap<String, ServletContext> serviceTrackerMap =
			Mockito.mock(ServiceTrackerMap.class);

		Mockito.when(
			serviceTrackerMap.getService("/o/frontend-js-web")
		).thenReturn(
			servletContext
		);

		return serviceTrackerMap;
	}

	private ServletContext _mockServletContext(String resourcePath)
		throws Exception {

		ServletContext servletContext = Mockito.mock(ServletContext.class);

		URL url = Mockito.mock(URL.class);

		Mockito.when(
			url.openStream()
		).thenReturn(
			new ByteArrayInputStream(
				"export default x;".getBytes(StandardCharsets.UTF_8))
		);

		Mockito.when(
			servletContext.getResource(resourcePath)
		).thenReturn(
			url
		);

		return servletContext;
	}

	private ThemeLocalService _mockThemeLocalService() {
		ThemeLocalService themeLocalService = Mockito.mock(
			ThemeLocalService.class);

		return themeLocalService;
	}

	private static final long _COMPANY_ID = 1L;

	private static final String _HASH =
		FrontendJSWebTestUtil.randomHashedFileHash();

	private static final String _UNHASHED_FILE_PATH = "/__liferay__/index.js";

	private String _hashedFilePath;

}
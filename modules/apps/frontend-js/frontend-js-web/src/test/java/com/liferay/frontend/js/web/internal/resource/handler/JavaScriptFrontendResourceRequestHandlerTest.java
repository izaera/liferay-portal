/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.web.internal.resource.handler;

import com.liferay.frontend.js.web.internal.configuration.FrontendCachingConfiguration;
import com.liferay.frontend.js.web.internal.resource.FrontendResource;
import com.liferay.frontend.js.web.test.util.FrontendJSWebTestUtil;
import com.liferay.petra.io.StreamUtil;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.portlet.PortletConfigFactory;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.language.LanguageResources;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;

import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.util.ResourceBundle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class JavaScriptFrontendResourceRequestHandlerTest {

	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_hashedFilePath = StringUtil.replace(
			_UNHASHED_FILE_PATH, ".js", ".(" + _HASH + ").js");
	}

	@After
	public void tearDown() {
		if (_languageResourcesMockedStatic != null) {
			_languageResourcesMockedStatic.close();

			_languageResourcesMockedStatic = null;
		}
	}

	@Test
	public void testCanHandleRequest() throws Exception {
		JavaScriptFrontendResourceRequestHandler
			javaScriptFrontendResourceRequestHandler =
				new JavaScriptFrontendResourceRequestHandler(
					_mockFrontendCachingConfiguration(86400, false),
					_mockHashedFilesRegistry(true, false), _mockLanguage(),
					Mockito.mock(PortletConfigFactory.class));

		Assert.assertFalse(
			javaScriptFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest("/nonsense/request/main.js", false)));
		Assert.assertTrue(
			javaScriptFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH, false)));
		Assert.assertTrue(
			javaScriptFrontendResourceRequestHandler.canHandleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _hashedFilePath, false)));
	}

	@Test
	public void testHandleRequestForTranslatableFile() throws Exception {
		_mockLanguageResources();

		long maxAge = RandomTestUtil.randomLong();
		boolean sendNoCache = true;

		JavaScriptFrontendResourceRequestHandler
			javaScriptFrontendResourceRequestHandler =
				new JavaScriptFrontendResourceRequestHandler(
					_mockFrontendCachingConfiguration(maxAge, sendNoCache),
					_mockHashedFilesRegistry(true, true), _mockLanguage(),
					Mockito.mock(PortletConfigFactory.class));

		FrontendResource frontendResource =
			javaScriptFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _hashedFilePath, true));

		Assert.assertEquals(
			ContentTypes.APPLICATION_JAVASCRIPT,
			frontendResource.getContentType());
		Assert.assertNull(frontendResource.getETag());
		Assert.assertEquals(
			StringUtil.replace(
				_TRANSLATABLE_JS_CONTENT, "Liferay.Language.get('portlet')",
				"'Portlet'"),
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(maxAge, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isImmutable());
		Assert.assertEquals(sendNoCache, frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithHash() throws Exception {
		JavaScriptFrontendResourceRequestHandler
			javaScriptFrontendResourceRequestHandler =
				new JavaScriptFrontendResourceRequestHandler(
					_mockFrontendCachingConfiguration(86400, true),
					_mockHashedFilesRegistry(true, false), _mockLanguage(),
					Mockito.mock(PortletConfigFactory.class));

		FrontendResource frontendResource =
			javaScriptFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _hashedFilePath, false));

		Assert.assertEquals(
			ContentTypes.APPLICATION_JAVASCRIPT,
			frontendResource.getContentType());
		Assert.assertEquals(_HASH, frontendResource.getETag());
		Assert.assertEquals(
			_JS_CONTENT,
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(31536000L, frontendResource.getMaxAge());
		Assert.assertTrue(frontendResource.isImmutable());
		Assert.assertFalse(frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithoutHashForHashedFile() throws Exception {
		long maxAge = RandomTestUtil.randomLong();
		boolean sendNoCache = true;

		JavaScriptFrontendResourceRequestHandler
			javaScriptFrontendResourceRequestHandler =
				new JavaScriptFrontendResourceRequestHandler(
					_mockFrontendCachingConfiguration(maxAge, sendNoCache),
					_mockHashedFilesRegistry(true, false), _mockLanguage(),
					Mockito.mock(PortletConfigFactory.class));

		FrontendResource frontendResource =
			javaScriptFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH, false));

		Assert.assertEquals(
			ContentTypes.APPLICATION_JAVASCRIPT,
			frontendResource.getContentType());
		Assert.assertEquals(_HASH, frontendResource.getETag());
		Assert.assertEquals(
			_JS_CONTENT,
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(maxAge, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isImmutable());
		Assert.assertEquals(sendNoCache, frontendResource.isSendNoCache());
	}

	@Test
	public void testHandleRequestWithoutHashForUnhashedFile() throws Exception {
		long maxAge = RandomTestUtil.randomLong();
		boolean sendNoCache = true;

		JavaScriptFrontendResourceRequestHandler
			javaScriptFrontendResourceRequestHandler =
				new JavaScriptFrontendResourceRequestHandler(
					_mockFrontendCachingConfiguration(maxAge, sendNoCache),
					_mockHashedFilesRegistry(false, false), _mockLanguage(),
					Mockito.mock(PortletConfigFactory.class));

		FrontendResource frontendResource =
			javaScriptFrontendResourceRequestHandler.handleRequest(
				_mockHttpServletRequest(
					"/o/frontend-js-web" + _UNHASHED_FILE_PATH, false));

		Assert.assertEquals(
			ContentTypes.APPLICATION_JAVASCRIPT,
			frontendResource.getContentType());
		Assert.assertNull(frontendResource.getETag());
		Assert.assertEquals(
			_JS_CONTENT,
			StreamUtil.toString(frontendResource.getInputStream()));
		Assert.assertEquals(maxAge, frontendResource.getMaxAge());
		Assert.assertFalse(frontendResource.isImmutable());
		Assert.assertEquals(sendNoCache, frontendResource.isSendNoCache());
	}

	private FrontendCachingConfiguration _mockFrontendCachingConfiguration(
		long esModulesMaxAge, boolean sendNoCacheForESModules) {

		FrontendCachingConfiguration frontendCachingConfiguration =
			Mockito.mock(FrontendCachingConfiguration.class);

		Mockito.when(
			frontendCachingConfiguration.esModulesMaxAge()
		).thenReturn(
			esModulesMaxAge
		);

		Mockito.when(
			frontendCachingConfiguration.sendNoCacheForESModules()
		).thenReturn(
			sendNoCacheForESModules
		);

		return frontendCachingConfiguration;
	}

	private HashedFilesRegistry _mockHashedFilesRegistry(
			boolean hashedFile, boolean translatableFile)
		throws Exception {

		HashedFilesRegistry hashedFilesRegistry = Mockito.mock(
			HashedFilesRegistry.class);

		if (hashedFile) {
			Mockito.when(
				hashedFilesRegistry.getHashedFileURI(
					Mockito.eq("/o/frontend-js-web" + _UNHASHED_FILE_PATH))
			).thenReturn(
				"/o/frontend-js-web" + _hashedFilePath
			);
		}

		URL url = Mockito.mock(URL.class);

		String content =
			translatableFile ? _TRANSLATABLE_JS_CONTENT : _JS_CONTENT;

		Mockito.when(
			url.openStream()
		).thenReturn(
			new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))
		);

		Mockito.when(
			hashedFilesRegistry.getResource(
				Mockito.eq(
					"/o/frontend-js-web" +
						(hashedFile ? _hashedFilePath : _UNHASHED_FILE_PATH)))
		).thenReturn(
			url
		);

		return hashedFilesRegistry;
	}

	private HttpServletRequest _mockHttpServletRequest(
		String requestURI, boolean translatableRequest) {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setRequestURI(requestURI);

		if (translatableRequest) {
			mockHttpServletRequest.addParameter("languageId", "en_US");
			mockHttpServletRequest.addParameter("translate", "true");
		}

		return mockHttpServletRequest;
	}

	private Language _mockLanguage() {
		Language language = Mockito.mock(Language.class);

		Mockito.when(
			language.process(Mockito.any(), Mockito.any(), Mockito.any())
		).thenAnswer(
			(Answer<String>)invocationOnMock -> {
				String content = invocationOnMock.getArgument(2);

				return StringUtil.replace(
					content, "Liferay.Language.get('portlet')", "'Portlet'");
			}
		);

		return language;
	}

	private void _mockLanguageResources() {
		_languageResourcesMockedStatic = Mockito.mockStatic(
			LanguageResources.class);

		ResourceBundle resourceBundle = Mockito.mock(ResourceBundle.class);

		_languageResourcesMockedStatic.when(
			() -> LanguageResources.getResourceBundle(Mockito.any())
		).thenReturn(
			resourceBundle
		);
	}

	private static final String _HASH =
		FrontendJSWebTestUtil.randomHashedFileHash();

	private static final String _JS_CONTENT = "function x(){return 'Portlet';}";

	private static final String _TRANSLATABLE_JS_CONTENT =
		"function x(){return Liferay.Language.get('portlet');}";

	private static final String _UNHASHED_FILE_PATH = "/js/main.js";

	private String _hashedFilePath;
	private MockedStatic<LanguageResources> _languageResourcesMockedStatic;

}
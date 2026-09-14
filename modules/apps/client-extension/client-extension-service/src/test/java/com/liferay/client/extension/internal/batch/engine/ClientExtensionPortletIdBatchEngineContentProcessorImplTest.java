/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.client.extension.internal.batch.engine;

import com.liferay.batch.engine.BatchEngineContentProcessor;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Iván Zaera Avellón
 */
public class ClientExtensionPortletIdBatchEngineContentProcessorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_exportImportThreadLocalMockedStatic = Mockito.mockStatic(
			ExportImportThreadLocal.class);
		_companyThreadLocalMockedStatic = Mockito.mockStatic(
			CompanyThreadLocal.class);

		_setImportInProcess(true);
		_setCompanyId(_TARGET_COMPANY_ID);
	}

	@After
	public void tearDown() {
		_companyThreadLocalMockedStatic.close();
		_exportImportThreadLocalMockedStatic.close();
	}

	@Test
	public void testProcessIgnoresContentFromSameCompany() {
		String content = _portletId(_TARGET_COMPANY_ID, _UUID_ERC);

		Assert.assertEquals(
			content, _batchEngineContentProcessor.process(content));
	}

	@Test
	public void testProcessIgnoresContentWhenCompanyIdIsNotSet() {
		_setCompanyId(0);

		String content = _portletId(_SOURCE_COMPANY_ID, _UUID_ERC);

		Assert.assertEquals(
			content, _batchEngineContentProcessor.process(content));
	}

	@Test
	public void testProcessIgnoresContentWhenImportIsNotInProcess() {
		_setImportInProcess(false);

		String content = _portletId(_SOURCE_COMPANY_ID, _UUID_ERC);

		Assert.assertEquals(
			content, _batchEngineContentProcessor.process(content));
	}

	@Test
	public void testProcessIgnoresNullContent() {
		Assert.assertNull(_batchEngineContentProcessor.process(null));
	}

	@Test
	public void testProcessIgnoresUnrelatedPortletIds() {
		String content = "com_liferay_journal_web_portlet_JournalPortlet";

		Assert.assertEquals(
			content, _batchEngineContentProcessor.process(content));
	}

	@Test
	public void testProcessPreservesPortletInstanceSuffix() {
		Assert.assertEquals(
			_portletId(_TARGET_COMPANY_ID, _UUID_ERC + "_INSTANCE_abcd1234"),
			_batchEngineContentProcessor.process(
				_portletId(
					_SOURCE_COMPANY_ID, _UUID_ERC + "_INSTANCE_abcd1234")));
	}

	@Test
	public void testProcessRewritesCustomExternalReferenceCode() {
		Assert.assertEquals(
			_portletId(_TARGET_COMPANY_ID, "my_custom_widget"),
			_batchEngineContentProcessor.process(
				_portletId(_SOURCE_COMPANY_ID, "my_custom_widget")));
	}

	@Test
	public void testProcessRewritesMultipleOccurrences() {
		Assert.assertEquals(
			_pageDefinition(
				_portletId(_TARGET_COMPANY_ID, _UUID_ERC),
				_portletId(_TARGET_COMPANY_ID, "second_erc")),
			_batchEngineContentProcessor.process(
				_pageDefinition(
					_portletId(_SOURCE_COMPANY_ID, _UUID_ERC),
					_portletId(54321, "second_erc"))));
	}

	@Test
	public void testProcessRewritesUppercaseExternalReferenceCode() {
		Assert.assertEquals(
			_portletId(_TARGET_COMPANY_ID, "MyWidget"),
			_batchEngineContentProcessor.process(
				_portletId(_SOURCE_COMPANY_ID, "MyWidget")));
	}

	@Test
	public void testProcessRewritesUUIDStyleExternalReferenceCode() {
		Assert.assertEquals(
			_portletId(_TARGET_COMPANY_ID, _UUID_ERC),
			_batchEngineContentProcessor.process(
				_portletId(_SOURCE_COMPANY_ID, _UUID_ERC)));
	}

	private String _pageDefinition(
		String firstPortletId, String secondPortletId) {

		return StringBundler.concat(
			"{\"widgetName\": \"", firstPortletId, "\", \"other\": \"",
			secondPortletId, "\"}");
	}

	private String _portletId(long companyId, String externalReferenceCode) {
		return StringBundler.concat(
			_PORTLET_ID_PREFIX, companyId, StringPool.UNDERLINE,
			externalReferenceCode);
	}

	private void _setCompanyId(long companyId) {
		_companyThreadLocalMockedStatic.when(
			CompanyThreadLocal::getCompanyId
		).thenReturn(
			companyId
		);
	}

	private void _setImportInProcess(boolean importInProcess) {
		_exportImportThreadLocalMockedStatic.when(
			ExportImportThreadLocal::isImportInProcess
		).thenReturn(
			importInProcess
		);
	}

	private static final String _PORTLET_ID_PREFIX =
		"com_liferay_client_extension_web_internal_portlet_" +
			"ClientExtensionEntryPortlet_";

	private static final long _SOURCE_COMPANY_ID = 12345;

	private static final long _TARGET_COMPANY_ID = 67890;

	private static final String _UUID_ERC =
		"a1b2c3d4_e5f6_7890_abcd_ef1234567890";

	private final BatchEngineContentProcessor _batchEngineContentProcessor =
		new ClientExtensionPortletIdBatchEngineContentProcessorImpl();
	private MockedStatic<CompanyThreadLocal> _companyThreadLocalMockedStatic;
	private MockedStatic<ExportImportThreadLocal>
		_exportImportThreadLocalMockedStatic;

}
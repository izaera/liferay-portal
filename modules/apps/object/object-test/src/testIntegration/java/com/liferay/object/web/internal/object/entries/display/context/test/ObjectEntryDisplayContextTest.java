/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.object.entries.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderingContext;
import com.liferay.dynamic.data.mapping.model.Value;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectLayoutBoxConstants;
import com.liferay.object.constants.ObjectWebKeys;
import com.liferay.object.display.context.ObjectEntryDisplayContext;
import com.liferay.object.display.context.ObjectEntryDisplayContextFactory;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectLayoutBox;
import com.liferay.object.model.ObjectLayoutColumn;
import com.liferay.object.model.ObjectLayoutRow;
import com.liferay.object.model.ObjectLayoutTab;
import com.liferay.object.rest.manager.v1_0.DefaultObjectEntryManager;
import com.liferay.object.rest.manager.v1_0.ObjectEntryManager;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectLayoutLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.object.service.persistence.ObjectLayoutBoxPersistence;
import com.liferay.object.service.persistence.ObjectLayoutColumnPersistence;
import com.liferay.object.service.persistence.ObjectLayoutRowPersistence;
import com.liferay.object.service.persistence.ObjectLayoutTabPersistence;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.TreeTestUtil;
import com.liferay.object.tree.Edge;
import com.liferay.object.tree.Node;
import com.liferay.object.tree.Tree;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletActionResponse;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.portlet.PortletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;

/**
 * @author Pedro Leite
 */
@FeatureFlags("LPD-34594")
@RunWith(Arquillian.class)
public class ObjectEntryDisplayContextTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_dtoConverterContext = new DefaultDTOConverterContext(
			false, Collections.emptyMap(), _dtoConverterRegistry, null,
			LocaleUtil.getDefault(), null, TestPropsValues.getUser());
		_defaultObjectEntryManager =
			(DefaultObjectEntryManager)_objectEntryManager;
		_objectDefinition = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(
				new TextObjectFieldBuilder(
				).labelMap(
					RandomTestUtil.randomLocaleStringMap()
				).name(
					"textObjectFieldName"
				).build()));
	}

	@Test
	public void testGetBackURL() throws Exception {
		Tree objectDefinitionTree = TreeTestUtil.createObjectDefinitionTree(
			_objectDefinitionLocalService, _objectRelationshipLocalService,
			true,
			LinkedHashMapBuilder.put(
				"A", new String[] {"AA", "AB"}
			).put(
				"AA", new String[] {"AAA", "AAB"}
			).put(
				"AB", new String[0]
			).put(
				"AAA", new String[0]
			).put(
				"AAB", new String[0]
			).build());

		Node nodeA = objectDefinitionTree.getRootNode();

		TreeTestUtil.createObjectEntryTree(
			"1", _objectDefinitionLocalService, _objectEntryLocalService,
			_objectFieldLocalService, _objectRelationshipLocalService,
			nodeA.getPrimaryKey());

		ObjectDefinition objectDefinitionAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AA");

		ObjectEntry objectEntryAA1 = _objectEntryLocalService.getObjectEntry(
			"AA1", objectDefinitionAA.getObjectDefinitionId());

		MockHttpServletRequest mockHttpServletRequest =
			_getMockHttpServletRequest(
				objectEntryAA1.getExternalReferenceCode(), objectDefinitionAA);

		ObjectEntry objectEntryA1 = _objectEntryLocalService.getObjectEntry(
			"A1", nodeA.getPrimaryKey());

		ObjectDefinition objectDefinitionA =
			_objectDefinitionLocalService.getObjectDefinition(
				nodeA.getPrimaryKey());

		Assert.assertEquals(
			PortletURLBuilder.create(
				PortalUtil.getControlPanelPortletURL(
					mockHttpServletRequest, objectDefinitionA.getPortletId(),
					PortletRequest.ACTION_PHASE)
			).setMVCRenderCommandName(
				"/object_entries/edit_object_entry"
			).setParameter(
				"externalReferenceCode",
				objectEntryA1.getExternalReferenceCode()
			).setParameter(
				"screenNavigationCategoryKey",
				() -> {
					Node nodeAA = objectDefinitionTree.getNode(
						objectDefinitionAA.getPrimaryKey());

					Edge edge = nodeAA.getEdge();

					return edge.getObjectRelationshipId();
				}
			).buildString(),
			_getBackURL(mockHttpServletRequest));

		ObjectDefinition objectDefinitionAAA =
			_objectDefinitionLocalService.getObjectDefinition(
				TestPropsValues.getCompanyId(), "C_AAA");

		ObjectEntry objectEntryAAA1 = _objectEntryLocalService.getObjectEntry(
			"AAA1", objectDefinitionAAA.getObjectDefinitionId());

		mockHttpServletRequest = _getMockHttpServletRequest(
			objectEntryAAA1.getExternalReferenceCode(), objectDefinitionAAA);

		Assert.assertEquals(
			PortletURLBuilder.create(
				PortalUtil.getControlPanelPortletURL(
					mockHttpServletRequest, objectDefinitionAA.getPortletId(),
					PortletRequest.ACTION_PHASE)
			).setMVCRenderCommandName(
				"/object_entries/edit_object_entry"
			).setParameter(
				"externalReferenceCode",
				objectEntryAA1.getExternalReferenceCode()
			).setParameter(
				"screenNavigationCategoryKey",
				() -> {
					Node nodeAAA = objectDefinitionTree.getNode(
						objectDefinitionAAA.getPrimaryKey());

					Edge edge = nodeAAA.getEdge();

					return edge.getObjectRelationshipId();
				}
			).buildString(),
			_getBackURL(mockHttpServletRequest));

		TreeTestUtil.deleteObjectDefinitionHierarchy(
			_objectDefinitionLocalService,
			new String[] {"C_A", "C_AA", "C_AB", "C_AAA", "C_AAB"},
			_objectEntryLocalService, _objectRelationshipLocalService);
	}

	@Test
	public void testRenderDDMForm() throws Exception {
		_objectLayoutLocalService.addObjectLayout(
			TestPropsValues.getUserId(),
			_objectDefinition.getObjectDefinitionId(), true,
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			Collections.singletonList(_addObjectLayoutTab()));

		_testRenderDDMForm("Aprovado", LocaleUtil.BRAZIL);
		_testRenderDDMForm("承認済み", LocaleUtil.JAPAN);
		_testRenderDDMForm("Approved", LocaleUtil.US);
	}

	private ObjectLayoutBox _addObjectLayoutBox() throws Exception {
		ObjectLayoutBox objectLayoutBox = _objectLayoutBoxPersistence.create(0);

		objectLayoutBox.setNameMap(RandomTestUtil.randomLocaleStringMap());
		objectLayoutBox.setType(ObjectLayoutBoxConstants.TYPE_REGULAR);
		objectLayoutBox.setObjectLayoutRows(
			Arrays.asList(
				_addObjectLayoutRow("status"),
				_addObjectLayoutRow("textObjectFieldName")));

		return objectLayoutBox;
	}

	private ObjectLayoutColumn _addObjectLayoutColumn(String objectFieldName)
		throws Exception {

		ObjectLayoutColumn objectLayoutColumn =
			_objectLayoutColumnPersistence.create(0);

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			_objectDefinition.getObjectDefinitionId(), objectFieldName);

		objectLayoutColumn.setObjectFieldId(objectField.getObjectFieldId());

		return objectLayoutColumn;
	}

	private ObjectLayoutRow _addObjectLayoutRow(String objectFieldName)
		throws Exception {

		ObjectLayoutRow objectLayoutRow = _objectLayoutRowPersistence.create(0);

		objectLayoutRow.setObjectLayoutColumns(
			Collections.singletonList(_addObjectLayoutColumn(objectFieldName)));

		return objectLayoutRow;
	}

	private ObjectLayoutTab _addObjectLayoutTab() throws Exception {
		ObjectLayoutTab objectLayoutTab = _objectLayoutTabPersistence.create(0);

		objectLayoutTab.setNameMap(RandomTestUtil.randomLocaleStringMap());
		objectLayoutTab.setObjectLayoutBoxes(
			Collections.singletonList(_addObjectLayoutBox()));

		return objectLayoutTab;
	}

	private String _getBackURL(MockHttpServletRequest mockHttpServletRequest)
		throws Exception {

		ObjectEntryDisplayContext objectEntryDisplayContext =
			_objectEntryDisplayContextFactory.create(mockHttpServletRequest);

		return objectEntryDisplayContext.getBackURL();
	}

	private MockHttpServletRequest _getMockHttpServletRequest(
			String externalReferenceCode, Locale locale,
			ObjectDefinition objectDefinition)
		throws Exception {

		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE,
			new MockLiferayPortletActionResponse());
		mockHttpServletRequest.setAttribute(
			ObjectWebKeys.OBJECT_DEFINITION, objectDefinition);
		mockHttpServletRequest.setAttribute(
			ObjectWebKeys.OBJECT_ENTRY_READ_ONLY, Boolean.FALSE);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()));
		themeDisplay.setLocale(locale);
		themeDisplay.setScopeGroupId(TestPropsValues.getGroupId());
		themeDisplay.setSiteGroupId(TestPropsValues.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		mockHttpServletRequest.setParameter(
			"externalReferenceCode", externalReferenceCode);
		mockHttpServletRequest.setParameter(
			"mvcRenderCommandName", "/object_entries/edit_object_entry");

		return mockHttpServletRequest;
	}

	private MockHttpServletRequest _getMockHttpServletRequest(
			String externalReferenceCode, ObjectDefinition objectDefinition)
		throws Exception {

		return _getMockHttpServletRequest(
			externalReferenceCode, LocaleUtil.getDefault(), objectDefinition);
	}

	private void _testRenderDDMForm(String expectedStatusLabel, Locale locale)
		throws Exception {

		AtomicReference<DDMFormRenderingContext> atomicReference =
			new AtomicReference<>();

		String textObjectFieldValue = RandomTestUtil.randomString();

		com.liferay.object.rest.dto.v1_0.ObjectEntry objectEntry =
			_defaultObjectEntryManager.addObjectEntry(
				_dtoConverterContext, _objectDefinition,
				new com.liferay.object.rest.dto.v1_0.ObjectEntry() {
					{
						properties = HashMapBuilder.<String, Object>put(
							"textObjectFieldName", textObjectFieldValue
						).build();
					}
				},
				null);

		ObjectEntryDisplayContext objectEntryDisplayContext =
			_objectEntryDisplayContextFactory.create(
				_getMockHttpServletRequest(
					objectEntry.getExternalReferenceCode(), locale,
					_objectDefinition));

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					objectEntryDisplayContext, "_ddmFormRenderer",
					ProxyUtil.newProxyInstance(
						DDMFormRenderer.class.getClassLoader(),
						new Class<?>[] {DDMFormRenderer.class},
						(proxy, method, arguments) -> {
							if (Objects.equals(method.getName(), "render")) {
								atomicReference.set(
									(DDMFormRenderingContext)arguments[2]);
							}

							return method.invoke(_ddmFormRenderer, arguments);
						}))) {

			objectEntryDisplayContext.renderDDMForm(new MockPageContext());
		}

		DDMFormRenderingContext ddmFormRenderingContext = atomicReference.get();

		DDMFormValues ddmFormValues =
			ddmFormRenderingContext.getDDMFormValues();

		List<DDMFormFieldValue> ddmFormFieldValues =
			ddmFormValues.getDDMFormFieldValues();

		Assert.assertEquals(
			ddmFormValues.toString(), 1, ddmFormFieldValues.size());

		DDMFormFieldValue ddmFormFieldValue = ddmFormFieldValues.get(0);

		List<DDMFormFieldValue> nestedDDMFormFieldValues =
			ddmFormFieldValue.getNestedDDMFormFieldValues();

		Assert.assertEquals(
			nestedDDMFormFieldValues.toString(), 2,
			nestedDDMFormFieldValues.size());

		DDMFormFieldValue nestedDDMFormFieldValue1 =
			nestedDDMFormFieldValues.get(0);

		Assert.assertEquals("status", nestedDDMFormFieldValue1.getName());

		Value value1 = nestedDDMFormFieldValue1.getValue();

		Assert.assertEquals(expectedStatusLabel, value1.getString(locale));

		DDMFormFieldValue nestedDDMFormFieldValue2 =
			nestedDDMFormFieldValues.get(1);

		Assert.assertEquals(
			"textObjectFieldName", nestedDDMFormFieldValue2.getName());

		Value value2 = nestedDDMFormFieldValue2.getValue();

		Assert.assertEquals(textObjectFieldValue, value2.getString(locale));
	}

	@Inject(
		filter = "object.entry.manager.storage.type=" + ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT
	)
	private static ObjectEntryManager _objectEntryManager;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DDMFormRenderer _ddmFormRenderer;

	private DefaultObjectEntryManager _defaultObjectEntryManager;
	private DTOConverterContext _dtoConverterContext;

	@Inject
	private DTOConverterRegistry _dtoConverterRegistry;

	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectEntryDisplayContextFactory _objectEntryDisplayContextFactory;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectLayoutBoxPersistence _objectLayoutBoxPersistence;

	@Inject
	private ObjectLayoutColumnPersistence _objectLayoutColumnPersistence;

	@Inject
	private ObjectLayoutLocalService _objectLayoutLocalService;

	@Inject
	private ObjectLayoutRowPersistence _objectLayoutRowPersistence;

	@Inject
	private ObjectLayoutTabPersistence _objectLayoutTabPersistence;

	@Inject
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}
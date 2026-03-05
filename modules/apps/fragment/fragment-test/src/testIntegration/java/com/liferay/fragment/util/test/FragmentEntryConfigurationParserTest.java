/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.fragment.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.entry.processor.constants.FragmentEntryProcessorConstants;
import com.liferay.fragment.util.configuration.FragmentEntryConfigurationParser;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Rubén Pulido
 */
@RunWith(Arquillian.class)
public class FragmentEntryConfigurationParserTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetConfigurationDefaultValuesJSONObject() throws Exception {
		JSONObject configurationDefaultValuesJSONObject =
			_fragmentEntryConfigurationParser.
				getConfigurationDefaultValuesJSONObject(
					_read("configuration.json"));

		JSONObject expectedConfigurationDefaultValuesJSONObject =
			JSONFactoryUtil.createJSONObject(
				_read("expected-configuration-default-values.json"));

		Assert.assertEquals(
			expectedConfigurationDefaultValuesJSONObject.toString(),
			configurationDefaultValuesJSONObject.toString());
	}

	@Test
	public void testGetFieldValueLocalizableFieldsWithoutLocale()
		throws Exception {

		Map<String, Object[]> fieldTypeValues =
			LinkedHashMapBuilder.<String, Object[]>put(
				"text",
				new Object[] {
					RandomTestUtil.randomString(), RandomTestUtil.randomString()
				}
			).put(
				"colorPicker", new Object[] {"#0F0303", "#35CC58"}
			).put(
				"length", new Object[] {"300px", "320px"}
			).put(
				"checkbox", new Object[] {Boolean.FALSE, Boolean.TRUE}
			).build();

		for (Map.Entry<String, Object[]> entry : fieldTypeValues.entrySet()) {
			String fieldType = entry.getKey();
			Object englishValue = entry.getValue()[0];
			Object spanishValue = entry.getValue()[1];

			String fieldName = RandomTestUtil.randomString();

			JSONObject configurationJSONObject = JSONUtil.put(
				"fieldSets",
				JSONUtil.put(
					JSONUtil.put(
						"fields",
						JSONUtil.put(
							JSONUtil.put(
								"defaultValue", englishValue
							).put(
								"label", fieldName
							).put(
								"localizable", true
							).put(
								"name", fieldName
							).put(
								"type", fieldType
							)))));

			JSONObject editableValuesJSONObject = JSONUtil.put(
				FragmentEntryProcessorConstants.
					KEY_FREEMARKER_FRAGMENT_ENTRY_PROCESSOR,
				JSONUtil.put(
					fieldName,
					JSONUtil.put(
						LocaleUtil.toLanguageId(LocaleUtil.US), englishValue
					).put(
						LocaleUtil.toLanguageId(LocaleUtil.SPAIN), spanishValue
					)));

			Object value = _fragmentEntryConfigurationParser.getFieldValue(
				configurationJSONObject.toString(),
				editableValuesJSONObject.toString(), fieldName);

			if (englishValue instanceof Boolean) {
				_assertLocalizableValue(
					value, englishValue, spanishValue, true);
			}
			else {
				_assertLocalizableValue(
					value, englishValue, spanishValue, false);
			}
		}
	}

	@Test
	public void testTranslateConfigurationEn() throws Exception {
		_testTranslateConfiguration("en");
	}

	@Test
	public void testTranslateConfigurationEs() throws Exception {
		_testTranslateConfiguration("es");
	}

	private void _assertLocalizableValue(
		Object value, Object englishValue, Object spanishValue,
		boolean booleanValue) {

		Assert.assertTrue(value instanceof JSONObject);

		JSONObject valueJSONObject = (JSONObject)value;

		Object expectedEnglishValue;
		Object expectedSpanishValue;

		if (booleanValue) {
			expectedEnglishValue = valueJSONObject.getBoolean(
				LocaleUtil.toLanguageId(LocaleUtil.US));
			expectedSpanishValue = valueJSONObject.getBoolean(
				LocaleUtil.toLanguageId(LocaleUtil.SPAIN));
		}
		else {
			expectedEnglishValue = valueJSONObject.getString(
				LocaleUtil.toLanguageId(LocaleUtil.US));
			expectedSpanishValue = valueJSONObject.getString(
				LocaleUtil.toLanguageId(LocaleUtil.SPAIN));
		}

		Assert.assertEquals(expectedEnglishValue, englishValue);
		Assert.assertEquals(expectedSpanishValue, spanishValue);
	}

	private ResourceBundle _getResourceBundle(String language) {
		Class<?> clazz = getClass();

		Package pkg = clazz.getPackage();

		return ResourceBundleUtil.getBundle(
			pkg.getName() + ".dependencies.content.Language",
			new Locale(language), clazz);
	}

	private String _read(String fileName) throws Exception {
		return new String(
			FileUtil.getBytes(getClass(), "dependencies/" + fileName));
	}

	private void _testTranslateConfiguration(String language) throws Exception {
		JSONObject configurationJSONObject = JSONFactoryUtil.createJSONObject(
			_read("configuration_untranslated.json"));

		JSONObject expectedConfigurationTranslatedJSONObject =
			JSONFactoryUtil.createJSONObject(
				_read(
					String.format(
						"expected_configuration_translated_%s.json",
						language)));

		Assert.assertEquals(
			expectedConfigurationTranslatedJSONObject.toString(),
			_fragmentEntryConfigurationParser.translateConfiguration(
				configurationJSONObject, _getResourceBundle(language)));
	}

	@Inject
	private FragmentEntryConfigurationParser _fragmentEntryConfigurationParser;

}
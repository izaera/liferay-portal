/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.internal.jaxrs.writer;

import static com.liferay.apio.architect.form.FieldType.BOOLEAN;
import static com.liferay.apio.architect.form.FieldType.LONG;
import static com.liferay.apio.architect.form.FieldType.NESTED_MODEL;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

import com.liferay.apio.architect.form.Body;
import com.liferay.apio.architect.form.FieldType;
import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.form.FormField;
import com.liferay.apio.architect.language.AcceptLanguage;

import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class FormMessageBodyWriterTest {

	@Test
	public void testGetURLForReturnsNullForNullFormID() {
		Form form = mock(Form.class);

		when(
			form.getId()
		).thenReturn(
			null
		);

		String url = FormMessageBodyWriter.getURLFor(form, () -> "localhost");

		assertThat(url, is(nullValue()));
	}

	@Test
	public void testGetURLForReturnsURLForValidFormID() {
		Form form = mock(Form.class);

		when(
			form.getId()
		).thenReturn(
			"/uri"
		);

		String url = FormMessageBodyWriter.getURLFor(form, () -> "localhost");

		assertThat(url, is("localhost/uri"));
	}

	@Test
	public void testIsWritableReturnsFalseIfNotFormClass() {
		FormMessageBodyWriter formMessageBodyWriter =
			new FormMessageBodyWriter();

		boolean writeable = formMessageBodyWriter.isWriteable(
			Long.class, null, null, null);

		assertFalse(writeable);
	}

	@Test
	public void testIsWritableReturnsTrueIfFormClass() {
		FormMessageBodyWriter formMessageBodyWriter =
			new FormMessageBodyWriter();

		boolean writeable = formMessageBodyWriter.isWriteable(
			Form.class, null, null, null);

		assertTrue(writeable);
	}

	@Test
	public void testToJSONObjectCreatesValidJSONObject() {
		Form<?> form = _createForm();

		JSONObject jsonObject = FormMessageBodyWriter.toJSONObject(
			form, () -> Locale.US, () -> "localhost", true);

		JSONObject expectedJSONObject = _getExpectedJSONObject();

		assertThat(jsonObject, is(sameJSONObjectAs(expectedJSONObject)));
	}

	@Test
	public void testToJSONObjectCreatesValidJSONObjectWithNested() {
		Form<?> form = _createForm(
			asList(
				_createFormField(BOOLEAN, "boolean", true),
				_createFormField(LONG, "long", false),
				_createFormField(
					NESTED_MODEL, "nested", false, _createForm())));

		JSONObject jsonObject = FormMessageBodyWriter.toJSONObject(
			form, () -> Locale.US, () -> "localhost", true);

		JSONObject nestedJSONObject = _getExpectedJSONObject();

		nestedJSONObject.remove("@context");

		JSONObject expectedJSONObject = _getExpectedJSONObject();

		JSONArray jsonArray = expectedJSONObject.optJSONArray(
			"supportedProperty");

		jsonArray.put(
			_createJSONObject(
				formFieldBuilder -> formFieldBuilder.put(
					"@type", "SupportedProperty"
				).put(
					"property", "nested"
				).put(
					"required", false
				).put(
					"readable", false
				).put(
					"writeable", true
				).put(
					"value", nestedJSONObject
				)));

		assertThat(jsonObject, is(sameJSONObjectAs(expectedJSONObject)));
	}

	@Test
	public void testToJSONObjectCreatesValidObjectWithNullNested() {
		FormField formField = _createFormField(BOOLEAN, "field", true);

		JSONObject jsonObject = FormMessageBodyWriter.toJSONObject(
			formField, null);

		JSONObject expectedJSONObject = _createJSONObject(
			builder -> builder.put(
				"@type", "SupportedProperty"
			).put(
				"property", "field"
			).put(
				"required", true
			).put(
				"readable", false
			).put(
				"writeable", true
			));

		assertThat(jsonObject, is(sameJSONObjectAs(expectedJSONObject)));
	}

	@Test
	public void testToJSONObjectCreatesValidObjectWithPresentNested() {
		FormField formField = _createFormField(LONG, "property", false);

		JSONObject nestedJSONObject = _createJSONObject(
			nestedBuilder -> nestedBuilder.put("field", "value"));

		JSONObject jsonObject = FormMessageBodyWriter.toJSONObject(
			formField, nestedJSONObject);

		JSONObject expected = _createJSONObject(
			builder -> builder.put(
				"@type", "SupportedProperty"
			).put(
				"property", "property"
			).put(
				"required", false
			).put(
				"readable", false
			).put(
				"writeable", true
			).put(
				"value", nestedJSONObject
			));

		assertThat(jsonObject, is(sameJSONObjectAs(expected)));
	}

	private static JSONObject _createJSONObject(
		Function1<Map<String, Object>, Map<String, Object>> function1) {

		return function1.andThen(
			Map::toJavaMap
		).andThen(
			JSONObject::new
		).apply(
			HashMap.empty()
		);
	}

	private Form<?> _createForm() {
		return _createForm(
			asList(
				_createFormField(BOOLEAN, "boolean", true),
				_createFormField(LONG, "long", false)));
	}

	private Form<?> _createForm(List<FormField> formFields) {
		return new Form<Object>() {

			@Override
			public Object get(Body body) {
				throw new AssertionError("Should not be called");
			}

			@Override
			public String getDescription(AcceptLanguage acceptLanguage) {
				return "Description";
			}

			@Override
			public List<FormField> getFormFields() {
				return formFields;
			}

			@Override
			public String getId() {
				return "/uri";
			}

			@Override
			public List<Object> getList(Body body) {
				throw new AssertionError("Should not be called");
			}

			@Override
			public String getTitle(AcceptLanguage acceptLanguage) {
				return "Title";
			}

		};
	}

	private FormField _createFormField(
		FieldType fieldType, String name, boolean required) {

		return _createFormField(fieldType, name, required, null);
	}

	private FormField _createFormField(
		FieldType fieldType, String name, boolean required, Form form) {

		return new FormField() {

			@Override
			public FieldType getFieldType() {
				return fieldType;
			}

			@Override
			public Form getForm() {
				return form;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public boolean isRequired() {
				return required;
			}

		};
	}

	private JSONObject _getExpectedJSONObject() {
		return _createJSONObject(
			builder -> builder.put(
				"@context",
				asList(
					singletonMap("@vocab", "http://schema.org"),
					"https://www.w3.org/ns/hydra/core#")
			).put(
				"@type", "Class"
			).put(
				"@id", "localhost/uri"
			).put(
				"title", "Title"
			).put(
				"description", "Description"
			).put(
				"supportedProperty",
				asList(
					_createJSONObject(
						formFieldBuilder -> formFieldBuilder.put(
							"@type", "SupportedProperty"
						).put(
							"property", "boolean"
						).put(
							"required", true
						).put(
							"readable", false
						).put(
							"writeable", true
						)),
					_createJSONObject(
						formFieldBuilder -> formFieldBuilder.put(
							"@type", "SupportedProperty"
						).put(
							"property", "long"
						).put(
							"required", false
						).put(
							"readable", false
						).put(
							"writeable", true
						)))
			));
	}

}
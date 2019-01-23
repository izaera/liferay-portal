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

package com.liferay.apio.architect.internal.test.jaxrs.resource;

import static com.liferay.apio.architect.form.FieldType.BOOLEAN;
import static com.liferay.apio.architect.form.FieldType.BOOLEAN_LIST;
import static com.liferay.apio.architect.form.FieldType.FILE;
import static com.liferay.apio.architect.form.FieldType.LONG;
import static com.liferay.apio.architect.form.FieldType.STRING;
import static com.liferay.apio.architect.operation.HTTPMethod.POST;
import static com.liferay.apio.architect.operation.HTTPMethod.PUT;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONObjectAs;

import com.liferay.apio.architect.credentials.Credentials;
import com.liferay.apio.architect.form.Body;
import com.liferay.apio.architect.form.FieldType;
import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.form.FormField;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.annotation.Action;
import com.liferay.apio.architect.internal.annotation.ActionManager;
import com.liferay.apio.architect.internal.documentation.Documentation;
import com.liferay.apio.architect.internal.entrypoint.EntryPoint;
import com.liferay.apio.architect.internal.test.base.BaseTest;
import com.liferay.apio.architect.language.AcceptLanguage;
import com.liferay.apio.architect.resource.Resource;
import com.liferay.apio.architect.resource.Resource.Item;
import com.liferay.apio.architect.resource.Resource.Nested;
import com.liferay.apio.architect.resource.Resource.Paged;
import com.liferay.apio.architect.single.model.SingleModel;

import io.vavr.control.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for integration between {@link
 * com.liferay.apio.architect.internal.jaxrs.resource.FormResource} and {@link
 * com.liferay.apio.architect.internal.jaxrs.writer.FormMessageBodyWriter}
 * classes.
 *
 * @author Alejandro Hernández
 */
public class FormResourceTest extends BaseTest {

	@BeforeClass
	public static void setUpClass() {
		BaseTest.setUpClass();

		beforeClassUnregisterImplementationFor(ActionManager.class);
		beforeClassRegisterImplementationFor(
			ActionManager.class, new ActionManagerImpl(), noProperties);
	}

	@Test
	public void testCreatorFormReturns404IfInvalidName() {
		Response response = _makeRequestTo("f/c/wrong");

		assertThat(response.getStatus(), is(404));

		assertThat(asJSONObject(response), is(sameJSONObjectAs(_404)));
	}

	@Test
	public void testCreatorFormReturnsValidJSONObject() {
		Response response = _makeRequestTo("f/c/name");

		assertThat(response.getStatus(), is(200));

		JSONObject expectedJSONObject = _getExpectedJSONObject(
			"boolean1", "boolean2", false);

		assertThat(
			asJSONObject(response), is(sameJSONObjectAs(expectedJSONObject)));
	}

	@Test
	public void testCustomRouteFormReturns404IfInvalidName() {
		Response response = _makeRequestTo("f/p/name/wrong");

		assertThat(response.getStatus(), is(404));

		assertThat(asJSONObject(response), is(sameJSONObjectAs(_404)));
	}

	@Test
	public void testCustomRouteFormReturnsValidJSONObject() {
		Response response = _makeRequestTo("f/p/name/custom");

		assertThat(response.getStatus(), is(200));

		JSONObject expected = _getExpectedJSONObject("boolean", "long", false);

		assertThat(asJSONObject(response), is(sameJSONObjectAs(expected)));
	}

	@Test
	public void testNestedCreatorFormReturns404IfInvalidName() {
		Response response = _makeRequestTo("f/c/parent/wrong");

		assertThat(response.getStatus(), is(404));

		assertThat(asJSONObject(response), is(sameJSONObjectAs(_404)));
	}

	@Test
	public void testNestedCreatorFormReturnsValidJSONObject() {
		Response response = _makeRequestTo("f/c/parent/name");

		assertThat(response.getStatus(), is(200));

		JSONObject expectedJSONObject = _getExpectedJSONObject(
			"booleanList", "long", true);

		assertThat(
			asJSONObject(response), is(sameJSONObjectAs(expectedJSONObject)));
	}

	@Test
	public void testUpdaterFormReturns404IfInvalidName() {
		Response response = _makeRequestTo("f/u/wrong");

		assertThat(response.getStatus(), is(404));

		assertThat(asJSONObject(response), is(sameJSONObjectAs(_404)));
	}

	@Test
	public void testUpdaterFormReturnsValidJSONObject() {
		Response response = _makeRequestTo("f/u/name");

		assertThat(response.getStatus(), is(200));

		JSONObject expected = _getExpectedJSONObject("string", "file", true);

		assertThat(asJSONObject(response), is(sameJSONObjectAs(expected)));
	}

	private JSONObject _getExpectedJSONObject(
		String propertyName1, String propertyName2, boolean required2) {

		return createJSONObject(
			builder -> builder.put(
				"@context",
				asList(
					singletonMap("@vocab", "http://schema.org"),
					"https://www.w3.org/ns/hydra/core#")
			).put(
				"@type", "Class"
			).put(
				"@id", getJAXRSServiceEndpoint() + "uri"
			).put(
				"title", "Title"
			).put(
				"description", "Description"
			).put(
				"supportedProperty",
				asList(
					createJSONObject(
						formFieldBuilder -> formFieldBuilder.put(
							"@type", "SupportedProperty"
						).put(
							"property", propertyName1
						).put(
							"required", true
						).put(
							"readable", false
						).put(
							"writeable", true
						)),
					createJSONObject(
						formFieldBuilder -> formFieldBuilder.put(
							"@type", "SupportedProperty"
						).put(
							"property", propertyName2
						).put(
							"required", required2
						).put(
							"readable", false
						).put(
							"writeable", true
						)))
			));
	}

	private Response _makeRequestTo(String path) {
		WebTarget webTarget = createDefaultTarget();

		return webTarget.path(
			path
		).request(
		).header(
			"Accept", "application/ld+json"
		).get();
	}

	private static final JSONObject _404 = createJSONObject(
		builder -> builder.put(
			"@type", "not-found"
		).put(
			"statusCode", 404
		).put(
			"title", "Not Found"
		));

	private static class ActionManagerImpl implements ActionManager {

		@Override
		public Either<Action.Error, Action> getAction(
			String method, List<String> params) {

			throw new AssertionError("Should not be called");
		}

		@Override
		public Stream<ActionSemantics> getActionSemantics(
			Resource resource, Credentials credentials,
			HttpServletRequest httpServletRequest) {

			throw new AssertionError("Should not be called");
		}

		@Override
		public Stream<ActionSemantics> getActionSemanticsStream() {
			return _actionSemantics.stream();
		}

		@Override
		public Documentation getDocumentation(
			HttpServletRequest httpServletRequest) {

			throw new AssertionError("Should not be called");
		}

		@Override
		public EntryPoint getEntryPoint() {
			throw new AssertionError("Should not be called");
		}

		@Override
		public Optional<SingleModel> getItemSingleModel(
			Item item, HttpServletRequest request) {

			throw new AssertionError("Should not be called");
		}

		private static Form<?> _createForm(List<FormField> formFields) {
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

		private static FormField _createFormField(
			FieldType fieldType, String name, boolean required) {

			return new FormField() {

				@Override
				public FieldType getFieldType() {
					return fieldType;
				}

				@Override
				public Form getForm() {
					return null;
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

		private static final List<ActionSemantics> _actionSemantics =
			new ArrayList<>();

		static {
			_actionSemantics.add(
				ActionSemantics.ofResource(
					Paged.of("name")
				).name(
					"create"
				).method(
					POST
				).returns(
					Void.class
				).permissionFunction(
				).executeFunction(
					__ -> null
				).form(
					_createForm(
						asList(
							_createFormField(BOOLEAN, "boolean1", true),
							_createFormField(BOOLEAN, "boolean2", false))),
					Form::get
				).build());

			_actionSemantics.add(
				ActionSemantics.ofResource(
					Nested.of(Item.of("parent"), "name")
				).name(
					"create"
				).method(
					POST
				).returns(
					Void.class
				).permissionFunction(
				).executeFunction(
					__ -> null
				).form(
					_createForm(
						asList(
							_createFormField(BOOLEAN_LIST, "booleanList", true),
							_createFormField(LONG, "long", true))),
					Form::get
				).build());

			_actionSemantics.add(
				ActionSemantics.ofResource(
					Item.of("name")
				).name(
					"replace"
				).method(
					PUT
				).returns(
					Void.class
				).permissionFunction(
				).executeFunction(
					__ -> null
				).form(
					_createForm(
						asList(
							_createFormField(STRING, "string", true),
							_createFormField(FILE, "file", true))),
					Form::get
				).build());

			_actionSemantics.add(
				ActionSemantics.ofResource(
					Item.of("name")
				).name(
					"custom"
				).method(
					POST
				).returns(
					Void.class
				).permissionFunction(
				).executeFunction(
					__ -> null
				).form(
					_createForm(
						asList(
							_createFormField(BOOLEAN, "boolean", true),
							_createFormField(LONG, "long", false))),
					Form::get
				).build());
		}

	}

}
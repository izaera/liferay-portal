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

package com.liferay.apio.architect.internal.jaxrs.resource;

import static com.liferay.apio.architect.internal.util.matcher.FailsWith.failsWith;
import static com.liferay.apio.architect.operation.HTTPMethod.POST;
import static com.liferay.apio.architect.operation.HTTPMethod.PUT;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.form.Form.Builder;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.annotation.ActionManager;
import com.liferay.apio.architect.internal.form.FormImpl.BuilderImpl;
import com.liferay.apio.architect.resource.Resource.Item;
import com.liferay.apio.architect.resource.Resource.Nested;
import com.liferay.apio.architect.resource.Resource.Paged;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.ws.rs.NotFoundException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class FormResourceTest {

	@Before
	public void setUp() {
		_actionManager = mock(ActionManager.class);

		when(
			_actionManager.getActionSemanticsStream()
		).then(
			__ -> _actionSemantics.stream()
		);

		_formResource = new FormResource();

		_formResource.actionManager = _actionManager;
	}

	@Test
	public void testCreatorFormReturnsFormForCreateAction() {
		Form creatorForm = _formResource.creatorForm("name");

		assertThat(creatorForm, is(_creatorForm));
	}

	@Test
	public void testCustomRouteFormReturnsFormForCustomAction() {
		Form creatorForm = _formResource.customRouteForm("name", "custom");

		assertThat(creatorForm, is(_customRouteForm));
	}

	@Test
	public void testNestedCreatorFormReturnsFormForNestedCreateAction() {
		Form creatorForm = _formResource.nestedCreatorForm("parent", "name");

		assertThat(creatorForm, is(_nestedCreatorForm));
	}

	@Test
	public void testThrowNotFoundWhenNoActionSemanticsArePresent() {
		when(
			_actionManager.getActionSemanticsStream()
		).then(
			__ -> Stream.empty()
		);

		assertThat(
			() -> _formResource.creatorForm("resource"),
			failsWith(NotFoundException.class));

		assertThat(
			() -> _formResource.customRouteForm("resource", "action"),
			failsWith(NotFoundException.class));

		assertThat(
			() -> _formResource.nestedCreatorForm("parent", "resource"),
			failsWith(NotFoundException.class));

		assertThat(
			() -> _formResource.updaterForm("resource"),
			failsWith(NotFoundException.class));
	}

	@Test
	public void testUpdaterFormReturnsFormForReplaceAction() {
		Form creatorForm = _formResource.updaterForm("name");

		assertThat(creatorForm, is(_updaterForm));
	}

	private static Form _createRandomForm() {
		Builder<Object> builder = new BuilderImpl<>(null, null);

		return builder.title(
			__ -> ""
		).description(
			__ -> ""
		).constructor(
			() -> new Random().nextInt()
		).build();
	}

	private static final List<ActionSemantics> _actionSemantics =
		new ArrayList<>();
	private static final Form _creatorForm = _createRandomForm();
	private static final Form _customRouteForm = _createRandomForm();
	private static final Form _nestedCreatorForm = _createRandomForm();
	private static final Form _updaterForm = _createRandomForm();

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
				_creatorForm, Form::get
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
				_nestedCreatorForm, Form::get
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
				_updaterForm, Form::get
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
				_customRouteForm, Form::get
			).build());
	}

	private ActionManager _actionManager;
	private FormResource _formResource;

}
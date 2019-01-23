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

package com.liferay.apio.architect.internal.routes;

import static com.liferay.apio.architect.internal.action.Predicates.isRemoveAction;
import static com.liferay.apio.architect.internal.action.Predicates.isReplaceAction;
import static com.liferay.apio.architect.internal.action.Predicates.isRetrieveAction;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.FORM_BUILDER_FUNCTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.FORM_BUILDER_SUPPLIER;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.GET_CUSTOM_ROUTE;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.HAS_REMOVE_PERMISSION_FUNCTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.HAS_UPDATE_PERMISSION_FUNCTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IS_READ_ACTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IS_WRITE_ACTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.POST_CUSTOM_ROUTE;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.filterActionSemantics;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.getParams;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.prependWith;
import static com.liferay.apio.architect.internal.util.matcher.FailsWith.failsWith;

import static io.vavr.Predicates.isNull;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import com.liferay.apio.architect.annotation.Id;
import com.liferay.apio.architect.form.Body;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.routes.ItemRoutesImpl.BuilderImpl;
import com.liferay.apio.architect.internal.routes.RoutesTestUtil.CustomIdentifier;
import com.liferay.apio.architect.resource.Resource.Item;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.single.model.SingleModel;

import io.vavr.control.Try;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class ItemRoutesImplTest {

	@Before
	public void setUp() {
		_builder = new ItemRoutesImpl.BuilderImpl<>(
			Item.of("name"), FORM_BUILDER_SUPPLIER,
			__ -> Optional.of("custom"));
	}

	@Test
	public void testEmptyBuilderDoesNotGenerateActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		ItemRoutesImpl<String, Long> itemRoutesImpl =
			(ItemRoutesImpl<String, Long>)itemRoutes;

		assertThat(itemRoutesImpl.getActionSemantics(), is(empty()));
	}

	@Test
	public void testFiveParameterBuilderMethodsCreatesActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.addGetter(
			this::_testAndReturnFourParameterGetterRoute, String.class,
			Long.class, Boolean.class, Integer.class
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnFourParameterCustomRoute,
			String.class, Long.class, Boolean.class, Integer.class,
			CustomIdentifier.class, (credentials, aLong) -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnFourParameterCustomRoute,
			String.class, Long.class, Boolean.class, Integer.class,
			CustomIdentifier.class, (credentials, aLong) -> true,
			FORM_BUILDER_FUNCTION
		).addRemover(
			this::_testFourParameterRemoverRoute, String.class, Long.class,
			Boolean.class, Integer.class, HAS_REMOVE_PERMISSION_FUNCTION
		).addUpdater(
			this::_testAndReturnFourParameterUpdaterRoute, String.class,
			Long.class, Boolean.class, Integer.class,
			HAS_UPDATE_PERMISSION_FUNCTION, FORM_BUILDER_FUNCTION
		).build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		_testActionSemantics(
			(ItemRoutesImpl<String, Long>)itemRoutes,
			asList(String.class, Long.class, Boolean.class, Integer.class));
	}

	@Test
	public void testFourParameterBuilderMethodsCreatesActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.addGetter(
			this::_testAndReturnThreeParameterGetterRoute, String.class,
			Long.class, Boolean.class
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnThreeParameterCustomRoute,
			String.class, Long.class, Boolean.class, CustomIdentifier.class,
			(credentials, aLong) -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnThreeParameterCustomRoute,
			String.class, Long.class, Boolean.class, CustomIdentifier.class,
			(credentials, aLong) -> true, FORM_BUILDER_FUNCTION
		).addRemover(
			this::_testThreeParameterRemoverRoute, String.class, Long.class,
			Boolean.class, HAS_REMOVE_PERMISSION_FUNCTION
		).addUpdater(
			this::_testAndReturnThreeParameterUpdaterRoute, String.class,
			Long.class, Boolean.class, HAS_UPDATE_PERMISSION_FUNCTION,
			FORM_BUILDER_FUNCTION
		).build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		_testActionSemantics(
			(ItemRoutesImpl<String, Long>)itemRoutes,
			asList(String.class, Long.class, Boolean.class, Void.class));
	}

	@Test
	public void testItemRoutesDeprecatedMethodsThrowsException() {
		ItemRoutes<String, Long> itemRoutes = _builder.build();

		assertThat(
			itemRoutes::getItemFunctionOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			itemRoutes::getCustomItemFunctionsOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			itemRoutes::getCustomRoutes,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			itemRoutes::getUpdateItemFunctionOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			itemRoutes::getDeleteConsumerOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			itemRoutes::getFormOptional,
			failsWith(UnsupportedOperationException.class));
	}

	@Test
	public void testOneParameterBuilderMethodsCreatesActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.addGetter(
			this::_testAndReturnNoParameterGetterRoute
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnNoParameterCustomRoute,
			CustomIdentifier.class, (credentials, aLong) -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnNoParameterCustomRoute,
			CustomIdentifier.class, (credentials, aLong) -> true,
			FORM_BUILDER_FUNCTION
		).addRemover(
			this::_testAndReturnNoParameterRemoverRoute,
			HAS_REMOVE_PERMISSION_FUNCTION
		).addUpdater(
			this::_testAndReturnNoParameterUpdaterRoute,
			HAS_UPDATE_PERMISSION_FUNCTION, FORM_BUILDER_FUNCTION
		).build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		_testActionSemantics(
			(ItemRoutesImpl<String, Long>)itemRoutes,
			asList(Void.class, Void.class, Void.class, Void.class));
	}

	@Test
	public void testThreeParameterBuilderMethodsCreatesActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.addGetter(
			this::_testAndReturnTwoParameterGetterRoute, String.class,
			Long.class
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnTwoParameterCustomRoute,
			String.class, Long.class, CustomIdentifier.class,
			(credentials, aLong) -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnTwoParameterCustomRoute,
			String.class, Long.class, CustomIdentifier.class,
			(credentials, aLong) -> true, FORM_BUILDER_FUNCTION
		).addRemover(
			this::_testTwoParameterRemoverRoute, String.class, Long.class,
			HAS_REMOVE_PERMISSION_FUNCTION
		).addUpdater(
			this::_testAndReturnTwoParameterUpdaterRoute, String.class,
			Long.class, HAS_UPDATE_PERMISSION_FUNCTION, FORM_BUILDER_FUNCTION
		).build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		_testActionSemantics(
			(ItemRoutesImpl<String, Long>)itemRoutes,
			asList(String.class, Long.class, Void.class, Void.class));
	}

	@Test
	public void testTwoParameterBuilderMethodsCreatesActionSemantics() {
		ItemRoutes<String, Long> itemRoutes = _builder.addGetter(
			this::_testAndReturnOneParameterGetterRoute, String.class
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnOneParameterCustomRoute,
			String.class, CustomIdentifier.class, (credentials, aLong) -> true,
			null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnOneParameterCustomRoute,
			String.class, CustomIdentifier.class, (credentials, aLong) -> true,
			FORM_BUILDER_FUNCTION
		).addRemover(
			this::_testOneParameterRemoverRoute, String.class,
			HAS_REMOVE_PERMISSION_FUNCTION
		).addUpdater(
			this::_testAndReturnOneParameterUpdaterRoute, String.class,
			HAS_UPDATE_PERMISSION_FUNCTION, FORM_BUILDER_FUNCTION
		).build();

		assertThat(itemRoutes, instanceOf(ItemRoutesImpl.class));

		_testActionSemantics(
			(ItemRoutesImpl<String, Long>)itemRoutes,
			asList(String.class, Void.class, Void.class, Void.class));
	}

	private void _testActionSemantics(
		ItemRoutesImpl<String, Long> itemRoutesImpl,
		List<Class<?>> paramClasses) {

		List<ActionSemantics> actionSemantics =
			itemRoutesImpl.getActionSemantics();

		assertThat(actionSemantics.size(), is(5));

		_testRouteActionSemantics(
			prependWith(paramClasses, Id.class),
			filterActionSemantics(actionSemantics, isRetrieveAction), "GET",
			"retrieve", "name", "Apio");

		_testRouteActionSemantics(
			prependWith(paramClasses, Id.class, Body.class),
			filterActionSemantics(actionSemantics, isReplaceAction), "PUT",
			"replace", "name", "Updated");

		_testRemoveActionSemantics(
			prependWith(paramClasses, Id.class),
			filterActionSemantics(actionSemantics, isRemoveAction));

		_testRouteActionSemantics(
			prependWith(paramClasses, Id.class, Void.class),
			filterActionSemantics(actionSemantics, IS_READ_ACTION), "GET",
			"read", "custom", "Custom Apio");

		_testRouteActionSemantics(
			prependWith(paramClasses, Id.class, Body.class),
			filterActionSemantics(actionSemantics, IS_WRITE_ACTION), "POST",
			"write", "custom", "Custom Apio");
	}

	private String _testAndReturnFourParameterCustomRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong,
		Boolean aBoolean, Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterCustomRoute(
			identifier, body, string, aLong, aBoolean);
	}

	private String _testAndReturnFourParameterGetterRoute(
		Long identifier, String string, Long aLong, Boolean aBoolean,
		Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterGetterRoute(
			identifier, string, aLong, aBoolean);
	}

	private String _testAndReturnFourParameterUpdaterRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong,
		Boolean aBoolean, Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterUpdaterRoute(
			identifier, body, string, aLong, aBoolean);
	}

	private String _testAndReturnNoParameterCustomRoute(
		Long identifier, Map<String, Object> body) {

		assertThat(identifier, is(42L));

		if (body != null) {
			assertThat(body.get("key"), is("Apio"));
		}

		return "Custom Apio";
	}

	private String _testAndReturnNoParameterGetterRoute(Long identifier) {
		assertThat(identifier, is(42L));

		return "Apio";
	}

	private void _testAndReturnNoParameterRemoverRoute(Long identifier) {
		assertThat(identifier, is(42L));
	}

	private String _testAndReturnNoParameterUpdaterRoute(
		Long identifier, Map<String, Object> body) {

		assertThat(identifier, is(42L));

		assertThat(body.get("key"), is("Apio"));

		return "Updated";
	}

	private String _testAndReturnOneParameterCustomRoute(
		Long identifier, Map<String, Object> body, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterCustomRoute(identifier, body);
	}

	private String _testAndReturnOneParameterGetterRoute(
		Long identifier, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterGetterRoute(identifier);
	}

	private String _testAndReturnOneParameterUpdaterRoute(
		Long identifier, Map<String, Object> body, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterUpdaterRoute(identifier, body);
	}

	private String _testAndReturnThreeParameterCustomRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong,
		Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterCustomRoute(
			identifier, body, string, aLong);
	}

	private String _testAndReturnThreeParameterGetterRoute(
		Long identifier, String string, Long aLong, Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterGetterRoute(identifier, string, aLong);
	}

	private String _testAndReturnThreeParameterUpdaterRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong,
		Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterUpdaterRoute(
			identifier, body, string, aLong);
	}

	private String _testAndReturnTwoParameterCustomRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterCustomRoute(identifier, body, string);
	}

	private String _testAndReturnTwoParameterGetterRoute(
		Long identifier, String string, Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterGetterRoute(identifier, string);
	}

	private String _testAndReturnTwoParameterUpdaterRoute(
		Long identifier, Map<String, Object> body, String string, Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterUpdaterRoute(identifier, body, string);
	}

	private void _testFourParameterRemoverRoute(
		Long identifier, String string, Long aLong, Boolean aBoolean,
		Integer integer) {

		assertThat(integer, is(2017));

		_testThreeParameterRemoverRoute(identifier, string, aLong, aBoolean);
	}

	private void _testOneParameterRemoverRoute(Long identifier, String string) {
		assertThat(string, is("Apio"));

		_testAndReturnNoParameterRemoverRoute(identifier);
	}

	private void _testRemoveActionSemantics(
		List<Class<?>> paramClasses, ActionSemantics actionSemantics) {

		assertThat(actionSemantics.getHTTPMethod(), is("DELETE"));
		assertThat(actionSemantics.getActionName(), is("remove"));
		assertThat(actionSemantics.getParamClasses(), is(paramClasses));
		assertThat(actionSemantics.getResource(), is(Item.of("name")));
		assertThat(actionSemantics.getReturnClass(), is(equalTo(Void.class)));
		assertThat(actionSemantics.getAnnotations(), hasSize(0));

		Try.of(
			() -> actionSemantics.execute(
				getParams(actionSemantics, paramClasses))
		).filter(
			isNull()
		).get();
	}

	private void _testRouteActionSemantics(
		List<Class<?>> paramClasses, ActionSemantics actionSemantics,
		String method, String actionName, String resourceName, String result) {

		assertThat(actionSemantics.getHTTPMethod(), is(method));
		assertThat(actionSemantics.getActionName(), is(actionName));
		assertThat(actionSemantics.getParamClasses(), is(paramClasses));
		assertThat(actionSemantics.getResource(), is(Item.of("name")));
		assertThat(
			actionSemantics.getReturnClass(), is(equalTo(SingleModel.class)));
		assertThat(actionSemantics.getAnnotations(), hasSize(0));

		SingleModel<?> singleModel = Try.of(
			() -> actionSemantics.execute(
				getParams(actionSemantics, paramClasses))
		).map(
			SingleModel.class::cast
		).get();

		assertThat(singleModel.getModel(), is(result));
		assertThat(singleModel.getOperations(), is(empty()));
		assertThat(singleModel.getResourceName(), is(resourceName));
	}

	private void _testThreeParameterRemoverRoute(
		Long identifier, String string, Long aLong, Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		_testTwoParameterRemoverRoute(identifier, string, aLong);
	}

	private void _testTwoParameterRemoverRoute(
		Long identifier, String string, Long aLong) {

		assertThat(aLong, is(42L));

		_testOneParameterRemoverRoute(identifier, string);
	}

	private BuilderImpl<String, Long> _builder;

}
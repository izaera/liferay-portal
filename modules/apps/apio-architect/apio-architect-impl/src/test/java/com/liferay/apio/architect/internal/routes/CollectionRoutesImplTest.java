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

import static com.liferay.apio.architect.internal.action.Predicates.isCreateAction;
import static com.liferay.apio.architect.internal.action.Predicates.isRetrieveAction;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.FORM_BUILDER_FUNCTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.FORM_BUILDER_SUPPLIER;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.GET_CUSTOM_ROUTE;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IDENTIFIER_FUNCTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IS_BATCH_CREATE_ACTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IS_READ_ACTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.IS_WRITE_ACTION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.PAGINATION;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.POST_CUSTOM_ROUTE;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.filterActionSemantics;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.getParams;
import static com.liferay.apio.architect.internal.routes.RoutesTestUtil.prependWith;
import static com.liferay.apio.architect.internal.util.matcher.FailsWith.failsWith;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import com.liferay.apio.architect.annotation.EntryPoint;
import com.liferay.apio.architect.batch.BatchResult;
import com.liferay.apio.architect.form.Body;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.routes.CollectionRoutesImpl.BuilderImpl;
import com.liferay.apio.architect.internal.routes.RoutesTestUtil.CustomIdentifier;
import com.liferay.apio.architect.pagination.Page;
import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.resource.Resource.Paged;
import com.liferay.apio.architect.routes.CollectionRoutes;
import com.liferay.apio.architect.single.model.SingleModel;

import io.vavr.control.Try;

import java.lang.annotation.Annotation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class CollectionRoutesImplTest {

	@Before
	public void setUp() {
		_builder = new BuilderImpl<>(
			Paged.of("name"), FORM_BUILDER_SUPPLIER, IDENTIFIER_FUNCTION,
			__ -> Optional.of("custom"));
	}

	@Test
	public void testCollectionRoutesDeprecatedMethodsThrowsException() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.build();

		assertThat(
			collectionRoutes::getBatchCreateItemFunctionOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			collectionRoutes::getCreateItemFunctionOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			collectionRoutes::getCustomPageFunctionsOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			collectionRoutes::getCustomRoutes,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			collectionRoutes::getGetPageFunctionOptional,
			failsWith(UnsupportedOperationException.class));

		assertThat(
			collectionRoutes::getFormOptional,
			failsWith(UnsupportedOperationException.class));
	}

	@Test
	public void testEmptyBuilderDoesNotGenerateActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		CollectionRoutesImpl<String, Long> collectionRoutesImpl =
			(CollectionRoutesImpl<String, Long>)collectionRoutes;

		assertThat(collectionRoutesImpl.getActionSemantics(), is(empty()));
	}

	@Test
	public void testFiveParameterBuilderMethodsCreatesActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.addCreator(
			this::_testAndReturnFourParameterCreatorRoute, String.class,
			Long.class, Boolean.class, Integer.class,
			__ -> true, FORM_BUILDER_FUNCTION
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnFourParameterCustomRoute,
			String.class, Long.class, Boolean.class, Integer.class,
			CustomIdentifier.class, __ -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnFourParameterCustomRoute,
			String.class, Long.class, Boolean.class, Integer.class,
			CustomIdentifier.class, __ -> true, FORM_BUILDER_FUNCTION
		).addGetter(
			this::_testAndReturnFourParameterGetterRoute, String.class,
			Long.class, Boolean.class, Integer.class
		).build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		_testActionSemantics(
			(CollectionRoutesImpl<String, Long>)collectionRoutes,
			asList(String.class, Long.class, Boolean.class, Integer.class));
	}

	@Test
	public void testFourParameterBuilderMethodsCreatesActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.addCreator(
			this::_testAndReturnThreeParameterCreatorRoute, String.class,
			Long.class, Boolean.class, __ -> true, FORM_BUILDER_FUNCTION
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnThreeParameterCustomRoute,
			String.class, Long.class, Boolean.class, CustomIdentifier.class,
			__ -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnThreeParameterCustomRoute,
			String.class, Long.class, Boolean.class, CustomIdentifier.class,
			__ -> true, FORM_BUILDER_FUNCTION
		).addGetter(
			this::_testAndReturnThreeParameterGetterRoute, String.class,
			Long.class, Boolean.class
		).build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		_testActionSemantics(
			(CollectionRoutesImpl<String, Long>)collectionRoutes,
			asList(String.class, Long.class, Boolean.class, Void.class));
	}

	@Test
	public void testOneParameterBuilderMethodsCreatesActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.addCreator(
			this::_testAndReturnNoParameterCreatorRoute,
			__ -> true, FORM_BUILDER_FUNCTION
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnNoParameterCustomRoute,
			CustomIdentifier.class, __ -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnNoParameterCustomRoute,
			CustomIdentifier.class, __ -> true, FORM_BUILDER_FUNCTION
		).addGetter(
			this::_testAndReturnNoParameterGetterRoute
		).build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		_testActionSemantics(
			(CollectionRoutesImpl<String, Long>)collectionRoutes,
			asList(Void.class, Void.class, Void.class, Void.class));
	}

	@Test
	public void testThreeParameterBuilderMethodsCreatesActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.addCreator(
			this::_testAndReturnTwoParameterCreatorRoute, String.class,
			Long.class, __ -> true, FORM_BUILDER_FUNCTION
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnTwoParameterCustomRoute,
			String.class, Long.class, CustomIdentifier.class, __ -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnTwoParameterCustomRoute,
			String.class, Long.class, CustomIdentifier.class,
			__ -> true, FORM_BUILDER_FUNCTION
		).addGetter(
			this::_testAndReturnTwoParameterGetterRoute, String.class,
			Long.class
		).build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		_testActionSemantics(
			(CollectionRoutesImpl<String, Long>)collectionRoutes,
			asList(String.class, Long.class, Void.class, Void.class));
	}

	@Test
	public void testTwoParameterBuilderMethodsCreatesActionSemantics() {
		CollectionRoutes<String, Long> collectionRoutes = _builder.addCreator(
			this::_testAndReturnOneParameterCreatorRoute, String.class,
			__ -> true, FORM_BUILDER_FUNCTION
		).addCustomRoute(
			GET_CUSTOM_ROUTE, this::_testAndReturnOneParameterCustomRoute,
			String.class, CustomIdentifier.class, __ -> true, null
		).addCustomRoute(
			POST_CUSTOM_ROUTE, this::_testAndReturnOneParameterCustomRoute,
			String.class, CustomIdentifier.class,
			__ -> true, FORM_BUILDER_FUNCTION
		).addGetter(
			this::_testAndReturnOneParameterGetterRoute, String.class
		).build();

		assertThat(collectionRoutes, instanceOf(CollectionRoutesImpl.class));

		_testActionSemantics(
			(CollectionRoutesImpl<String, Long>)collectionRoutes,
			asList(String.class, Void.class, Void.class, Void.class));
	}

	private void _testActionSemantics(
		CollectionRoutesImpl<String, Long> collectionRoutesImpl,
		List<Class<?>> paramClasses) {

		List<ActionSemantics> actionSemantics =
			collectionRoutesImpl.getActionSemantics();

		assertThat(actionSemantics.size(), is(5));

		_testRetrieveActionSemantics(
			prependWith(paramClasses, Pagination.class),
			filterActionSemantics(actionSemantics, isRetrieveAction));

		_testRouteActionSemantics(
			prependWith(paramClasses, Body.class),
			filterActionSemantics(actionSemantics, isCreateAction), "POST",
			"create", "name");

		_testBatchCreateActionSemantics(
			prependWith(paramClasses, Body.class),
			filterActionSemantics(actionSemantics, IS_BATCH_CREATE_ACTION));

		_testRouteActionSemantics(
			prependWith(paramClasses, Pagination.class, Void.class),
			filterActionSemantics(actionSemantics, IS_READ_ACTION), "GET",
			"read", "custom");

		_testRouteActionSemantics(
			prependWith(paramClasses, Pagination.class, Body.class),
			filterActionSemantics(actionSemantics, IS_WRITE_ACTION), "POST",
			"write", "custom");
	}

	private String _testAndReturnFourParameterCreatorRoute(
		Map<String, Object> body, String string, Long aLong, Boolean aBoolean,
		Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterCreatorRoute(
			body, string, aLong, aBoolean);
	}

	private String _testAndReturnFourParameterCustomRoute(
		Pagination pagination, Map<String, Object> body, String string,
		Long aLong, Boolean aBoolean, Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterCustomRoute(
			pagination, body, string, aLong, aBoolean);
	}

	private PageItems<String> _testAndReturnFourParameterGetterRoute(
		Pagination pagination, String string, Long aLong, Boolean aBoolean,
		Integer integer) {

		assertThat(integer, is(2017));

		return _testAndReturnThreeParameterGetterRoute(
			pagination, string, aLong, aBoolean);
	}

	private String _testAndReturnNoParameterCreatorRoute(
		Map<String, Object> body) {

		assertThat(body.get("key"), is("Apio"));

		return "Apio";
	}

	private String _testAndReturnNoParameterCustomRoute(
		Pagination pagination, Map<String, Object> body) {

		assertThat(pagination, is(PAGINATION));

		if (body != null) {
			assertThat(body.get("key"), is("Apio"));
		}

		return "Apio";
	}

	private PageItems<String> _testAndReturnNoParameterGetterRoute(
		Pagination pagination) {

		assertThat(pagination, is(PAGINATION));

		return new PageItems<>(singletonList("Apio"), 1);
	}

	private String _testAndReturnOneParameterCreatorRoute(
		Map<String, Object> body, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterCreatorRoute(body);
	}

	private String _testAndReturnOneParameterCustomRoute(
		Pagination pagination, Map<String, Object> body, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterCustomRoute(pagination, body);
	}

	private PageItems<String> _testAndReturnOneParameterGetterRoute(
		Pagination pagination, String string) {

		assertThat(string, is("Apio"));

		return _testAndReturnNoParameterGetterRoute(pagination);
	}

	private String _testAndReturnThreeParameterCreatorRoute(
		Map<String, Object> body, String string, Long aLong, Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterCreatorRoute(body, string, aLong);
	}

	private String _testAndReturnThreeParameterCustomRoute(
		Pagination pagination, Map<String, Object> body, String string,
		Long aLong, Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterCustomRoute(
			pagination, body, string, aLong);
	}

	private PageItems<String> _testAndReturnThreeParameterGetterRoute(
		Pagination pagination, String string, Long aLong, Boolean aBoolean) {

		assertThat(aBoolean, is(true));

		return _testAndReturnTwoParameterGetterRoute(pagination, string, aLong);
	}

	private String _testAndReturnTwoParameterCreatorRoute(
		Map<String, Object> body, String string, Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterCreatorRoute(body, string);
	}

	private String _testAndReturnTwoParameterCustomRoute(
		Pagination pagination, Map<String, Object> body, String string,
		Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterCustomRoute(pagination, body, string);
	}

	private PageItems<String> _testAndReturnTwoParameterGetterRoute(
		Pagination pagination, String string, Long aLong) {

		assertThat(aLong, is(42L));

		return _testAndReturnOneParameterGetterRoute(pagination, string);
	}

	private void _testBatchCreateActionSemantics(
		List<Class<?>> paramClasses, ActionSemantics actionSemantics) {

		assertThat(actionSemantics.getHTTPMethod(), is("POST"));
		assertThat(actionSemantics.getActionName(), is("batch-create"));
		assertThat(actionSemantics.getParamClasses(), is(paramClasses));
		assertThat(actionSemantics.getResource(), is(Paged.of("name")));
		assertThat(
			actionSemantics.getReturnClass(), is(equalTo(BatchResult.class)));
		assertThat(actionSemantics.getAnnotations(), hasSize(0));

		BatchResult<?> batchResult = Try.of(
			() -> actionSemantics.execute(
				getParams(actionSemantics, paramClasses))
		).map(
			BatchResult.class::cast
		).get();

		assertThat(batchResult.getIdentifiers(), contains(42L, 42L));
	}

	private void _testRetrieveActionSemantics(
		List<Class<?>> paramClasses, ActionSemantics actionSemantics) {

		assertThat(actionSemantics.getHTTPMethod(), is("GET"));
		assertThat(actionSemantics.getActionName(), is("retrieve"));
		assertThat(actionSemantics.getParamClasses(), is(paramClasses));
		assertThat(actionSemantics.getResource(), is(Paged.of("name")));
		assertThat(actionSemantics.getReturnClass(), is(equalTo(Page.class)));
		assertThat(actionSemantics.getAnnotations(), hasSize(1));

		List<Annotation> annotations = actionSemantics.getAnnotations();

		Annotation annotation = annotations.get(0);

		assertThat(annotation.annotationType(), is(equalTo(EntryPoint.class)));

		Page<?> page = Try.of(
			() -> actionSemantics.execute(
				getParams(actionSemantics, paramClasses))
		).map(
			Page.class::cast
		).get();

		assertThat(page.getItems(), contains("Apio"));
		assertThat(page.getItemsPerPage(), is(10));
		assertThat(page.getLastPageNumber(), is(1));
		assertThat(page.getPageNumber(), is(1));
		assertThat(page.getResourceName(), is("name"));
		assertThat(page.getTotalCount(), is(1));
	}

	private void _testRouteActionSemantics(
		List<Class<?>> paramClasses, ActionSemantics actionSemantics,
		String method, String actionName, String resourceName) {

		assertThat(actionSemantics.getHTTPMethod(), is(method));
		assertThat(actionSemantics.getActionName(), is(actionName));
		assertThat(actionSemantics.getParamClasses(), is(paramClasses));
		assertThat(actionSemantics.getResource(), is(Paged.of("name")));
		assertThat(
			actionSemantics.getReturnClass(), is(equalTo(SingleModel.class)));
		assertThat(actionSemantics.getAnnotations(), hasSize(0));

		SingleModel<?> singleModel = Try.of(
			() -> actionSemantics.execute(
				getParams(actionSemantics, paramClasses))
		).map(
			SingleModel.class::cast
		).get();

		assertThat(singleModel.getModel(), is("Apio"));
		assertThat(singleModel.getOperations(), is(empty()));
		assertThat(singleModel.getResourceName(), is(resourceName));
	}

	private BuilderImpl<String, Long> _builder;

}
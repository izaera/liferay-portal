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

package com.liferay.apio.architect.internal.action;

import static java.util.Arrays.asList;

import com.liferay.apio.architect.annotation.EntryPoint;
import com.liferay.apio.architect.pagination.Page;
import com.liferay.apio.architect.resource.Resource;
import com.liferay.apio.architect.resource.Resource.Paged;

import java.lang.annotation.Annotation;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class contains several {@link ActionSemantics}-related predicates that
 * can be used to test conditions or filter lists.
 *
 * <p>
 * This class should not be instantiated.
 * </p>
 *
 * @author Alejandro Hernández
 * @review
 */
public class Predicates {

	/**
	 * Checks if an action's method is {@code DELETE}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isActionByDELETE =
		isActionBy("DELETE");

	/**
	 * Checks if an action's method is {@code GET}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isActionByGET = isActionBy(
		"GET");

	/**
	 * Checks if an action's method is {@code PATCH}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isActionByPATCH = isActionBy(
		"PATCH");

	/**
	 * Checks if an action's method is {@code POST}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isActionByPOST = isActionBy(
		"POST");

	/**
	 * Checks if an action's method is {@code PUT}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isActionByPUT = isActionBy(
		"PUT");

	/**
	 * Checks if an action's method is {@code POST} and its name is {@code
	 * create}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isCreateAction =
		isActionByPOST.and(isActionNamed("create"));

	/**
	 * Checks if an action's method is {@code DELETE} and its name is {@code
	 * remove}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isRemoveAction =
		isActionByDELETE.and(isActionNamed("remove"));

	/**
	 * Checks if an action's method is {@code PUT} and its name is {@code
	 * replace}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isReplaceAction =
		isActionByPUT.and(isActionNamed("replace"));

	/**
	 * Checks if an action's method is {@code GET} and its name is {@code
	 * retrieve}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isRetrieveAction =
		isActionByGET.and(isActionNamed("retrieve"));

	/**
	 * Checks if an action is a root collection action.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isRootCollectionAction =
		isRetrieveAction.and(
			returnsAnyOf(Page.class)
		).and(
			hasAnnotation(EntryPoint.class)
		).and(
			isActionFor(Paged.class)
		);

	/**
	 * Checks if an action's method is {@code PATCH} and its name is {@code
	 * update}.
	 *
	 * @review
	 */
	public static final Predicate<ActionSemantics> isUpdateAction =
		isActionByPATCH.and(isActionNamed("update"));

	/**
	 * Transforms the object under testing with the provided function and checks
	 * equality between the result and the provided object.
	 *
	 * @param  transformFunction the function used to transform the object
	 * @param  s the object to check equality with
	 * @return {@code true} if the result of applying the function and the
	 *         provided object are equals; {@code false} otherwise
	 * @review
	 */
	public static <T, S> Predicate<T> areEquals(
		Function<T, S> transformFunction, S s) {

		return t -> transformFunction.andThen(
			s::equals
		).apply(
			t
		);
	}

	/**
	 * Checks if an action is annotated with a certain {@link Annotation}.
	 *
	 * @review
	 */
	public static <A extends Annotation> Predicate<ActionSemantics>
		hasAnnotation(Class<A> annotation) {

		return actionSemantics -> Stream.of(
			actionSemantics.getAnnotations()
		).flatMap(
			List::stream
		).map(
			Annotation::annotationType
		).anyMatch(
			annotation::isAssignableFrom
		);
	}

	/**
	 * Returns a predicate that checks if the action's name and method are
	 * equals to the ones provided.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> isAction(
		String name, String method) {

		return isActionNamed(name).and(isActionBy(method));
	}

	/**
	 * Checks if an action's method is the one provided.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> isActionBy(String method) {
		return areEquals(ActionSemantics::getHTTPMethod, method);
	}

	/**
	 * Returns a predicate that checks if the action's resource is an instance
	 * of the provided resource class.
	 *
	 * @review
	 */
	public static <R extends Resource> Predicate<ActionSemantics> isActionFor(
		Class<R> clazz) {

		return actionSemantics -> clazz.isInstance(
			actionSemantics.getResource());
	}

	/**
	 * Returns a predicate that checks if the action's resource is equals to the
	 * one provided.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> isActionFor(Resource resource) {
		return isActionForAny(resource);
	}

	/**
	 * Returns a predicate that checks if the action's resource is equals to any
	 * of the provided.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> isActionForAny(
		Resource... resources) {

		return actionSemantics -> Stream.of(
			resources
		).anyMatch(
			resource -> resource.equals(actionSemantics.getResource())
		);
	}

	/**
	 * Returns a predicate that checks if the action's name is equals to the one
	 * provided.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> isActionNamed(String name) {
		return actionSemantics -> name.equals(actionSemantics.getActionName());
	}

	/**
	 * Returns a predicate that checks if the action's return class is any of
	 * the provided ones.
	 *
	 * @review
	 */
	public static Predicate<ActionSemantics> returnsAnyOf(Class<?>... classes) {
		return actionSemantics -> {
			List<Class<?>> list = asList(classes);

			return list.contains(actionSemantics.getReturnClass());
		};
	}

	private Predicates() {
		throw new UnsupportedOperationException();
	}

}
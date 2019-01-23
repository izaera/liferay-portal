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

package com.liferay.apio.architect.internal.test.base;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.Predicates.is;

import static java.util.function.Function.identity;

import static org.osgi.service.jaxrs.runtime.JaxrsServiceRuntimeConstants.JAX_RS_SERVICE_ENDPOINT;
import static org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants.JAX_RS_APPLICATION_SELECT;
import static org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants.JAX_RS_RESOURCE;

import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.control.Try;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.jaxrs.runtime.JaxrsServiceRuntime;
import org.osgi.util.promise.Promise;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Provides utility methods for basic whiteboard and Apio operations and checks.
 * This class is the base class for integration tests.
 *
 * @author Alejandro Hernández
 */
public class BaseTest {

	@BeforeClass
	public static void setUpClass() {
		ServiceReference<ServiceComponentRuntime> serviceReference =
			_bundleContext.getServiceReference(ServiceComponentRuntime.class);

		_serviceComponentRuntime = _bundleContext.getService(serviceReference);
	}

	@AfterClass
	public static void tearDownClass() {
		_iterateAndExecute(
			_classRegistrations, ServiceRegistration::unregister);
		_iterateAndExecute(
			_classedDisabledImplementations,
			_update(_serviceComponentRuntime::enableComponent));
	}

	@Before
	public void setUp() {
		_clientBuilderTracker = new ServiceTracker<>(
			_bundleContext, ClientBuilder.class, null);

		_clientBuilderTracker.open();

		_runtimeServiceReference = _bundleContext.getServiceReference(
			JaxrsServiceRuntime.class);
	}

	@After
	public void tearDown() {
		_clientBuilderTracker.close();
		_iterateAndExecute(_registrations, ServiceRegistration::unregister);
		_iterateAndExecute(
			_disabledImplementations,
			_update(_serviceComponentRuntime::enableComponent));
	}

	/**
	 * Tries to read a response's entity as a {@link JSONObject}.
	 *
	 * @review
	 */
	protected static JSONObject asJSONObject(Response response) {
		String entity = response.readEntity(String.class);

		return Try.of(
			() -> new JSONObject(entity)
		).getOrElseThrow(
			t -> new AssertionError(
				"Unable to create JSON object out of entity: " + entity, t)
		);
	}

	/**
	 * Registers a new service with the provided properties. Returns the service
	 * registration.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific class test lifecycle
	 * (inside methods annotated with {@link BeforeClass} or {@link AfterClass}.
	 * </p>
	 *
	 * @param  serviceClass the service class
	 * @param  service the service
	 * @param  properties the service properties
	 * @return the service registration
	 * @review
	 */
	protected static <T> ServiceRegistration<T>
		beforeClassRegisterImplementationFor(
			Class<T> serviceClass, T service, Map<String, Object> properties) {

		ServiceRegistration<T> serviceRegistration =
			_bundleContext.registerService(
				serviceClass, service,
				properties.toJavaMap(Hashtable::new, identity()));

		_classRegistrations.add(serviceRegistration);

		return serviceRegistration;
	}

	/**
	 * Registers a new service as a JAX-RS resource with the provided
	 * properties. Returns the service registration.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific class test lifecycle
	 * (inside methods annotated with {@link BeforeClass} or {@link AfterClass}.
	 * </p>
	 *
	 * @param  service the JAX-RS resource
	 * @param  properties the service properties
	 * @return the service registration
	 * @review
	 */
	protected static ServiceRegistration<?> beforeClassRegisterResource(
		Object service, Map<String, Object> properties) {

		return beforeClassRegisterImplementationFor(
			Object.class, service,
			properties.merge(_defaultResourceProperties));
	}

	/**
	 * Unregister the default implementation for a given service class. The
	 * implementation is expected to be present in the same bundle as the
	 * service class.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific class test lifecycle
	 * (inside methods annotated with {@link BeforeClass} or {@link AfterClass}.
	 * </p>
	 *
	 * @param  serviceClass the service class
	 * @review
	 */
	protected static <T> void beforeClassUnregisterImplementationFor(
		Class<T> serviceClass) {

		ComponentDescriptionDTO componentDescriptionDTO =
			_unregisterImplementationFor(serviceClass);

		_classedDisabledImplementations.add(componentDescriptionDTO);
	}

	/**
	 * Creates a {@link JSONObject} by populating a map with data.
	 *
	 * @review
	 */
	protected static JSONObject createJSONObject(
		Function1<Map<String, Object>, Map<String, Object>> function) {

		return function.andThen(
			Map::toJavaMap
		).andThen(
			JSONObject::new
		).apply(
			HashMap.empty()
		);
	}

	/**
	 * Registers a new service with the provided properties. Returns the service
	 * registration.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific test lifecycle
	 * (inside methods annotated with {@link Before}, {@link After} or {@link
	 * org.junit.Test}.
	 * </p>
	 *
	 * @param  serviceClass the service class
	 * @param  service the service
	 * @param  properties the service properties
	 * @return the service registration
	 * @review
	 */
	protected <T> ServiceRegistration<T> beforeTestRegisterAs(
		Class<T> serviceClass, T service, Map<String, Object> properties) {

		ServiceRegistration<T> serviceRegistration =
			_bundleContext.registerService(
				serviceClass, service,
				properties.toJavaMap(Hashtable::new, identity()));

		_registrations.add(serviceRegistration);

		return serviceRegistration;
	}

	/**
	 * Registers a new service as a JAX-RS resource with the provided
	 * properties. Returns the service registration.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific test lifecycle
	 * (inside methods annotated with {@link Before}, {@link After} or {@link
	 * org.junit.Test}.
	 * </p>
	 *
	 * @param  service the JAX-RS resource
	 * @param  properties the service properties
	 * @return the service registration
	 * @review
	 */
	protected ServiceRegistration<?> beforeTestRegisterResource(
		Object service, Map<String, Object> properties) {

		return beforeTestRegisterAs(
			Object.class, service,
			properties.merge(_defaultResourceProperties));
	}

	/**
	 * Unregister the default implementation for a given service class. The
	 * implementation is expected to be present in the same bundle as the
	 * service class.
	 *
	 * <p>
	 * Warning! This method must be only used in a specific test lifecycle
	 * (inside methods annotated with {@link Before}, {@link After} or {@link
	 * org.junit.Test}.
	 * </p>
	 *
	 * @param  serviceClass the service class
	 * @review
	 */
	protected <T> void beforeTestUnregisterImplementationFor(
		Class<T> serviceClass) {

		ComponentDescriptionDTO componentDescriptionDTO =
			_unregisterImplementationFor(serviceClass);

		_disabledImplementations.add(componentDescriptionDTO);
	}

	/**
	 * Returns a new {@code ClientBuilder} instance by using the bundle context
	 * to retrieve the registered client builder implementation.
	 */
	protected Client createClient() {
		return Try.of(
			() -> _clientBuilderTracker.waitForService(5000)
		).map(
			ClientBuilder::build
		).getOrElseThrow(
			t -> new AssertionError("Unable to create a valid ClientBuilder", t)
		);
	}

	/**
	 * Creates a {@link WebTarget} containing the path in which the JAX-RS
	 * Whiteboard is listening to requests.
	 *
	 * @review
	 */
	protected WebTarget createDefaultTarget() {
		Client client = createClient();

		return client.target(getJAXRSServiceEndpoint());
	}

	/**
	 * Return the endpoint were the JAX-RS whiteboard is listening.
	 *
	 * @review
	 */
	protected String getJAXRSServiceEndpoint() {
		return Try.of(
			() -> _runtimeServiceReference.getProperty(JAX_RS_SERVICE_ENDPOINT)
		).map(
			_TO_LIST
		).filter(
			endpoints -> endpoints.size() != 0,
			() -> new IllegalStateException(
				"Unable to find endpoints in \"" + JAX_RS_SERVICE_ENDPOINT +
					"\" property")
		).map(
			Traversable::head
		).getOrElseThrow(
			t -> new AssertionError("Unable to create a valid WebTarget", t)
		);
	}

	/**
	 * This constant should be used when no properties are required on
	 * register/unregister methods.
	 *
	 * @see    #beforeClassRegisterImplementationFor(Class, Object, Map)
	 * @see    #beforeClassRegisterResource(Object, Map)
	 * @see    #beforeTestRegisterAs(Class, Object, Map)
	 * @see    #beforeTestRegisterResource(Object, Map)
	 * @review
	 */
	protected static final Map<String, Object> noProperties = HashMap.empty();

	private static <T> void _iterateAndExecute(
		Collection<T> collection, Consumer<T> consumer) {

		Iterator<T> iterator = collection.iterator();

		while (iterator.hasNext()) {
			Try.of(
				iterator::next
			).andThen(
				consumer
			).andFinally(
				iterator::remove
			);
		}
	}

	private static <T> ComponentDescriptionDTO _unregisterImplementationFor(
		Class<T> serviceClass) {

		ServiceReference<T> serviceReference =
			_bundleContext.getServiceReference(serviceClass);

		String implementationClassName = (String)serviceReference.getProperty(
			"component.name");

		Bundle bundle = FrameworkUtil.getBundle(serviceClass);

		ComponentDescriptionDTO componentDescriptionDTO =
			_serviceComponentRuntime.getComponentDescriptionDTO(
				bundle, implementationClassName);

		_update(
			_serviceComponentRuntime::disableComponent
		).accept(
			componentDescriptionDTO
		);

		return componentDescriptionDTO;
	}

	private static <C extends ComponentDescriptionDTO> Consumer<C> _update(
		Function1<C, Promise<Void>> updateFunction) {

		return c -> Try.of(
			() -> updateFunction.apply(c)
		).andThenTry(
			Promise::getValue
		).onFailure(
			t -> {
				throw new AssertionError(
					"Unable to update component " + c.name, t);
			}
		);
	}

	@SuppressWarnings({"Convert2MethodRef", "unchecked"})
	private static final Function<Object, List<String>> _TO_LIST = v -> Match(
		v
	).of(
		Case($(is(null)), List.empty()),
		Case($(instanceOf(String[].class)), array -> List.of(array)),
		Case($(instanceOf(Iterable.class)), iterable -> List.ofAll(iterable)),
		Case($(), value -> List.of(value.toString()))
	);

	private static final BundleContext _bundleContext = FrameworkUtil.getBundle(
		BaseTest.class).getBundleContext();
	private static final Collection<ComponentDescriptionDTO>
		_classedDisabledImplementations = new ArrayList<>();
	private static final Collection<ServiceRegistration<?>>
		_classRegistrations = new ArrayList<>();
	private static final Map<String, Object> _defaultResourceProperties =
		HashMap.of(
			JAX_RS_APPLICATION_SELECT,
			"(liferay.apio.architect.application=true)", JAX_RS_RESOURCE,
			"true");
	private static ServiceComponentRuntime _serviceComponentRuntime;

	private ServiceTracker<ClientBuilder, ClientBuilder> _clientBuilderTracker;
	private final Collection<ComponentDescriptionDTO> _disabledImplementations =
		new ArrayList<>();
	private final Collection<ServiceRegistration<?>> _registrations =
		new ArrayList<>();
	private ServiceReference<JaxrsServiceRuntime> _runtimeServiceReference;

}
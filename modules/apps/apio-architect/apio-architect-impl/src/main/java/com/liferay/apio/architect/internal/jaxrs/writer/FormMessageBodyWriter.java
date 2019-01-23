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

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.form.FormField;
import com.liferay.apio.architect.internal.url.ApplicationURL;
import com.liferay.apio.architect.internal.wiring.osgi.manager.provider.ProviderManager;
import com.liferay.apio.architect.language.AcceptLanguage;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Writes {@link Form}
 *
 * @author Alejandro Hernández
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(liferay.apio.architect.application=true)",
		"osgi.jaxrs.extension=true"
	},
	service = MessageBodyWriter.class
)
@Produces("application/ld+json")
@Provider
public class FormMessageBodyWriter implements MessageBodyWriter<Form<?>> {

	@Override
	public boolean isWriteable(
		Class<?> type, Type genericType, Annotation[] annotations,
		MediaType mediaType) {

		return Form.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(
			Form<?> form, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream outputStream)
		throws WebApplicationException {

		httpHeaders.put(CONTENT_TYPE, singletonList("application/ld+json"));

		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
			outputStream, StandardCharsets.UTF_8);

		PrintWriter printWriter = new PrintWriter(outputStreamWriter, true);

		AcceptLanguage acceptLanguage = _providerManager.provideMandatory(
			_request, AcceptLanguage.class);

		ApplicationURL applicationURL = _providerManager.provideMandatory(
			_request, ApplicationURL.class);

		JSONObject jsonObject = toJSONObject(
			form, acceptLanguage, applicationURL, true);

		jsonObject.write(printWriter);

		printWriter.close();
	}

	/**
	 * Returns the {@link Form} URL by expanding its URI.
	 *
	 * @review
	 */
	protected static String getURLFor(
		Form<?> form, ApplicationURL applicationURL) {

		return Optional.ofNullable(
			form.getId()
		).map(
			uri -> applicationURL.get() + uri
		).orElse(
			null
		);
	}

	/**
	 * Transforms a list of {@link FormField} into a {@link JSONArray} of
	 * JSON-LD {@code supportedProperty}.
	 *
	 * @see    <a
	 *         href="https://www.w3.org/ns/hydra/core#SupportedProperty">SupportedProperty</a>
	 * @review
	 */
	protected static JSONArray toJSONArray(
		List<FormField> formFields, AcceptLanguage acceptLanguage,
		ApplicationURL applicationURL) {

		JSONArray jsonArray = new JSONArray();

		formFields.forEach(
			formField -> {
				Form form = formField.getForm();

				if (form == null) {
					jsonArray.put(toJSONObject(formField, null));
				}
				else {
					JSONObject jsonObject = toJSONObject(
						form, acceptLanguage, applicationURL, false);

					jsonArray.put(toJSONObject(formField, jsonObject));
				}
			});

		return jsonArray;
	}

	/**
	 * Transforms a {@link Form} into its {@link JSONObject}'s view.
	 *
	 * @review
	 */
	protected static JSONObject toJSONObject(
		Form<?> form, AcceptLanguage acceptLanguage,
		ApplicationURL applicationURL, boolean includeContext) {

		return new JSONObject().putOpt(
			"@context", includeContext ? _contextJSONArray : null
		).put(
			"@type", "Class"
		).putOpt(
			"@id", getURLFor(form, applicationURL)
		).put(
			"title", form.getTitle(acceptLanguage)
		).put(
			"description", form.getDescription(acceptLanguage)
		).put(
			"supportedProperty",
			toJSONArray(form.getFormFields(), acceptLanguage, applicationURL)
		);
	}

	/**
	 * Transforms a {@link FormField} into its {@link JSONObject}'s view. If a
	 * {@code valueJSONObject} is provided, it will be stored as the field
	 * value.
	 *
	 * @review
	 */
	protected static JSONObject toJSONObject(
		FormField formField, JSONObject valueJSONObject) {

		return new JSONObject().put(
			"@type", "SupportedProperty"
		).put(
			"property", formField.getName()
		).put(
			"required", formField.isRequired()
		).put(
			"readable", false
		).put(
			"writeable", true
		).putOpt(
			"value", valueJSONObject
		);
	}

	private static final JSONArray _contextJSONArray = new JSONArray().put(
		new JSONObject(singletonMap("@vocab", "http://schema.org"))
	).put(
		"https://www.w3.org/ns/hydra/core#"
	);

	@Reference
	private ProviderManager _providerManager;

	@Context
	private HttpServletRequest _request;

}
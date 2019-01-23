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

package com.liferay.apio.architect.internal.writer;

import static com.liferay.apio.architect.internal.url.URLCreator.createAbsoluteURL;
import static com.liferay.apio.architect.internal.url.URLCreator.createActionURL;

import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.internal.action.ActionSemantics;
import com.liferay.apio.architect.internal.message.json.ActionMapper;
import com.liferay.apio.architect.internal.message.json.JSONObjectBuilder;
import com.liferay.apio.architect.internal.request.RequestInfo;

import java.util.Optional;

/**
 * Writes the actions of a resource using the methods of an {@link
 * ActionMapper}.
 *
 * @author Javier Gamarra
 * @review
 */
public class ActionWriter {

	public ActionWriter(
		ActionMapper actionMapper, RequestInfo requestInfo,
		JSONObjectBuilder jsonObjectBuilder) {

		_actionMapper = actionMapper;
		_requestInfo = requestInfo;
		_jsonObjectBuilder = jsonObjectBuilder;
	}

	public void write(ActionSemantics actionSemantics) {
		JSONObjectBuilder operationJSONObjectBuilder = new JSONObjectBuilder();

		Optional<String> optional = createActionURL(
			_requestInfo.getApplicationURL(), actionSemantics.getResource(),
			actionSemantics.getActionName());

		optional.ifPresent(
			url -> _actionMapper.mapActionSemanticsURL(
				operationJSONObjectBuilder, url));

		Optional<Form> formOptional = actionSemantics.getFormOptional();

		formOptional.map(
			Form::getId
		).map(
			uri -> createAbsoluteURL(_requestInfo.getApplicationURL(), uri)
		).ifPresent(
			url -> _actionMapper.mapActionSemanticsExpectedResourceURL(
				operationJSONObjectBuilder, url)
		);

		_actionMapper.mapHTTPMethod(
			operationJSONObjectBuilder, actionSemantics.getHTTPMethod());

		_actionMapper.onFinish(
			_jsonObjectBuilder, operationJSONObjectBuilder, actionSemantics);
	}

	private final ActionMapper _actionMapper;
	private final JSONObjectBuilder _jsonObjectBuilder;
	private final RequestInfo _requestInfo;

}
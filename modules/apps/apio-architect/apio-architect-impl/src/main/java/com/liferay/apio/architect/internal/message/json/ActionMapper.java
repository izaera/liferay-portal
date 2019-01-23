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

package com.liferay.apio.architect.internal.message.json;

import com.liferay.apio.architect.internal.action.ActionSemantics;

/**
 * Maps {@link ActionSemantics} data to its representation in a JSON object.
 * Instances of this interface work like events. The different {@code
 * javax.ws.rs.ext.MessageBodyWriter} instances call the {@code ActionMapper}
 * methods. In each method, developers should only map the provided part of the
 * resource to its representation in a JSON object. To enable this, each method
 * receives a {@link JSONObjectBuilder}.
 *
 * <p>
 * The method {@link #onFinish} is called when the writer finishes writing the
 * operation. Otherwise, the operation mapper's methods aren't called in a
 * particular order.
 * </p>
 *
 * @author Javier Gamarra
 * @review
 */
public interface ActionMapper {

	/**
	 * Maps a resource action's expected resource URL to its JSON object
	 * representation.
	 *
	 * @param  jsonObjectBuilder the JSON object builder for the action
	 * @param  url the expected resource url
	 * @review
	 */
	public default void mapActionSemanticsExpectedResourceURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {
	}

	/**
	 * Maps a resource action's url to its JSON object representation.
	 *
	 * @param  jsonObjectBuilder the JSON object builder for the action
	 * @param  url the action's url
	 * @review
	 */
	public default void mapActionSemanticsURL(
		JSONObjectBuilder jsonObjectBuilder, String url) {
	}

	/**
	 * Maps a resource action's method to its JSON object representation.
	 *
	 * @param  jsonObjectBuilder the JSON object builder for the action
	 * @param  httpMethod the operation's method
	 * @review
	 */
	public default void mapHTTPMethod(
		JSONObjectBuilder jsonObjectBuilder, String httpMethod) {
	}

	/**
	 * Finishes the action. This is the final action-mapper method the writer
	 * calls.
	 *
	 * @param  resourceJSONObjectBuilder the JSON object builder for the model
	 * @param  actionJSONObjectBuilder the JSON object builder for the action
	 * @param  actionSemantics the action semantics
	 * @review
	 */
	public default void onFinish(
		JSONObjectBuilder resourceJSONObjectBuilder,
		JSONObjectBuilder actionJSONObjectBuilder,
		ActionSemantics actionSemantics) {
	}

}
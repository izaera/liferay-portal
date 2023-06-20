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

package com.liferay.frontend.js.web.internal.js.importmaps.extender;

import com.liferay.frontend.js.importmaps.extender.JSImportMapsContributor;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = JSImportMapsContributor.class)
public class FrontendJsWebJSImportMapsContributor
	implements JSImportMapsContributor {

	@Override
	public JSONObject getImportMapsJSONObject(Locale locale) {
		JSONObject jsonObject = _importMapsJSONObjects.get(locale);

		if (jsonObject == null) {
			jsonObject = _jsonFactory.createJSONObject();

			jsonObject.put(
				"@liferay/lang/",
				"/o/js/lang/" + locale.getLanguage() + StringPool.SLASH);

			_importMapsJSONObjects.putIfAbsent(locale, jsonObject);

			jsonObject = _importMapsJSONObjects.get(locale);
		}

		return jsonObject;
	}

	@Activate
	protected void activate() {
	}

	private final ConcurrentHashMap<Locale, JSONObject> _importMapsJSONObjects =
		new ConcurrentHashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

}
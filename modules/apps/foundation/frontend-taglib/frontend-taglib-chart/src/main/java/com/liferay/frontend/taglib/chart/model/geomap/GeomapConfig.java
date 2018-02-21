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

package com.liferay.frontend.taglib.chart.model.geomap;

import com.liferay.frontend.taglib.chart.model.ChartObject;

/**
 * @author Julien Castelain
 */
public class GeomapConfig extends ChartObject {

	public GeomapColor getColor() {
		return get("color", GeomapColor.class);
	}

	public Object getData() {
		return get("data", Object.class);
	}

	public void setColor(GeomapColor color) {
		set("color", color);
	}

	public void setDataUrl(String url) {
		Object data = get("data", Object.class, false);

		if (data != null) {
			throw new IllegalStateException("data has already been set");
		}

		set("data", url);
	}

	public void setJson(Object jsonObj) {
		Object data = get("data", String.class, false);

		if (data != null) {
			throw new IllegalStateException("data has already been set");
		}

		set("data", jsonObj);
	}

}
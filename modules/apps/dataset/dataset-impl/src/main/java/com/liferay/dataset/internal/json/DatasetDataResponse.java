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

package com.liferay.dataset.internal.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Iván Zaera Avellón
 */
public class DatasetDataResponse {

	public DatasetDataResponse(
		List<DatasetDataResponseRow> datasetDataResponseRows, int totalCount) {

		_datasetDataResponseRows = datasetDataResponseRows;
		_totalCount = totalCount;
	}

	@JsonProperty("items")
	private final List<DatasetDataResponseRow> _datasetDataResponseRows;

	@JsonProperty("totalCount")
	private final int _totalCount;

}
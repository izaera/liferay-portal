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

package com.liferay.dataset.internal.ui.filter.date;

import com.liferay.dataset.ui.filter.DatasetFilter;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributor;
import com.liferay.dataset.ui.filter.date.BaseDateRangeDatasetFilter;
import com.liferay.dataset.ui.filter.date.DateDatasetFilterItem;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luca Pellizzon
 */
@Component(
	property = "dataset.filter.type=dateRange",
	service = DatasetFilterContextContributor.class
)
public class DateRangeClayDataSetFilterContextContributor
	implements DatasetFilterContextContributor {

	@Override
	public Map<String, Object> getDatasetFilterContext(
		DatasetFilter datasetFilter, Locale locale) {

		if (datasetFilter instanceof BaseDateRangeDatasetFilter) {
			return _serialize((BaseDateRangeDatasetFilter)datasetFilter);
		}

		return Collections.emptyMap();
	}

	private JSONObject _getJSONObject(
		DateDatasetFilterItem dateDatasetFilterItem) {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		jsonObject.put(
			"day", dateDatasetFilterItem.getDay()
		).put(
			"month", dateDatasetFilterItem.getMonth()
		).put(
			"year", dateDatasetFilterItem.getYear()
		);

		return jsonObject;
	}

	private Map<String, Object> _serialize(
		BaseDateRangeDatasetFilter baseDateRangeDatasetFilter) {

		return HashMapBuilder.<String, Object>put(
			"max",
			_getJSONObject(
				baseDateRangeDatasetFilter.getMaxDateDatasetFilterItem())
		).put(
			"min",
			_getJSONObject(
				baseDateRangeDatasetFilter.getMinDateDatasetFilterItem())
		).build();
	}

	@Reference
	private JSONFactory _jsonFactory;

}
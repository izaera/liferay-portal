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

package com.liferay.dataset.internal.ui.filter;

import com.liferay.dataset.ui.filter.DatasetFilter;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributor;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributorRegistry;
import com.liferay.dataset.ui.filter.DatasetFilterRegistry;
import com.liferay.dataset.ui.filter.DatasetFilterSerializer;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(service = DatasetFilterSerializer.class)
public class DatasetFilterSerializerImpl implements DatasetFilterSerializer {

	@Override
	public JSONArray serialize(String datasetDisplayName, Locale locale) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		// TODO: use clayDatasetFilter.getClass() instead of getClass()

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		List<DatasetFilter> datasetFilters =
			_datasetFilterRegistry.getDatasetFilters(datasetDisplayName);

		for (DatasetFilter datasetFilter : datasetFilters) {
			String label = LanguageUtil.get(
				resourceBundle, datasetFilter.getLabel());

			JSONObject jsonObject = JSONUtil.put(
				"id", datasetFilter.getId()
			).put(
				"label", label
			).put(
				"type", datasetFilter.getType()
			);

			List<DatasetFilterContextContributor>
				datasetFilterContextContributors =
					_datasetFilterContextContributorRegistry.
						getDatasetFilterContextContributors(
							datasetFilter.getType());

			for (DatasetFilterContextContributor
					datasetFilterContextContributor :
						datasetFilterContextContributors) {

				Map<String, Object> datasetFilterContext =
					datasetFilterContextContributor.getDatasetFilterContext(
						datasetFilter, locale);

				if (datasetFilterContext == null) {
					continue;
				}

				for (Map.Entry<String, Object> datasetFilterContextEntry :
						datasetFilterContext.entrySet()) {

					jsonObject.put(
						datasetFilterContextEntry.getKey(),
						datasetFilterContextEntry.getValue());
				}
			}

			jsonArray.put(jsonObject);
		}

		return jsonArray;
	}

	@Reference
	private DatasetFilterContextContributorRegistry
		_datasetFilterContextContributorRegistry;

	@Reference
	private DatasetFilterRegistry _datasetFilterRegistry;

	@Reference
	private JSONFactory _jsonFactory;

}
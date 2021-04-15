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

package com.liferay.dataset.taglib.internal.json;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributor;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributorRegistry;
import com.liferay.dataset.ui.view.DatasetViewRegistry;
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
 * @author Iván Zaera Avellón
 */
@Component(service = DataSetViewsContextJSONFactory.class)
public class DataSetViewsContextJSONFactory {

	public JSONArray createJSONArray(String datasetDisplayName, Locale locale) {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		List<DatasetView> datasetViews = _datasetViewRegistry.getDatasetViews(
			datasetDisplayName);

		for (DatasetView datasetView : datasetViews) {
			ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
				"content.Language", locale, datasetView.getClass());

			JSONObject jsonObject = JSONUtil.put(
				"contentRenderer", datasetView.getContentRendererName()
			).put(
				"contentRendererModuleURL",
				datasetView.getContentRendererModuleURL()
			).put(
				"label",
				LanguageUtil.get(resourceBundle, datasetView.getLabel())
			).put(
				"name", datasetView.getName()
			).put(
				"thumbnail", datasetView.getThumbnail()
			);

			List<DatasetViewContentRendererContextContributor>
				datasetViewContentRendererContextContributors =
					_datasetViewContentRendererContextContributorRegistry.
						getDatasetViewContentRendererContextContributors(
							datasetView.getContentRendererName());

			for (DatasetViewContentRendererContextContributor
					datasetViewContentRendererContextContributor :
						datasetViewContentRendererContextContributors) {

				Map<String, Object> contentRendererContext =
					datasetViewContentRendererContextContributor.
						getContentRendererContext(datasetView, locale);

				if (contentRendererContext == null) {
					continue;
				}

				for (Map.Entry<String, Object> contentRendererContextEntry :
						contentRendererContext.entrySet()) {

					jsonObject.put(
						contentRendererContextEntry.getKey(),
						contentRendererContextEntry.getValue());
				}
			}

			jsonArray.put(jsonObject);
		}

		return jsonArray;
	}

	@Reference
	private DatasetViewContentRendererContextContributorRegistry
		_datasetViewContentRendererContextContributorRegistry;

	@Reference
	private DatasetViewRegistry _datasetViewRegistry;

	@Reference
	private JSONFactory _jsonFactory;

}
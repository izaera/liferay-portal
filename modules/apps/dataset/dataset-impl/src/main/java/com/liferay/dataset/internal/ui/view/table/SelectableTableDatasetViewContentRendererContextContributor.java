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

package com.liferay.dataset.internal.ui.view.table;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributor;
import com.liferay.dataset.ui.view.DatasetViewContentRendererNames;
import com.liferay.dataset.ui.view.table.BaseSelectableTableDatasetView;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "dataset.view.content.renderer.name=" + DatasetViewContentRendererNames.SELECTABLE_TABLE,
	service = DatasetViewContentRendererContextContributor.class
)
public class SelectableTableDatasetViewContentRendererContextContributor
	implements DatasetViewContentRendererContextContributor {

	@Override
	public Map<String, Object> getContentRendererContext(
		DatasetView datasetView, Locale locale) {

		if (datasetView instanceof BaseSelectableTableDatasetView) {
			return _serialize(
				(BaseSelectableTableDatasetView)datasetView, locale);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseSelectableTableDatasetView baseSelectableTableDatasetView,
		Locale locale) {

		return HashMapBuilder.<String, Object>put(
			"schema",
			JSONUtil.put(
				"firstColumnLabel",
				baseSelectableTableDatasetView.getFirstColumnLabel(locale)
			).put(
				"firstColumnName",
				baseSelectableTableDatasetView.getFirstColumnName()
			)
		).build();
	}

}
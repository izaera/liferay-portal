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

package com.liferay.dataset.internal.ui.view.list;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributor;
import com.liferay.dataset.ui.view.DatasetViewContentRendererNames;
import com.liferay.dataset.ui.view.list.BaseListDatasetView;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marco Leo
 */
@Component(
	property = "dataset.view.content.renderer.name=" + DatasetViewContentRendererNames.LIST,
	service = DatasetViewContentRendererContextContributor.class
)
public class ListClayDataSetContentRendererContextContributor
	implements DatasetViewContentRendererContextContributor {

	@Override
	public Map<String, Object> getContentRendererContext(
		DatasetView datasetView, Locale locale) {

		if (datasetView instanceof BaseListDatasetView) {
			return _serialize((BaseListDatasetView)datasetView);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseListDatasetView baseListDatasetView) {

		return HashMapBuilder.<String, Object>put(
			"schema",
			HashMapBuilder.<String, Object>put(
				"description", baseListDatasetView.getDescription()
			).put(
				"image", baseListDatasetView.getImage()
			).put(
				"sticker", baseListDatasetView.getSticker()
			).put(
				"symbol", baseListDatasetView.getSymbol()
			).put(
				"title",
				() -> {
					String title = baseListDatasetView.getTitle();

					if (title.contains(StringPool.PERIOD)) {
						return StringUtil.split(title, StringPool.PERIOD);
					}

					return title;
				}
			).build()
		).build();
	}

}
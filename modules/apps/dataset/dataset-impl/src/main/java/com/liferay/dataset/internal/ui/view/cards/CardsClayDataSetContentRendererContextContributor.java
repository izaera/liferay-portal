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

package com.liferay.dataset.internal.ui.view.cards;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributor;
import com.liferay.dataset.ui.view.DatasetViewContentRendererNames;
import com.liferay.dataset.ui.view.cards.BaseCardsDatasetView;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Basto
 */
@Component(
	property = "dataset.view.content.renderer.name=" + DatasetViewContentRendererNames.CARDS,
	service = DatasetViewContentRendererContextContributor.class
)
public class CardsClayDataSetContentRendererContextContributor
	implements DatasetViewContentRendererContextContributor {

	@Override
	public Map<String, Object> getContentRendererContext(
		DatasetView datasetView, Locale locale) {

		if (datasetView instanceof BaseCardsDatasetView) {
			return _serialize((BaseCardsDatasetView)datasetView);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseCardsDatasetView baseCardsDatasetView) {

		return HashMapBuilder.<String, Object>put(
			"schema",
			HashMapBuilder.<String, Object>put(
				"description", baseCardsDatasetView.getDescription()
			).put(
				"href", baseCardsDatasetView.getLink()
			).put(
				"image", baseCardsDatasetView.getImage()
			).put(
				"sticker", baseCardsDatasetView.getSticker()
			).put(
				"symbol", baseCardsDatasetView.getSymbol()
			).put(
				"title", baseCardsDatasetView.getTitle()
			).build()
		).build();
	}

	@Reference
	private JSONFactory _jsonFactory;

}
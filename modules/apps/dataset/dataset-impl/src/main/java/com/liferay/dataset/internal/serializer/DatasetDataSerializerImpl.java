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

package com.liferay.dataset.internal.serializer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.liferay.dataset.serializer.DatasetDataSerializer;
import com.liferay.dataset.ui.action.DatasetActionProvider;
import com.liferay.dataset.ui.action.DatasetActionProviderRegistry;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(service = DatasetDataSerializer.class)
public class DatasetDataSerializerImpl implements DatasetDataSerializer {

	@Override
	public String create(
			List<DatasetActionProvider> datasetActionProviders, long groupId,
			HttpServletRequest httpServletRequest, List<Object> items)
		throws Exception {

		List<DatasetDataSerializerRow> datasetDataSerializerRows =
			_getDatasetDataSerializerRows(
				datasetActionProviders, groupId, httpServletRequest, items);

		return _objectMapper.writeValueAsString(datasetDataSerializerRows);
	}

	@Override
	public String create(
			List<DatasetActionProvider> datasetActionProviders, long groupId,
			HttpServletRequest httpServletRequest, List<Object> items,
			int itemsCount)
		throws Exception {

		DatasetDataSerializerResponse datasetDataSerializerResponse =
			new DatasetDataSerializerResponse(
				_getDatasetDataSerializerRows(
					datasetActionProviders, groupId, httpServletRequest, items),
				itemsCount);

		return _objectMapper.writeValueAsString(datasetDataSerializerResponse);
	}

	private List<DatasetDataSerializerRow> _getDatasetDataSerializerRows(
			List<DatasetActionProvider> datasetActionProviders, long groupId,
			HttpServletRequest httpServletRequest, List<Object> items)
		throws Exception {

		List<DatasetDataSerializerRow> datasetDataSerializerRows =
			new ArrayList<>();

		for (Object item : items) {
			DatasetDataSerializerRow datasetDataSerializerRow =
				new DatasetDataSerializerRow(item);

			if (datasetActionProviders != null) {
				for (DatasetActionProvider datasetActionProvider :
						datasetActionProviders) {

					List<DropdownItem> dropdownItems =
						datasetActionProvider.getDropdownItems(
							httpServletRequest, groupId, item);

					if (dropdownItems != null) {
						datasetDataSerializerRow.addActionDropdownItems(
							dropdownItems);
					}
				}
			}

			datasetDataSerializerRows.add(datasetDataSerializerRow);
		}

		return datasetDataSerializerRows;
	}

	private static final ObjectMapper _objectMapper = new ObjectMapper() {
		{
			configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
			disable(SerializationFeature.INDENT_OUTPUT);
		}
	};

	@Reference
	private DatasetActionProviderRegistry _datasetActionProviderRegistry;

}
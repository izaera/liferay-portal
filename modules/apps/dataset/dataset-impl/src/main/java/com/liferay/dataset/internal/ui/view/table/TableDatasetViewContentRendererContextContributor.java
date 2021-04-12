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
import com.liferay.dataset.ui.view.table.BaseTableDatasetView;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchema;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaField;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "dataset.view.content.renderer.name=" + DatasetViewContentRendererNames.TABLE,
	service = DatasetViewContentRendererContextContributor.class
)
public class TableDatasetViewContentRendererContextContributor
	implements DatasetViewContentRendererContextContributor {

	@Override
	public Map<String, Object> getContentRendererContext(
		DatasetView datasetView, Locale locale) {

		if (datasetView instanceof BaseTableDatasetView) {
			return _serialize((BaseTableDatasetView)datasetView, locale);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseTableDatasetView baseTableDatasetView, Locale locale) {

		JSONArray fieldsJSONArray = _jsonFactory.createJSONArray();

		TableDatasetViewSchema tableDatasetViewSchema =
			baseTableDatasetView.getTableDatasetViewSchema();

		Map<String, TableDatasetViewSchemaField> map =
			tableDatasetViewSchema.getTableDatasetViewSchemaFieldsMap();

		ResourceBundle resourceBundle = baseTableDatasetView.getResourceBundle(
			locale);

		for (TableDatasetViewSchemaField tableDatasetViewSchemaField :
				map.values()) {

			String label = LanguageUtil.get(
				resourceBundle, tableDatasetViewSchemaField.getLabel());

			if (Validator.isNull(label)) {
				label = StringPool.BLANK;
			}

			JSONObject jsonObject = JSONUtil.put(
				"actionId", tableDatasetViewSchemaField.getActionId()
			).put(
				"contentRenderer",
				tableDatasetViewSchemaField.getContentRenderer()
			).put(
				"contentRendererModuleURL",
				tableDatasetViewSchemaField.getContentRendererModuleURL()
			).put(
				"expand", tableDatasetViewSchemaField.isExpand()
			).put(
				"label", label
			).put(
				"sortable", tableDatasetViewSchemaField.isSortable()
			);

			String fieldName = tableDatasetViewSchemaField.getFieldName();

			if (fieldName.contains(StringPool.PERIOD)) {
				jsonObject.put(
					"fieldName",
					StringUtil.split(fieldName, StringPool.PERIOD));
			}
			else {
				jsonObject.put("fieldName", fieldName);
			}

			TableDatasetViewSchemaField.SortingOrder sortingOrder =
				tableDatasetViewSchemaField.getSortingOrder();

			if (sortingOrder != null) {
				jsonObject.put(
					"sortingOrder",
					StringUtil.toLowerCase(sortingOrder.toString()));
			}

			fieldsJSONArray.put(jsonObject);
		}

		return HashMapBuilder.<String, Object>put(
			"schema", JSONUtil.put("fields", fieldsJSONArray)
		).build();
	}

	@Reference
	private JSONFactory _jsonFactory;

}
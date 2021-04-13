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

package com.liferay.dataset.internal.ui.view.table.schema;

import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchema;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaBuilder;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaField;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marco Leo
 */
public class TableDatasetViewSchemaBuilderImpl
	implements TableDatasetViewSchemaBuilder {

	public TableDatasetViewSchemaBuilderImpl() {
		_tableDatasetViewSchema = new TableDatasetViewSchema();
		_tableDatasetViewSchemaFieldsMap = new LinkedHashMap<>();
	}

	@Override
	public TableDatasetViewSchemaField addTableDatasetViewSchemaField(
		String fieldName) {

		TableDatasetViewSchemaField tableDatasetViewSchemaField =
			new TableDatasetViewSchemaField();

		tableDatasetViewSchemaField.setFieldName(fieldName);

		_tableDatasetViewSchemaFieldsMap.put(
			fieldName, tableDatasetViewSchemaField);

		return tableDatasetViewSchemaField;
	}

	@Override
	public TableDatasetViewSchemaField addTableDatasetViewSchemaField(
		String fieldName, String label) {

		TableDatasetViewSchemaField tableDatasetViewSchemaField =
			addTableDatasetViewSchemaField(fieldName);

		tableDatasetViewSchemaField.setLabel(label);

		return tableDatasetViewSchemaField;
	}

	@Override
	public void addTableDatasetViewSchemaField(
		TableDatasetViewSchemaField tableDatasetViewSchemaField) {

		_tableDatasetViewSchemaFieldsMap.put(
			tableDatasetViewSchemaField.getFieldName(),
			tableDatasetViewSchemaField);
	}

	@Override
	public TableDatasetViewSchema build() {
		_tableDatasetViewSchema.setTableDatasetViewSchemaFieldsMap(
			_tableDatasetViewSchemaFieldsMap);

		return _tableDatasetViewSchema;
	}

	@Override
	public void removeTableDatasetViewSchemaField(String fieldName) {
		_tableDatasetViewSchemaFieldsMap.remove(fieldName);
	}

	public void setTableDatasetViewSchema(
		TableDatasetViewSchema tableDatasetViewSchema) {

		_tableDatasetViewSchema = tableDatasetViewSchema;
	}

	private TableDatasetViewSchema _tableDatasetViewSchema;
	private final Map<String, TableDatasetViewSchemaField>
		_tableDatasetViewSchemaFieldsMap;

}
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

package com.liferay.remote.app.admin.web.internal.frontend.taglib.clay.data.set.view.table;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.table.BaseTableDatasetView;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchema;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaBuilder;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaBuilderFactory;
import com.liferay.dataset.ui.view.table.schema.TableDatasetViewSchemaField;
import com.liferay.frontend.taglib.clay.data.set.ClayDataSetDisplayView;
import com.liferay.frontend.taglib.clay.data.set.view.table.ClayTableSchemaBuilder;
import com.liferay.frontend.taglib.clay.data.set.view.table.ClayTableSchemaField;
import com.liferay.remote.app.admin.web.internal.constants.RemoteAppAdminConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Basto
 */
@Component(
	immediate = true,
	property = "dataset.display.name=" + RemoteAppAdminConstants.REMOTE_APP_ENTRY_DATA_SET_DISPLAY,
	service = DatasetView.class
)
public class RemoteAppEntryTableClayDataSetDisplayView
	extends BaseTableDatasetView {

	@Override
	public TableDatasetViewSchema getTableDatasetViewSchema() {
		TableDatasetViewSchemaBuilder tableDatasetViewSchemaBuilder =
			_tableDatasetViewSchemaBuilderFactory.create();

		_addClayTableSchemaField(
			tableDatasetViewSchemaBuilder, "name", "name", "actionLink");
		_addClayTableSchemaField(tableDatasetViewSchemaBuilder, "url", "url");

		return tableDatasetViewSchemaBuilder.build();
	}

	private void _addClayTableSchemaField(
		TableDatasetViewSchemaBuilder tableDatasetViewSchemaBuilder,
		String fieldName, String label) {

		_addClayTableSchemaField(
			tableDatasetViewSchemaBuilder, fieldName, label, null);
	}

	private void _addClayTableSchemaField(
		TableDatasetViewSchemaBuilder tableDatasetViewSchemaBuilder,
		String fieldName, String label, String contentRenderer) {

		TableDatasetViewSchemaField tableDatasetViewSchemaField =
			tableDatasetViewSchemaBuilder.addTableDatasetViewSchemaField(
				fieldName, label);

		if (contentRenderer != null) {
			tableDatasetViewSchemaField.setContentRenderer(contentRenderer);
		}
	}

	@Reference
	private TableDatasetViewSchemaBuilderFactory
		_tableDatasetViewSchemaBuilderFactory;

}
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

package com.liferay.app.builder.web.internal.portlet.tab;

import com.liferay.app.builder.model.AppBuilderApp;
import com.liferay.app.builder.portlet.tab.AppBuilderAppPortletTab;
import com.liferay.app.builder.portlet.tab.AppBuilderAppPortletTabContext;
import com.liferay.dynamic.data.mapping.model.DDMStructureLayout;
import com.liferay.dynamic.data.mapping.service.DDMStructureLayoutLocalService;
import com.liferay.portal.kernel.util.HashMapBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true, property = "app.builder.app.tab.name=standard",
	service = AppBuilderAppPortletTab.class
)
public class StandardAppBuilderAppPortletTab
	implements AppBuilderAppPortletTab {

	@Override
	public AppBuilderAppPortletTabContext getAppBuilderAppPortletTabContext(
		AppBuilderApp appBuilderApp, long dataRecordId) {

		AppBuilderAppPortletTabContext appBuilderAppPortletTabContext =
			new AppBuilderAppPortletTabContext();

		DDMStructureLayout ddmStructureLayout =
			_ddmStructureLayoutLocalService.fetchDDMStructureLayout(
				appBuilderApp.getDdmStructureLayoutId());

		return appBuilderAppPortletTabContext.addDataLayoutProperties(
			appBuilderApp.getDdmStructureLayoutId(),
			HashMapBuilder.<String, Object>put(
				"nameMap", ddmStructureLayout.getNameMap()
			).put(
				"readOnly", false
			).build());
	}

	@Override
	public String getEditEntryPoint() {
		return "app-builder-web/js/pages/entry/EditEntry.es";
	}

	@Override
	public String getListEntryPoint() {
		return "app-builder-web/js/pages/entry/ListEntries.es";
	}

	@Override
	public String getViewEntryPoint() {
		return "app-builder-web/js/pages/entry/ViewEntry.es";
	}

	@Reference
	private DDMStructureLayoutLocalService _ddmStructureLayoutLocalService;

}
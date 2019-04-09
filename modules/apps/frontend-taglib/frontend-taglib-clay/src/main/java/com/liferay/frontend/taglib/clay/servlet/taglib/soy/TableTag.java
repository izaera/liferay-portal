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

package com.liferay.frontend.taglib.clay.servlet.taglib.soy;

import com.liferay.frontend.taglib.clay.internal.ClayTableTagSchemaContributorsProvider;
import com.liferay.frontend.taglib.clay.internal.InfoListProviderProvider;
import com.liferay.frontend.taglib.clay.internal.servlet.taglib.display.context.TableDefaults;
import com.liferay.frontend.taglib.clay.servlet.taglib.contributor.ClayTableTagSchemaContributor;
import com.liferay.frontend.taglib.clay.servlet.taglib.model.table.Schema;
import com.liferay.frontend.taglib.clay.servlet.taglib.model.table.Size;
import com.liferay.frontend.taglib.clay.servlet.taglib.soy.base.BaseClayTag;
import com.liferay.info.provider.DefaultInfoListProviderContext;
import com.liferay.info.provider.InfoListProvider;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class TableTag<T> extends BaseClayTag {

	@Override
	public int doStartTag() {
		setComponentBaseName("ClayTable");
		setHydrate(true);
		setModuleBaseName("table");

		int returnValue = super.doStartTag();

		InfoListProvider<T> infoListProvider = getInfoListProvider();

		if (infoListProvider != null) {
			_populateContext(infoListProvider);
		}

		List<ClayTableTagSchemaContributor> clayTableTagSchemaContributors =
			getTableTagSchemaContributors();

		if (clayTableTagSchemaContributors != null) {
			_populateSchema(clayTableTagSchemaContributors);
		}

		putValue("schema", _schema.toMap());

		Map<String, Object> context = getContext();

		boolean selectable = GetterUtil.getBoolean(context.get("selectable"));

		boolean showCheckbox = GetterUtil.getBoolean(
			context.get("showCheckbox"),
			TableDefaults.isShowCheckbox(selectable));

		setShowCheckbox(showCheckbox);

		return returnValue;
	}

	public void setActionsMenuVariant(String actionsMenuVariant) {
		putValue("actionsMenuVariant", actionsMenuVariant);
	}

	public void setInfoListProviderClassName(String infoListProviderClassName) {
		putValue("infoListProviderClassName", infoListProviderClassName);
	}

	public void setItems(Collection<?> items) {
		putValue("items", items);
	}

	public void setSchema(Schema schema) {
		_schema = schema;
	}

	public void setSelectable(Boolean selectable) {
		putValue("selectable", selectable);
	}

	public void setShowActionsMenu(Boolean showActionsMenu) {
		putValue("showActionsMenu", showActionsMenu);
	}

	public void setShowCheckbox(Boolean showCheckbox) {
		putValue("showCheckbox", showCheckbox);
	}

	public void setSize(Size size) {
		String sizeValue = null;

		if (size != null) {
			sizeValue = size.getValue();
		}

		putValue("size", sizeValue);
	}

	public void setTableClasses(String tableClasses) {
		putValue("tableClasses", tableClasses);
	}

	public void setTableSchemaContributorKey(String tableSchemaContributorKey) {
		putValue("tableSchemaContributorKey", tableSchemaContributorKey);
	}

	public void setUseDefaultClasses(Boolean useDefaultClasses) {
		putValue("useDefaultClasses", useDefaultClasses);
	}

	public void setWrapTable(Boolean wrapTable) {
		putValue("wrapTable", wrapTable);
	}

	protected DefaultInfoListProviderContext
		createDefaultInfoListProviderContext() {

		HttpServletRequest httpServletRequest = getRequest();

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		DefaultInfoListProviderContext defaultInfoListProviderContext =
			new DefaultInfoListProviderContext(
				themeDisplay.getScopeGroup(), themeDisplay.getUser());

		defaultInfoListProviderContext.setLayout(themeDisplay.getLayout());

		return defaultInfoListProviderContext;
	}

	protected InfoListProvider<T> getInfoListProvider() {
		Map<String, Object> context = getContext();

		String infoListProviderClassName = (String)context.get(
			"infoListProviderClassName");

		if (Validator.isNull(infoListProviderClassName)) {
			return null;
		}

		InfoListProvider infoListProvider =
			InfoListProviderProvider.getInfoListProvider(
				infoListProviderClassName);

		if (infoListProvider == null) {
			return null;
		}

		return infoListProvider;
	}

	protected List<ClayTableTagSchemaContributor>
		getTableTagSchemaContributors() {

		Map<String, Object> context = getContext();

		String tableSchemaContributorKey = GetterUtil.getString(
			context.get("tableSchemaContributorKey"));

		if (Validator.isNull(tableSchemaContributorKey)) {
			return null;
		}

		return ClayTableTagSchemaContributorsProvider.
			getClayTableTagSchemaContributors(tableSchemaContributorKey);
	}

	private void _populateContext(InfoListProvider<T> infoListProvider) {
		Map<String, Object> context = getContext();

		if (context.get("items") == null) {
			setItems(
				infoListProvider.getInfoList(
					createDefaultInfoListProviderContext()));
		}
	}

	private void _populateSchema(
		List<ClayTableTagSchemaContributor> clayTableTagSchemaContributors) {

		for (ClayTableTagSchemaContributor clayTableTagSchemaContributor :
				clayTableTagSchemaContributors) {

			clayTableTagSchemaContributor.populate(_schema);
		}
	}

	private Schema _schema = new Schema();

}
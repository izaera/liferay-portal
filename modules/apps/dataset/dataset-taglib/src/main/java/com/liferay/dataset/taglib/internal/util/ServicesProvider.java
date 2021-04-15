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

package com.liferay.dataset.taglib.internal.util;

import com.liferay.dataset.taglib.internal.json.DataSetViewsContextJSONFactory;
import com.liferay.dataset.ui.ActiveViewSettingsProvider;
import com.liferay.dataset.ui.filter.DatasetFilterSerializer;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = {})
public class ServicesProvider {

	public static ActiveViewSettingsProvider getActiveViewSettingsProvider() {
		return _activeViewSettingsProvider;
	}

	public static DatasetFilterSerializer getDatasetFilterSerializer() {
		return _datasetFilterSerializer;
	}

	public static DataSetViewsContextJSONFactory
		getDataSetViewsContextJSONFactory() {

		return _dataSetViewsContextJSONFactory;
	}

	public static NPMResolver getNPMResolver() {
		return _npmResolver;
	}

	@Reference(unbind = "-")
	protected void setActiveViewSettingsProvider(
		ActiveViewSettingsProvider activeViewSettingsProvider) {

		_activeViewSettingsProvider = activeViewSettingsProvider;
	}

	@Reference(unbind = "-")
	protected void setDatasetFilterSerializer(
		DatasetFilterSerializer datasetFilterSerializer) {

		_datasetFilterSerializer = datasetFilterSerializer;
	}

	@Reference(unbind = "-")
	protected void setDataSetViewsContextJSONFactory(
		DataSetViewsContextJSONFactory dataSetViewsContextJSONFactory) {

		_dataSetViewsContextJSONFactory = dataSetViewsContextJSONFactory;
	}

	@Reference(unbind = "-")
	protected void setNPMResolver(NPMResolver npmResolver) {
		_npmResolver = npmResolver;
	}

	private static ActiveViewSettingsProvider _activeViewSettingsProvider;
	private static DatasetFilterSerializer _datasetFilterSerializer;
	private static DataSetViewsContextJSONFactory
		_dataSetViewsContextJSONFactory;
	private static NPMResolver _npmResolver;

}
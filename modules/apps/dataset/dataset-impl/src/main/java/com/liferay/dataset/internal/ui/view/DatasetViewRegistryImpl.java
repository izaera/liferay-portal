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

package com.liferay.dataset.internal.ui.view;

import com.liferay.dataset.ui.view.DatasetView;
import com.liferay.dataset.ui.view.DatasetViewRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory.ServiceWrapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Marco Leo
 */
@Component(immediate = true, service = DatasetViewRegistry.class)
public class DatasetViewRegistryImpl implements DatasetViewRegistry {

	@Override
	public List<DatasetView> getDatasetViews(String datasetDisplayName) {
		List<ServiceWrapper<DatasetView>> datasetViewServiceWrappers =
			_serviceTrackerMap.getService(datasetDisplayName);

		if (datasetViewServiceWrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset view is associated with " + datasetDisplayName);
			}

			return Collections.emptyList();
		}

		List<DatasetView> datasetViews = new ArrayList<>();

		for (ServiceWrapper<DatasetView> datasetViewServiceWrapper :
				datasetViewServiceWrappers) {

			datasetViews.add(datasetViewServiceWrapper.getService());
		}

		return datasetViews;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, DatasetView.class, "dataset.display.name",
			ServiceTrackerCustomizerFactory.<DatasetView>serviceWrapper(
				bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetViewRegistryImpl.class);

	private ServiceTrackerMap<String, List<ServiceWrapper<DatasetView>>>
		_serviceTrackerMap;

}
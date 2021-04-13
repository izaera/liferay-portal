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

package com.liferay.dataset.internal.ui.filter;

import com.liferay.dataset.ui.filter.DatasetFilter;
import com.liferay.dataset.ui.filter.DatasetFilterRegistry;
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
@Component(immediate = true, service = DatasetFilterRegistry.class)
public class DatasetFilterRegistryImpl implements DatasetFilterRegistry {

	@Override
	public List<DatasetFilter> getDatasetFilters(String datasetDisplayName) {
		List<ServiceWrapper<DatasetFilter>> datasetFilterServiceWrappers =
			_serviceTrackerMap.getService(datasetDisplayName);

		if (datasetFilterServiceWrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset filter is associated with " +
						datasetDisplayName);
			}

			return Collections.emptyList();
		}

		List<DatasetFilter> datasetFilters = new ArrayList<>();

		for (ServiceWrapper<DatasetFilter> datasetFilterServiceWrapper :
				datasetFilterServiceWrappers) {

			datasetFilters.add(datasetFilterServiceWrapper.getService());
		}

		return datasetFilters;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, DatasetFilter.class, "dataset.display.name",
			ServiceTrackerCustomizerFactory.<DatasetFilter>serviceWrapper(
				bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetFilterRegistryImpl.class);

	private ServiceTrackerMap<String, List<ServiceWrapper<DatasetFilter>>>
		_serviceTrackerMap;

}
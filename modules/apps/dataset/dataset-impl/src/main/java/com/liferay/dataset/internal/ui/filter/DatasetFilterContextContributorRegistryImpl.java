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

import com.liferay.dataset.ui.filter.DatasetFilterContextContributor;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributorRegistry;
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
@Component(
	immediate = true, service = DatasetFilterContextContributorRegistry.class
)
public class DatasetFilterContextContributorRegistryImpl
	implements DatasetFilterContextContributorRegistry {

	@Override
	public List<DatasetFilterContextContributor>
		getDatasetFilterContextContributors(String datasetFilterType) {

		List<ServiceWrapper<DatasetFilterContextContributor>>
			datasetFilterContextContributorServiceWrappers =
				_serviceTrackerMap.getService(datasetFilterType);

		if (datasetFilterContextContributorServiceWrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset filter context contributor is associated " +
						"with " + datasetFilterType);
			}

			return Collections.emptyList();
		}

		List<DatasetFilterContextContributor> datasetFilterContextContributors =
			new ArrayList<>();

		for (ServiceWrapper<DatasetFilterContextContributor>
				datasetFilterContextContributorServiceWrapper :
					datasetFilterContextContributorServiceWrappers) {

			datasetFilterContextContributors.add(
				datasetFilterContextContributorServiceWrapper.getService());
		}

		return datasetFilterContextContributors;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, DatasetFilterContextContributor.class,
			"dataset.filter.type",
			ServiceTrackerCustomizerFactory.
				<DatasetFilterContextContributor>serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetFilterContextContributorRegistryImpl.class);

	private ServiceTrackerMap
		<String, List<ServiceWrapper<DatasetFilterContextContributor>>>
			_serviceTrackerMap;

}
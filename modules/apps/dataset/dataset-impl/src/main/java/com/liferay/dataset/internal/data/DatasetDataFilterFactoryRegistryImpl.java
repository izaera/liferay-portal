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

package com.liferay.dataset.internal.data;

import com.liferay.dataset.data.DatasetDataFilterFactory;
import com.liferay.dataset.data.DatasetDataFilterFactoryRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory.ServiceWrapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ListUtil;

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
@Component(immediate = true, service = DatasetDataFilterFactoryRegistry.class)
public class DatasetDataFilterFactoryRegistryImpl
	implements DatasetDataFilterFactoryRegistry {

	@Override
	public List<DatasetDataFilterFactory> getDatasetDataFilterFactories() {
		List<DatasetDataFilterFactory> datasetDataFilterFactories =
			new ArrayList<>();

		List<ServiceWrapper<DatasetDataFilterFactory>>
			datasetDataFilterFactoryServiceWrappers = ListUtil.fromCollection(
				_serviceTrackerMap.values());

		for (ServiceWrapper<DatasetDataFilterFactory>
				datasetDataFilterFactoryServiceWrapper :
					datasetDataFilterFactoryServiceWrappers) {

			datasetDataFilterFactories.add(
				datasetDataFilterFactoryServiceWrapper.getService());
		}

		return Collections.unmodifiableList(datasetDataFilterFactories);
	}

	@Override
	public DatasetDataFilterFactory getDatasetDataFilterFactory(
		String datasetDataProviderKey) {

		ServiceWrapper<DatasetDataFilterFactory>
			datasetDataFilterFactoryServiceWrapper =
				_serviceTrackerMap.getService(datasetDataProviderKey);

		if (datasetDataFilterFactoryServiceWrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset data filter factory registered for " +
						datasetDataProviderKey);
			}

			return new DefaultDatasetDataFilterFactoryImpl();
		}

		return datasetDataFilterFactoryServiceWrapper.getService();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, DatasetDataFilterFactory.class,
			"dataset.data.provider.key",
			ServiceTrackerCustomizerFactory.
				<DatasetDataFilterFactory>serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetDataFilterFactoryRegistryImpl.class);

	private ServiceTrackerMap<String, ServiceWrapper<DatasetDataFilterFactory>>
		_serviceTrackerMap;

}
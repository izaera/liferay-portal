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

import com.liferay.dataset.data.DatasetDataProvider;
import com.liferay.dataset.data.DatasetDataProviderRegistry;
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
@Component(immediate = true, service = DatasetDataProviderRegistry.class)
public class DatasetDataProviderRegistryImpl
	implements DatasetDataProviderRegistry {

	@Override
	public DatasetDataProvider getDatasetDataProvider(
		String datasetDataProviderKey) {

		ServiceWrapper<DatasetDataProvider> datasetDataProviderServiceWrapper =
			_serviceTrackerMap.getService(datasetDataProviderKey);

		if (datasetDataProviderServiceWrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset data provider is associated with " +
						datasetDataProviderKey);
			}

			return null;
		}

		return datasetDataProviderServiceWrapper.getService();
	}

	@Override
	public List<DatasetDataProvider> getDatasetDataProviders() {
		List<DatasetDataProvider> datasetDataProviders = new ArrayList<>();

		List<ServiceWrapper<DatasetDataProvider>>
			datasetDataProviderServiceWrappers = ListUtil.fromCollection(
				_serviceTrackerMap.values());

		for (ServiceWrapper<DatasetDataProvider>
				datasetDataProviderServiceWrapper :
					datasetDataProviderServiceWrappers) {

			datasetDataProviders.add(
				datasetDataProviderServiceWrapper.getService());
		}

		return Collections.unmodifiableList(datasetDataProviders);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, DatasetDataProvider.class,
			"dataset.data.provider.key",
			ServiceTrackerCustomizerFactory.<DatasetDataProvider>serviceWrapper(
				bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetDataProviderRegistryImpl.class);

	private ServiceTrackerMap<String, ServiceWrapper<DatasetDataProvider>>
		_serviceTrackerMap;

}
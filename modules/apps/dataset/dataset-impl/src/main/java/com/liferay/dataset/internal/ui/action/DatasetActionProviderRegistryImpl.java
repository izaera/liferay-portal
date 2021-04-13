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

package com.liferay.dataset.internal.ui.action;

import com.liferay.dataset.ui.action.DatasetActionProvider;
import com.liferay.dataset.ui.action.DatasetActionProviderRegistry;
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
@Component(immediate = true, service = DatasetActionProviderRegistry.class)
public class DatasetActionProviderRegistryImpl
	implements DatasetActionProviderRegistry {

	@Override
	public List<DatasetActionProvider> getDatasetActionProviders(
		String datasetDataProviderKey) {

		List<ServiceWrapper<DatasetActionProvider>>
			datasetActionProviderServiceWrappers =
				_serviceTrackerMap.getService(datasetDataProviderKey);

		if (datasetActionProviderServiceWrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset action provider is associated with " +
						datasetDataProviderKey);
			}

			return Collections.emptyList();
		}

		List<DatasetActionProvider> datasetActionProviders = new ArrayList<>();

		for (ServiceWrapper<DatasetActionProvider>
				datasetActionProviderServiceWrapper :
					datasetActionProviderServiceWrappers) {

			datasetActionProviders.add(
				datasetActionProviderServiceWrapper.getService());
		}

		return datasetActionProviders;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, DatasetActionProvider.class,
			"dataset.data.provider.key",
			ServiceTrackerCustomizerFactory.
				<DatasetActionProvider>serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetActionProviderRegistryImpl.class);

	private ServiceTrackerMap
		<String, List<ServiceWrapper<DatasetActionProvider>>>
			_serviceTrackerMap;

}
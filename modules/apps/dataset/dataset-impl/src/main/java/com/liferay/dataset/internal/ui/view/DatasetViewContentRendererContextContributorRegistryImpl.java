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

import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributor;
import com.liferay.dataset.ui.view.DatasetViewContentRendererContextContributorRegistry;
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
	immediate = true,
	service = DatasetViewContentRendererContextContributorRegistry.class
)
public class DatasetViewContentRendererContextContributorRegistryImpl
	implements DatasetViewContentRendererContextContributorRegistry {

	@Override
	public List<DatasetViewContentRendererContextContributor>
		getDatasetViewContentRendererContextContributors(
			String datasetViewContentRendererName) {

		List<ServiceWrapper<DatasetViewContentRendererContextContributor>>
			datasetViewContentRendererContextContributorServiceWrappers =
				_serviceTrackerMap.getService(datasetViewContentRendererName);

		if (datasetViewContentRendererContextContributorServiceWrappers ==
				null) {

			if (_log.isDebugEnabled()) {
				_log.debug(
					"No dataset view content renderer context contributor is " +
						"associated with " + datasetViewContentRendererName);
			}

			return Collections.emptyList();
		}

		List<DatasetViewContentRendererContextContributor>
			datasetViewContentRendererContextContributors = new ArrayList<>();

		for (ServiceWrapper<DatasetViewContentRendererContextContributor>
				datasetViewContentRendererContextContributorServiceWrapper :
					datasetViewContentRendererContextContributorServiceWrappers) {

			datasetViewContentRendererContextContributors.add(
				datasetViewContentRendererContextContributorServiceWrapper.
					getService());
		}

		return datasetViewContentRendererContextContributors;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, DatasetViewContentRendererContextContributor.class,
			"dataset.view.content.renderer.name",
			ServiceTrackerCustomizerFactory.
				<DatasetViewContentRendererContextContributor>serviceWrapper(
					bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetViewContentRendererContextContributorRegistryImpl.class);

	private ServiceTrackerMap
		<String,
		 List<ServiceWrapper<DatasetViewContentRendererContextContributor>>>
			_serviceTrackerMap;

}
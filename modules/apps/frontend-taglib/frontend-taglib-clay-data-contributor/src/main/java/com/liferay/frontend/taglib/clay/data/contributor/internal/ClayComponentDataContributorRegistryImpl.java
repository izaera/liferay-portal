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

package com.liferay.frontend.taglib.clay.data.contributor.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributor;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributorRegistry;
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
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true, service = ClayComponentDataContributorRegistry.class
)
public class ClayComponentDataContributorRegistryImpl
	implements ClayComponentDataContributorRegistry {

	@Override
	public ClayComponentDataContributor get(String key) {
		ServiceWrapper<ClayComponentDataContributor> wrapper =
			_serviceTrackerMap.getService(key);

		if (wrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No ClayComponentDataContributor registered with key " +
						key);
			}

			return null;
		}

		return wrapper.getService();
	}

	@Override
	public List<ClayComponentDataContributor> getDataContributors() {
		ArrayList<ClayComponentDataContributor> services = new ArrayList<>();

		List<ServiceWrapper<ClayComponentDataContributor>> values =
			ListUtil.fromCollection(_serviceTrackerMap.values());

		for (ServiceWrapper<ClayComponentDataContributor> wrapper : values) {
			services.add(wrapper.getService());
		}

		return Collections.unmodifiableList(services);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ClayComponentDataContributor.class,
			"tag.contributor.key",
			ServiceTrackerCustomizerFactory.serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentDataContributorRegistryImpl.class);

	private ServiceTrackerMap<String, ServiceWrapper<ClayComponentDataContributor>>
		_serviceTrackerMap;

}
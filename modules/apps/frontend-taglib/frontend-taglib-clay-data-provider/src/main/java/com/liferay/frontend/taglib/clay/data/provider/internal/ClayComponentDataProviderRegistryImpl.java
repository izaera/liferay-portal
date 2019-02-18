/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package com.liferay.frontend.taglib.clay.data.provider.internal;

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProvider;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProviderRegistry;
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
@Component(immediate = true, service = ClayComponentDataProviderRegistry.class)
public class ClayComponentDataProviderRegistryImpl
	implements ClayComponentDataProviderRegistry {

	@Override
	public ClayComponentDataProvider get(String key) {
		ServiceWrapper<ClayComponentDataProvider> wrapper =
			_serviceTrackerMap.getService(key);

		if (wrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No ClayComponentDataProvider registered with key " + key);
			}

			return null;
		}

		return wrapper.getService();
	}

	@Override
	public List<ClayComponentDataProvider> getDataProviders() {
		ArrayList<ClayComponentDataProvider> services = new ArrayList<>();

		List<ServiceWrapper<ClayComponentDataProvider>> values = ListUtil
			.fromCollection(_serviceTrackerMap.values());

		for (ServiceWrapper<ClayComponentDataProvider> wrapper : values) {
			services.add(wrapper.getService());
		}

		return Collections.unmodifiableList(services);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ClayComponentDataProvider.class,
			"clay.component.data.provider.key",
			ServiceTrackerCustomizerFactory.serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentDataProviderRegistryImpl.class);

	private ServiceTrackerMap<String, ServiceWrapper<ClayComponentDataProvider>>
		_serviceTrackerMap;

}
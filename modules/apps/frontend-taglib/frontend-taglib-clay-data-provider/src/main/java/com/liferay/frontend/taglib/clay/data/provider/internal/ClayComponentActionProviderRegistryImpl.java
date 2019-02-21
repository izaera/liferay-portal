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

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentActionProvider;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentActionProviderRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory.ServiceWrapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Marco Leo
 */
@Component(
	immediate = true, service = ClayComponentActionProviderRegistry.class
)
public class ClayComponentActionProviderRegistryImpl
	implements ClayComponentActionProviderRegistry {

	@Override
	public List<ClayComponentActionProvider> getActionProviders(String key) {
		List<ClayComponentActionProvider> providers = new ArrayList<>();

		List<ServiceWrapper<ClayComponentActionProvider>> wrappers =
			_serviceTrackerMap.getService(key);

		if (wrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No ClayComponentActionProvider registered with key " + key);
			}

			return null;
		}

		for (ServiceWrapper<ClayComponentActionProvider> wrapper : wrappers) {
			providers.add(wrapper.getService());
		}

		return providers;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, ClayComponentActionProvider.class,
			"clay.component.data.provider.key",
			ServiceTrackerCustomizerFactory.serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentActionProviderRegistryImpl.class);

	private ServiceTrackerMap
		<String, List<ServiceWrapper<ClayComponentActionProvider>>>
		_serviceTrackerMap;

}
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

package com.liferay.frontend.taglib.clay.data.contributor.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentActionContributor;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentActionContributorRegistry;
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
	immediate = true, service = ClayComponentActionContributorRegistry.class
)
public class ClayComponentActionContributorRegistryImpl
	implements ClayComponentActionContributorRegistry {

	@Override
	public List<ClayComponentActionContributor> getActionContributors(
		String key) {

		List<ClayComponentActionContributor> contributors = new ArrayList<>();

		List<ServiceWrapper<ClayComponentActionContributor>> wrappers =
			_serviceTrackerMap.getService(key);

		if (wrappers == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No ClayComponentActionContributor registered with key " +
						key);
			}

			return null;
		}

		for (ServiceWrapper<ClayComponentActionContributor> wrapper :
				wrappers) {

			contributors.add(wrapper.getService());
		}

		return contributors;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, ClayComponentActionContributor.class,
			"contributor.name",
			ServiceTrackerCustomizerFactory.serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentActionContributorRegistryImpl.class);

	private ServiceTrackerMap
		<String, List<ServiceWrapper<ClayComponentActionContributor>>>
		_serviceTrackerMap;

}
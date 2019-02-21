package com.liferay.frontend.taglib.clay.data.contributor.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayTagMetaAttributeContributor;
import com.liferay.frontend.taglib.clay.data.contributor.ClayTagMetaAttributeContributorRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory.ServiceWrapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true, service = ClayTagMetaAttributeContributorRegistry.class
)
public class ClayTagMetaAttributeContributorRegistryImpl
	implements ClayTagMetaAttributeContributorRegistry {

	@Override
	public ClayTagMetaAttributeContributor get(String contributorName) {
		ServiceWrapper<ClayTagMetaAttributeContributor> service = _serviceTrackerMap
			.getService(contributorName);

		if (service != null) {
			return service.getService();
		}

		return null;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, ClayTagMetaAttributeContributor.class,
			"contributor.name",
			ServiceTrackerCustomizerFactory.serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private ServiceTrackerMap<String, ServiceWrapper<ClayTagMetaAttributeContributor>>
		_serviceTrackerMap;

}
package com.liferay.frontend.js.loader.modules.extender.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.ServletContext;

import com.liferay.frontend.js.loader.modules.extender.registry.PackageInterpreter;
import com.liferay.frontend.js.loader.modules.extender.registry.BundleConfig;
import org.apache.felix.utils.log.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.liferay.osgi.util.ServiceTrackerFactory;

@Component(immediate = true, service = PackagesConfigTracker.class)
public class PackagesConfigTracker
	implements
		ServiceTrackerCustomizer<
			ServletContext, ServiceReference<ServletContext>> {

	@Activate
	protected void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		if (_serviceTracker != null) {
			_serviceTracker.close();
		}

		_bundleContext = componentContext.getBundleContext();

		_logger = new Logger(_bundleContext);

		_serviceTracker = ServiceTrackerFactory.open(
			_bundleContext,
			"(&(objectClass=" + ServletContext.class.getName() +
				")(osgi.web.contextpath=*))",
			this);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();

		_serviceTracker = null;
	}

	@Override
	public ServiceReference<ServletContext> addingService(
		ServiceReference<ServletContext> serviceReference) {

		for (PackageInterpreter interpreter : _interpreters) {
			BundleConfig pkgsBundleConfig =
				interpreter.interpret(serviceReference);

			if (pkgsBundleConfig != null) {
				_pkgsBundleConfigs.put(serviceReference, pkgsBundleConfig);

				return serviceReference;
			}
		}

		return null;
	}

	public Collection<BundleConfig> getPackagesBundleConfigs() {
		return _pkgsBundleConfigs.values();
	}

	@Override
	public void modifiedService(
		ServiceReference<ServletContext> serviceReference,
		ServiceReference<ServletContext> trackedServiceReference) {

		removedService(serviceReference, trackedServiceReference);

		addingService(serviceReference);
	}

	@Override
	public void removedService(
		ServiceReference<ServletContext> serviceReference,
		ServiceReference<ServletContext> trackedServiceReference) {

		BundleConfig pkgBundleConfig = _pkgsBundleConfigs.remove(
			serviceReference);

		if (pkgBundleConfig != null) {
			_bundleContext.ungetService(serviceReference);
		}
	}

	public void bindInterpreter(PackageInterpreter interpreter) {
		_interpreters.add(interpreter);
	}

	public void unbindInterpreter(PackageInterpreter interpreter) {
		_interpreters.remove(interpreter);
	}

	@Reference(
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		bind = "bindInterpreter", unbind = "unbindInterpreter"
	)
	private List<PackageInterpreter> _interpreters =
		new ArrayList<PackageInterpreter>();

	private BundleContext _bundleContext;
	private final Map<ServiceReference<ServletContext>, BundleConfig>
		_pkgsBundleConfigs = new ConcurrentSkipListMap<>();
	private ServiceTracker<ServletContext, ServiceReference<ServletContext>>
		_serviceTracker;
	private Logger _logger;

}

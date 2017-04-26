package com.liferay.frontend.js.loader.modules.extender.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.ServletContext;

import com.github.yuchi.semver.Range;
import com.github.yuchi.semver.Version;
import com.liferay.frontend.js.loader.modules.extender.registry.JSModule;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackageDependency;
import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSBundleProcessor;
import com.liferay.frontend.js.loader.modules.extender.registry.JSBundle;
import com.liferay.portal.kernel.util.StringPool;
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

@Component(immediate = true, service = PackageRegistry.class)
public class PackageRegistry {

	public JSPackage resolveJSPackageDependency(
		JSPackageDependency jsPackageDependency) {

		String packageName = jsPackageDependency.getPackageName();

		List<JSPackage> jsPackages = new ArrayList<>();

		for (JSPackage jsPackage : _jsPackages.values() ) {
			if (jsPackage.getName().equals(packageName)) {
				jsPackages.add(jsPackage);
			}
		}

		Collections.sort(jsPackages, new Comparator<JSPackage>() {
			@Override
			public int compare(JSPackage o1, JSPackage o2) {
				Version version1 = Version.from(o1.getVersion(), true);
				Version version2 = Version.from(o2.getVersion(), true);

				return version1.compareTo(version2);
			}
		});

		Range range = Range.from(
			jsPackageDependency.getVersionConstraints(), true);

		for (JSPackage jsPackage : jsPackages) {
			Version version = Version.from(jsPackage.getVersion(), true);

			if (range.test(version)) {
				return jsPackage;
			}
		}

		return null;
	}

	public Collection<JSPackage> getJSPackages() {
		return _jsPackages.values();
	}

	public Collection<JSBundle> getJSBundles() {
		return _bundles.values();
	}

	public Collection<JSModule> getResolvedJSModules() {
		return _resolvedJSModules.values();
	}

	public JSModule getResolvedJSModule(String identifier) {
		return _resolvedJSModules.get(identifier);
	}

	public JSModule getJSModule(String identifier) {
		return _jsModules.get(identifier);
	}

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
			new PackageRegistryServiceTrackerCustomizer());
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();

		_serviceTracker = null;
	}

	protected void bindBundleProcessor(JSBundleProcessor jsBundleProcessor) {
		_jsBundleProcessors.add(jsBundleProcessor);
	}

	protected void unbindBundleProcessor(JSBundleProcessor jsBundleProcessor) {
		_jsBundleProcessors.remove(jsBundleProcessor);
	}

	private void addBundle(
		ServiceReference<ServletContext> serviceReference, JSBundle jsBundle) {

		_bundles.put(serviceReference, jsBundle);

		refreshJSModuleCaches();
	}

	private JSBundle removeBundle(
		ServiceReference<ServletContext> serviceReference) {

		JSBundle jsBundle = _bundles.remove(serviceReference);

		if (jsBundle != null) {
			refreshJSModuleCaches();
		}

		return jsBundle;
	}

	private void refreshJSModuleCaches() {
		Map<String, JSPackage> jsPackages = new HashMap<>();
		Map<String, JSModule> jsModules = new HashMap<>();
		Map<String, JSModule> resolvedJSModules = new HashMap<>();

		for (JSBundle jsBundle : _bundles.values()) {
			for (JSPackage jsPackage : jsBundle.getJSPackages()) {
				jsPackages.put(jsPackage.getId(), jsPackage);

				for (JSModule jsModule : jsPackage.getJSModules()) {
					resolvedJSModules.put(jsModule.getResolvedId(), jsModule);
					jsModules.put(jsModule.getId(), jsModule);
				}
			}
		}

		_resolvedJSModules = resolvedJSModules;
		_jsModules = jsModules;
		_jsPackages = jsPackages;
	}

	@Reference(
		cardinality = ReferenceCardinality.AT_LEAST_ONE,
		bind = "bindBundleProcessor", unbind = "unbindBundleProcessor"
	)
	private List<JSBundleProcessor> _jsBundleProcessors = new ArrayList<>();

	private BundleContext _bundleContext;
	private final Map<ServiceReference<ServletContext>, JSBundle> _bundles =
		new ConcurrentSkipListMap<>();
	private ServiceTracker<ServletContext, ServiceReference<ServletContext>>
		_serviceTracker;
	private Map<String,JSPackage> _jsPackages = new HashMap<>();
	private Map<String,JSModule> _resolvedJSModules = new HashMap<>();
	private Map<String,JSModule> _jsModules = new HashMap<>();
	private Logger _logger;

	private class PackageRegistryServiceTrackerCustomizer implements
		ServiceTrackerCustomizer<
			ServletContext, ServiceReference<ServletContext>> {

		@Override
		public ServiceReference<ServletContext> addingService(
			ServiceReference<ServletContext> serviceReference) {

			for (JSBundleProcessor jsBundleProcessor : _jsBundleProcessors) {
				JSBundle jsBundle =  jsBundleProcessor.process(serviceReference);

				if (jsBundle != null) {
					addBundle(serviceReference, jsBundle);

					return serviceReference;
				}
			}

			return null;
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

			JSBundle jsBundle = removeBundle(serviceReference);

			if (jsBundle != null) {
				_bundleContext.ungetService(serviceReference);
			}
		}
	}
}

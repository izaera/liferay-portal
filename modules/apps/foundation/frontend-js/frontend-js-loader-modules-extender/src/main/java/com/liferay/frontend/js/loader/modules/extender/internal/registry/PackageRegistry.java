package com.liferay.frontend.js.loader.modules.extender.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.frontend.js.loader.modules.extender.registry.Builtin;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageConfig;
import com.liferay.frontend.js.loader.modules.extender.registry.BundleConfig;
import com.liferay.frontend.js.loader.modules.extender.internal.PackagesConfigTracker;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageDependency;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.util.StringPool;

@Component(immediate = true, service = PackageRegistry.class)
public class PackageRegistry {

	@Activate
	@Modified
	protected void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		_builtins = new ArrayList<Builtin>();

		// TODO create virtual/custom-path bundles for builtin modules

		_builtins.add(new Builtin("events", new NPMPackageDependency(
			"events", StringPool.STAR), null));
		_builtins.add(new Builtin("domain", new NPMPackageDependency(
			"domain-browser", StringPool.STAR), null));
		_builtins.add(new Builtin("assert", new NPMPackageDependency(
			"assert", StringPool.STAR), null));
	}

	public List<Builtin> getBuiltins() {
		return _builtins;
	}

	public PackageConfig[] getPackageConfigs() {
		// TODO: cache flat package configs
		Map<String, PackageConfig> packageConfigsMap = new HashMap<>();

		for (BundleConfig bundleConfig : getBundleConfigs()) {
			for (PackageConfig packageConfig : bundleConfig.getPackageConfigs()) {
				packageConfigsMap.put(packageConfig.getIdentifier(), packageConfig);
			}
		}

		Collection<PackageConfig> packageConfigs = packageConfigsMap.values();

		return packageConfigs.toArray(new PackageConfig[packageConfigs.size()]);
	}

	public Collection<BundleConfig> getBundleConfigs() {
		if (_packagesConfigTracker != null) {
			return _packagesConfigTracker.getPackagesBundleConfigs();
		}
		else {
			return Collections.emptyList();
		}
	}

	public PackageConfig resolve(PackageDependency packageDependency) {
		// TODO: don't do linear search but cache packages map
		PackageConfig[] packageConfigs = getPackageConfigs();

		List<PackageConfig> matchingPackageConfigs = new ArrayList<>();

		for (PackageConfig packageConfig : packageConfigs) {
			if (packageDependency.matches(packageConfig)) {
				matchingPackageConfigs.add(packageConfig);
			}
		}

		if (matchingPackageConfigs.isEmpty()) {
			return null;
		}

		Collections.sort(
			matchingPackageConfigs,
			new Comparator<PackageConfig>() {
				@Override
				public int compare(
					PackageConfig packageConfig1, PackageConfig packageConfig2) {

					return packageConfig1.getVersion().compareTo(
						packageConfig2.getVersion());
				}
			});

		return matchingPackageConfigs.get(0);
	}

	public PackageConfig find(String identifier) {
		PackageConfig[] packageConfigs = getPackageConfigs();

		for (PackageConfig packageConfig : packageConfigs) {
			if (packageConfig.getIdentifier().equals(identifier)) {
				return packageConfig;
			}
		}

		return null;
	}

	@Reference(unbind = "-")
	protected void setJSBundleConfigTracker(
		PackagesConfigTracker packagesConfigTracker) {

		_packagesConfigTracker = packagesConfigTracker;
	}

	private List<Builtin> _builtins;
	private PackagesConfigTracker _packagesConfigTracker;
}

package com.liferay.frontend.js.loader.modules.extender.internal.registry;

import com.liferay.frontend.js.loader.modules.extender.registry.ModuleAlias;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageConfig;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageDependency;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageIdentifier;
import com.liferay.frontend.js.loader.modules.extender.registry.PackageInterpreter;
import com.liferay.frontend.js.loader.modules.extender.registry.BundleConfig;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import org.apache.felix.utils.log.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(immediate = true, service = PackageInterpreter.class)
public class FlatNPMPackageInterpreter implements PackageInterpreter {

	public static final String BROWSER = "browser";
	public static final String DEPENDENCIES = "dependencies";
	public static final String DEPENDENCIES_OPTIONAL = "optionalDependencies";
	public static final String DEPENDENCIES_PEER = "peerDependencies";
	public static final String NAME = "name";
	public static final String MAIN = "main";
	public static final String PACKAGES = "packages";
	public static final String TYPE = "npm-flat";
	public static final String VERSION = "version";

	@Activate
	@Modified
	protected void activate(
			ComponentContext componentContext, Map<String, Object> properties)
		throws Exception {

		_bundleContext = componentContext.getBundleContext();

		_logger = new Logger(componentContext.getBundleContext());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public BundleConfig interpret(
		ServiceReference<ServletContext> serviceReference) {

		Bundle bundle = serviceReference.getBundle();

		ServletContext servletContext = _bundleContext.getService(
			serviceReference);

		try {
			BundleConfig bundleConfig =
				new BundleConfig(servletContext);

			PackageConfig packageConfig = parsePackage(
				bundle, "/META-INF/resources", bundleConfig);

			bundleConfig.addPackageConfig(packageConfig);
			System.err.println("==========> "+packageConfig);

			Enumeration<URL> urls = bundle.findEntries(
				"/META-INF/resources/node_modules", "*", false);

			while(urls.hasMoreElements()) {
				URL url = urls.nextElement();

				packageConfig = parsePackage(
					bundle, url.getPath(), bundleConfig);

				bundleConfig.addPackageConfig(packageConfig);
				System.err.println("==========> "+packageConfig);
			}

			return bundleConfig;
		}
		catch (InvalidPackageException ipe) {
			return null;
		}
	}

	protected PackageConfig parsePackage(
			Bundle bundle, String location,
			BundleConfig bundleConfig)
		throws InvalidPackageException {

		if (!location.endsWith(StringPool.SLASH)) {
			location += StringPool.SLASH;
		}

		JSONObject jsonObject = _readPackageJson(
			bundle, location + "package.json");

		if (jsonObject == null) {
			throw new InvalidPackageException(location);
		}

		String name = jsonObject.getString(NAME);
		String version = jsonObject.getString(VERSION);
		String main = jsonObject.getString(MAIN);

		PackageIdentifier pkgIdentifier = new NPMPackageIdentifier(name);

		List<PackageDependency> dependencies =
			new ArrayList<PackageDependency>();

		dependencies.addAll(parsePackageDependencies(jsonObject, DEPENDENCIES));
		dependencies.addAll(
			parsePackageDependencies(jsonObject, DEPENDENCIES_OPTIONAL));
		dependencies.addAll(
			parsePackageDependencies(jsonObject, DEPENDENCIES_PEER));

		List<ModuleAlias> moduleAliases = new ArrayList<ModuleAlias>();

		JSONObject browserAliases = jsonObject.getJSONObject(BROWSER);

		if (browserAliases != null) {
			Iterator<String> itr = browserAliases.keys();

			while (itr.hasNext()) {
				String aliasedName = itr.next();
				Object source = browserAliases.get(aliasedName);

				_logger.log(Logger.LOG_DEBUG, "-- " + aliasedName + " >> " + source);

				if (source == null) {
					continue;
				}
				else if (source instanceof Boolean) {
					boolean value = GetterUtil.getBoolean(source);

					if (!value) {
						_logger.log(
							Logger.LOG_WARNING,
							"Skipped modules are not supported (skipped " +
								aliasedName + " in " + name + "@" + version +
									")");
					}
					else {
						continue;
					}
				}
				else if (source instanceof String) {
					String sourceName = (String)source;

					ModuleAlias moduleAlias = new ModuleAlias(
						sourceName, aliasedName);

					moduleAliases.add(moduleAlias);
				}
			}
		}

		String servletPackagePath = location;

		if (servletPackagePath.startsWith("/META-INF/resources")) {
			servletPackagePath = servletPackagePath.substring(19);
		}

		return new PackageConfig(
			name, pkgIdentifier, version, main, dependencies, moduleAliases,
			servletPackagePath, bundleConfig);
	}

	protected List<PackageDependency> parsePackageDependencies(
		JSONObject jsonObject, String property) {

		List<PackageDependency> dependencies =
			new ArrayList<PackageDependency>();

		JSONObject deps = jsonObject.getJSONObject(property);

		if (deps == null) {
			return dependencies;
		}

		Iterator<String> itr = deps.keys();

		while (itr.hasNext()) {
			String name = itr.next();
			String value = deps.getString(name);

			dependencies.add(new NPMPackageDependency(name, value));
		}

		return dependencies;
	}

	private static JSONObject _readPackageJson(Bundle bundle, String location) {
		try {
			URL url = bundle.getResource(location);

			if (url == null) {
				return null;
			}

			String json = StringUtil.read(url.openStream());

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(json);

			return jsonObject;
		}
		catch (MalformedURLException murle) {
			return null;
		}
		catch (IOException ioe) {
			return null;
		}
		catch (JSONException jsone) {
			return null;
		}
	}

	private static class InvalidPackageException extends Exception {
		private static final long serialVersionUID = 123233863902880765L;

		public InvalidPackageException(String location) {
			super(location);
		}
	}

	private BundleContext _bundleContext;
	private Logger _logger;
	private String _indent = "";
}

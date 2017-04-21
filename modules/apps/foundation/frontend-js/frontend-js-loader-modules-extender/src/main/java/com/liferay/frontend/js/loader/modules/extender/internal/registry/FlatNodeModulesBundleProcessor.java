package com.liferay.frontend.js.loader.modules.extender.internal.registry;

import com.liferay.frontend.js.loader.modules.extender.registry.JSBundle;
import com.liferay.frontend.js.loader.modules.extender.registry.JSModule;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackageDependency;
import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSBundleProcessor;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(immediate = true, service = JSBundleProcessor.class)
public class FlatNodeModulesBundleProcessor implements JSBundleProcessor {

	public static final String NAME = "name";
	public static final String MAIN = "main";
	public static final String TYPE = "npm-flat";
	public static final String VERSION = "version";
	public static final String DEPENDENCIES = "dependencies";

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
	public JSBundle process(ServiceReference<ServletContext> serviceReference) {
		Bundle bundle = serviceReference.getBundle();

		URL url = bundle.getResource("META-INF/resources/package.json");

		if (url == null) {
			return null;
		}

		ServletContext servletContext = _bundleContext.getService(
			serviceReference);

		JSBundle jsBundle = new JSBundle(bundle, servletContext);

		processRootPackage(bundle, jsBundle);

		processNodePackages(bundle, jsBundle);

		return jsBundle;
	}

	private void processRootPackage(Bundle bundle, JSBundle jsBundle) {
		JSPackage jsPackage = parsePackage(
			bundle, "META-INF/resources/package.json", true);

		jsBundle.addJSPackage(jsPackage);

		Collection<JSModule> jsModules = parseModules(
			bundle, "META-INF/resources");

		for (JSModule jsModule : jsModules) {
			jsPackage.addJSModule(jsModule);
		}
	}

	private void processNodePackages(Bundle bundle, JSBundle jsBundle) {
		List<JSPackage> jsPackages = new ArrayList<>();

		Enumeration<URL> urls = bundle.findEntries(
			"META-INF/resources", "package.json", true);

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();

			String path = url.getPath();

			if (path.equals("/META-INF/resources/package.json")) {
				continue;
			}

			jsPackages.add(parsePackage(bundle, path.substring(1), false));
		}

		for (JSPackage jsPackage : jsPackages) {
			jsBundle.addJSPackage(jsPackage);

			Collection<JSModule> jsModules = parseModules(
				bundle,
				"META-INF/resources/node_modules/" +
					jsPackage.getName() + StringPool.AT +
					jsPackage.getVersion() );

			for (JSModule jsModule : jsModules) {
				jsPackage.addJSModule(jsModule);
			}
		}
	}

	/**
	 *
	 * @param bundle
	 * @param location the bundle relative path of the package folder
	 * @return
	 */
	private Collection<JSModule> parseModules(Bundle bundle, String location) {
		List<JSModule> jsModules = new ArrayList<>();

		String nodeModulesPath = StringPool.SLASH + location + "/node_modules/";

		Enumeration<URL> urls = bundle.findEntries(location, "*.js", true);

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();

			if (url.getPath().startsWith(nodeModulesPath)) {
				continue;
			}

			String name = url.getPath().substring(location.length() + 2);

			Collection<String> dependencies;

			try {
				dependencies = parseModuleDependencies(url);
			}
			catch (IOException e) {
				_logger.log(Logger.LOG_WARNING, "Unable to read " + url, e);

				continue;
			}

			jsModules.add(new JSModule(name, dependencies));
		}

		return jsModules;
	}

	/**
	 *
	 * @param bundle
	 * @param location the bundle relative path to a package.json file
	 * @return
	 */
	private JSPackage parsePackage(
		Bundle bundle, String location, boolean root) {

		JSONObject jsonObject = null;

		try {
			jsonObject = JSONFactoryUtil.createJSONObject(
				getResourceContent(bundle, location));
		}
		catch (JSONException e) {
			_logger.log(Logger.LOG_WARNING, "Unable to parse root package", e);

			return null;
		}

		JSPackage jsPackage = new JSPackage(
			jsonObject.getString(NAME), jsonObject.getString(VERSION),
			jsonObject.getString(MAIN), root);

		JSONObject dependencies = jsonObject.getJSONObject(DEPENDENCIES);

		Iterator<String> dependencyNames = dependencies.keys();

		while (dependencyNames.hasNext()) {
			String dependencyName = dependencyNames.next();
			String versionConstraints = dependencies.getString(dependencyName);

			jsPackage.addJSPackageDependency(
				new JSPackageDependency(dependencyName, versionConstraints));
		}

		// TODO: parse module aliases

		return jsPackage;
	}

	private Collection<String> parseModuleDependencies(URL url)
		throws IOException {

		String urlContent = StringUtil.read(url.openStream());

		Matcher matcher = _MODULE_DEFINITION_PATTERN.matcher(urlContent);

		if (!matcher.lookingAt()) {
			return Collections.emptyList();
		}

		String[] dependencies = matcher.group(1).split(",");

		for (int i = 0; i < dependencies.length; i++) {
			dependencies[i] = dependencies[i].trim();
			dependencies[i] = dependencies[i].replaceAll("'", "");
			dependencies[i] = dependencies[i].replaceAll("\"", "");
		}

		return Arrays.asList(dependencies);
	}

	private String getResourceContent(Bundle bundle, String location) {
		URL url = bundle.getResource(location);

		if (url == null) {
			return null;
		}

		try {
			return StringUtil.read(url.openStream());
		}
		catch (IOException e) {
			return null;
		}
	}

	private static final Pattern _MODULE_DEFINITION_PATTERN = Pattern.compile(
		"Liferay\\.Loader\\.define.*\\[(.*)\\].*function", Pattern.MULTILINE);

	private BundleContext _bundleContext;
	private Logger _logger;
	private String _indent = "";
}

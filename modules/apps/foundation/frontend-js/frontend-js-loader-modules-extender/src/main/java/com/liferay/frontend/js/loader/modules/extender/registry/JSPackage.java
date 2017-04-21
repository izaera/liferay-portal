package com.liferay.frontend.js.loader.modules.extender.registry;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.yuchi.semver.Version;
import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSBundleObject;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

public class JSPackage implements JSBundleObject {

	public JSPackage(
		String name, String version, String main, boolean root) {

		_name = name;
		_version = Version.from(version, true);
		_main = Validator.isNull(main) ? "index.js" : main;
		_root = root;
	}

	public void addJSModule(JSModule jsModule) {
		jsModule.setJSPackage(this);

		_jsModules.add(jsModule);
	}

	public void addJSModuleAlias(JSModuleAlias jsModuleAlias) {
		jsModuleAlias.setJSPackage(this);

		_jsModuleAliases.add(jsModuleAlias);
	}

	public void addJSPackageDependency(
		JSPackageDependency jsPackageDependency) {

		jsPackageDependency.setJSPackage(this);

		_jsPackageDependencies.put(
			jsPackageDependency.getName(), jsPackageDependency);
	}

	public JSBundle getJSBundle() {
		return _jsBundle;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	public Version getVersion() {
		return _version;
	}

	public String getMain() {
		return _main;
	}

	public Collection<JSModule> getJSModules() {
		return _jsModules;
	}

	public Collection<JSModuleAlias> getJSModuleAliases() {
		return _jsModuleAliases;
	}

	public Collection<JSPackageDependency> getJSPackageDependencies() {
		return _jsPackageDependencies.values();
	}

	public JSPackageDependency getJSPackageDependency(String dependency) {
		return _jsPackageDependencies.get(dependency);
	}


	public URL getResource(String location) {
		StringBundler path = new StringBundler();

		if (_root) {
			path.append("META-INF/resources/");
		} else {
			path.append("META-INF/resources/node_modules/");
			path.append(_name);
			path.append(StringPool.AT);
			path.append(_version);
			path.append(StringPool.SLASH);
		}

		path.append(location);

		return _jsBundle.getResource(path.toString());
	}

	@Override
	public String toString() {
		return getId();
	}

	protected void setJSBundle(JSBundle jsBundle) {
		if (_jsBundle != null) {
			throw new IllegalStateException(
				"Package " + getId() + " is already contained in bundle " +
				_jsBundle.getId());
		}

		_jsBundle = jsBundle;
		_id = jsBundle.getId() + StringPool.SLASH + _name + StringPool.AT +
			_version;
	}

	private JSBundle _jsBundle;
	private Map<String, JSPackageDependency> _jsPackageDependencies =
		new HashMap<>();
	private String _id;
	private String _name;
	private String _main;
	private List<JSModule> _jsModules = new ArrayList<>();
	private List<JSModuleAlias> _jsModuleAliases = new ArrayList<>();
	private Version _version;
	private boolean _root;
}
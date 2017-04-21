package com.liferay.frontend.js.loader.modules.extender.registry;

import com.liferay.frontend.js.loader.modules.extender.internal.registry.PackageRegistry;

public class JSPackageDependency {

	public JSPackageDependency(String name, String versionConstraints) {
		_name = name;
		_versionConstraints = versionConstraints;
	}

	public JSPackage getJSPackage() {
		return _jsPackage;
	}

	public String getName() {
		return _name;
	}

	public String getVersionConstraints() {
		return _versionConstraints;
	}

	@Override
	public String toString() {
		return getName() + ":" + getVersionConstraints();
	}

	protected void setJSPackage(JSPackage jsPackage) {
		if (_jsPackage != null) {
			throw new IllegalStateException(
				"Package dependency " + getName() +
				" is already contained in package " + _jsPackage.getId());
		}

		_jsPackage = jsPackage;
	}

	private JSPackage _jsPackage;
	private String _name;
	private String _versionConstraints;
}
package com.liferay.frontend.js.loader.modules.extender.registry;

import com.liferay.portal.kernel.util.StringPool;

public class JSPackageDependency {

	public JSPackageDependency(String packageName, String versionConstraints) {
		_packageName = packageName;
		_versionConstraints = versionConstraints;
	}

	public JSPackage getJSPackage() {
		return _jsPackage;
	}

	public String getPackageName() {
		return _packageName;
	}

	public String getVersionConstraints() {
		return _versionConstraints;
	}

	@Override
	public String toString() {
		return _packageName + StringPool.COLON + _versionConstraints;
	}

	protected void setJSPackage(JSPackage jsPackage) {
		if (_jsPackage != null) {
			throw new IllegalStateException(
				"Package dependency " + _packageName +
				" is already contained in package " + _jsPackage.getId());
		}

		_jsPackage = jsPackage;
	}

	private JSPackage _jsPackage;
	private String _packageName;
	private String _versionConstraints;
}
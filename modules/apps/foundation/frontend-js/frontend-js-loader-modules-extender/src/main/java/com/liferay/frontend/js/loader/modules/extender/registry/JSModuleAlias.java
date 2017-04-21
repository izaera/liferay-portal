package com.liferay.frontend.js.loader.modules.extender.registry;

public class JSModuleAlias {

	public JSModuleAlias(String name, String alias) {
		_name = name;
		_alias = alias;
	}

	public JSPackage getJsPackage() {
		return _jsPackage;
	}

	public String getName() {
		return _name;
	}

	public String getAlias() {
		return _alias;
	}

	@Override
	public String toString() {
		return getAlias() + "~=" + getName();
	}

	protected void setJSPackage(JSPackage jsPackage) {
		if (_jsPackage != null) {
			throw new IllegalStateException(
				"Module alias " + getAlias() +
				" is already contained in package " + _jsPackage.getId());
		}

		_jsPackage = jsPackage;
	}

	private String _alias;
	private JSPackage _jsPackage;
	private String _name;
}

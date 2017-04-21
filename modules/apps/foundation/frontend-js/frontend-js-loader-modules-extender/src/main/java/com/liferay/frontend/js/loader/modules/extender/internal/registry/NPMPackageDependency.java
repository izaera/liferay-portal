//package com.liferay.frontend.js.loader.modules.extender.internal.registry;
//
//import com.github.yuchi.semver.Range;
//import com.liferay.frontend.js.loader.modules.extender.registry.Package;
//import com.liferay.frontend.js.loader.modules.extender.registry.Dependency;
//
//public class NPMPackageDependency implements Dependency {
//
//	public NPMPackageDependency(String name, String value) {
//		_range = Range.from(value, true);
//		_pkgIdentifier = new NPMPackageIdentifier(name);
//		_name = name;
//		_value = value;
//	}
//
//	@Override
//	public String getName() {
//		return _name;
//	}
//
//	@Override
//	public PackageIdentifier getPackageIdentifier() {
//		return _pkgIdentifier;
//	}
//
//	@Override
//	public boolean matches(Package pkgConfig) {
//		if (matches(pkgConfig.getPackageIdentifier()) &&
//			_range.test(pkgConfig.getVersionConstraints())) {
//
//			return true;
//		}
//		else {
//			return false;
//		}
//	}
//
//	@Override
//	public boolean matches(PackageIdentifier pkgIdentifier) {
//		return pkgIdentifier.equals(getPackageIdentifier());
//	}
//
//	@Override
//	public String toString() {
//		return _name + " " + _value;
//	}
//
//	private String _name = null;
//	private String _value = null;
//	private Range _range = null;
//	private PackageIdentifier _pkgIdentifier = null;
//
//}

package com.liferay.frontend.js.loader.modules.extender.registry;

public interface PackageDependency {

	/**
	 * @return the contextual name of this dependency
	 */
	public String getName();

	/**
	 * @return the identifier of the resolved dependency
	 */
	public PackageIdentifier getPackageIdentifier();

	public boolean matches(PackageConfig pkgConfig);

	public boolean matches(PackageIdentifier pkgIdentifier);

}
package com.liferay.frontend.js.loader.modules.extender.registry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.github.yuchi.semver.Version;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

public class PackageConfig {

	public PackageConfig(
		String name, PackageIdentifier pkgIdentifier, String version,
		String main, List<PackageDependency> dependencies,
		List<ModuleAlias> moduleAliases, String servletPackagePath,
		BundleConfig bundleConfig) {

		if (servletPackagePath.endsWith(StringPool.SLASH)) {
			servletPackagePath = servletPackagePath.substring(
				0, servletPackagePath.length()-1);
		}

		_name = name;
		_version = Version.from(version, true);
		_main = main;
		_moduleAliases = moduleAliases;
		_dependencies = dependencies;
		_pkgIdentifier = pkgIdentifier;
		_bundleConfig = bundleConfig;
		_servletPackagePath = servletPackagePath;
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof PackageConfig)) {
			return false;
		}

		PackageConfig other = (PackageConfig)obj;

		if (StringUtil.equalsIgnoreCase(getName(), other.getName()) &&
			getVersion().equals(other.getVersion())) {

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = _name.toLowerCase().hashCode();
		result = 31 * result + _version.hashCode();
		return result;
	}

	public List<PackageDependency> getDependencies() {
		return _dependencies;
	}

	public String getIdentifier() {
		StringBundler sb = new StringBundler();

		sb.append(getName());
		sb.append(StringPool.AT);
		sb.append(getVersion());

		return sb.toString();
	}

	public String getMain() {
		if (Validator.isNull(_main)) {
			return "index.js";
		}
		else {
			return _main;
		}
	}

	public List<ModuleAlias> getModuleAliases() {
		return _moduleAliases;
	}

	public String getName() {
		return _name;
	}

	public PackageIdentifier getPackageIdentifier() {
		return _pkgIdentifier;
	}

	public String getPath() {
		StringBundler sb = new StringBundler();

		sb.append(PortalUtil.getPathContext());
		sb.append("/o/pkg/");
		sb.append(getIdentifier());
		sb.append("/");

		return sb.toString();
	}

	public URL getResource(String location) throws MalformedURLException {
		return _bundleConfig.getResource(_servletPackagePath + StringPool.SLASH + location);
	}

	public String getServletContextPath() {
		return _bundleConfig.getServletContextPath();
	}

	public Version getVersion() {
		return _version;
	}

	@Override
	public String toString() {
		return _name + "@" + _version;
	}

	private String _servletPackagePath;
	private PackageIdentifier _pkgIdentifier;
	private List<PackageDependency> _dependencies;
	private String _name;
	private String _main;
	private List<ModuleAlias> _moduleAliases;
	private BundleConfig _bundleConfig;
	private Version _version;

}
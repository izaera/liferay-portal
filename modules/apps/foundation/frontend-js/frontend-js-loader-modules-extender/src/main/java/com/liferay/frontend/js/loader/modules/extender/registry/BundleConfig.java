package com.liferay.frontend.js.loader.modules.extender.registry;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.liferay.portal.kernel.json.JSONObject;

public class BundleConfig {

	public BundleConfig(ServletContext servletContext) {
		_pkgConfigs = new ArrayList<PackageConfig>();
		_servletContext = servletContext;
	}

	public void addPackageConfig(PackageConfig pkgConfig) {
		_pkgConfigs.add(pkgConfig);
	}

	public List<PackageConfig> getPackageConfigs() {
		return _pkgConfigs;
	}

	public URL getResource(String path) throws MalformedURLException {
		URL url = _servletContext.getResource(path);
		
		if (url != null) {
			return url;
		}
		else {
			return _servletContext.getResource(path + ".js");
		}
	}

	public String getServletContextPath() {
		return _servletContext.getContextPath();
	}

	private ServletContext _servletContext;
	private List<PackageConfig> _pkgConfigs;
}

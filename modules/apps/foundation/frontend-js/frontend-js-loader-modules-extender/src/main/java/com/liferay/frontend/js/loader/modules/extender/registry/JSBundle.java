package com.liferay.frontend.js.loader.modules.extender.registry;

import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSBundleObject;
import com.liferay.portal.kernel.util.StringPool;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

public class JSBundle implements JSBundleObject {

	public JSBundle(Bundle bundle, ServletContext servletContext) {
		_bundle = bundle;
		_servletContext = servletContext;
		_jsPackages = new ArrayList<>();

		_id = Long.toString(_bundle.getBundleId());
	}

	public void addJSPackage(JSPackage jsPackage) {
		jsPackage.setJSBundle(this);

		_jsPackages.add(jsPackage);
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _bundle.getSymbolicName();
	}

	public String getVersion() {
		return _bundle.getVersion().toString();
	}

	public Collection<JSPackage> getJSPackages() {
		return _jsPackages;
	}

	public URL getResource(String location) {
		return _bundle.getResource(location);
	}

	@Override
	public String toString() {
		return getId();
	}

	private final Bundle _bundle;
	private final ServletContext _servletContext;
	private final List<JSPackage> _jsPackages;
	private final String _id;
}

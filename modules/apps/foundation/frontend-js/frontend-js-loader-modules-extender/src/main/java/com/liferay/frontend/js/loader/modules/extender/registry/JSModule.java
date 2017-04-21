package com.liferay.frontend.js.loader.modules.extender.registry;

import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSBundleAsset;
import com.liferay.frontend.js.loader.modules.extender.registry.definitions.JSResolvableBundleAsset;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class JSModule implements JSResolvableBundleAsset {

	public JSModule(String name, Collection<String> dependencies) {
		_name = name;
		_dependencies = dependencies;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public String getName() {
		return _name;
	}

	public JSPackage getJSPackage() {
		return _jsPackage;
	}

	public Collection<String> getDependencies() {
		return _dependencies;
	}

	@Override
	public String getURL() {
		return _url;
	}

	@Override
	public String getResolvedURL() {
		return _resolvedURL;
	}

	@Override
	public String getResolvedId() {
		return _resolvedId;
	}

	@Override
	public InputStream openStream() throws IOException {
		return _jsPackage.getResource(_name).openStream();
	}

	@Override
	public String toString() {
		return getId();
	}

	protected void setJSPackage(JSPackage jsPackage) {
		if (_jsPackage != null) {
			throw new IllegalStateException(
				"Module " + getId() + " is already contained in package " +
				_jsPackage.getId());
		}

		_jsPackage = jsPackage;
		_id = _jsPackage.getId() + StringPool.SLASH + _name;
		_url = "/o/js/module/" + _id;
		_resolvedId =
			jsPackage.getName() + StringPool.AT + jsPackage.getVersion() +
			StringPool.SLASH + _name;
		_resolvedURL = "/o/js/resolved-module/" + _resolvedId;
	}

	private JSPackage _jsPackage;
	private String _name;
	private String _id;
	private String _url;
	private String _resolvedURL;
	private String _resolvedId;
	private Collection<String> _dependencies;
}

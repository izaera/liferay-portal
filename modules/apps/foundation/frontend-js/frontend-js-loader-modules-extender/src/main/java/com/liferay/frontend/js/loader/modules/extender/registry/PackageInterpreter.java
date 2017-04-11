package com.liferay.frontend.js.loader.modules.extender.registry;

import org.osgi.framework.ServiceReference;

import javax.servlet.ServletContext;

public interface PackageInterpreter {

	public String getType();

	public BundleConfig interpret(
		ServiceReference<ServletContext> serviceReference);

}

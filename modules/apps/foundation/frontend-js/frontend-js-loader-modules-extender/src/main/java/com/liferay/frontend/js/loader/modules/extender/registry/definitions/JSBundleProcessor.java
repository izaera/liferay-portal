package com.liferay.frontend.js.loader.modules.extender.registry.definitions;

import com.liferay.frontend.js.loader.modules.extender.registry.JSBundle;
import org.osgi.framework.ServiceReference;

import javax.servlet.ServletContext;

public interface JSBundleProcessor {

	public String getType();

	public JSBundle process(ServiceReference<ServletContext> serviceReference);

}

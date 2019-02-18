package com.liferay.frontend.taglib.clay.servlet.taglib.data.provider;

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProviderRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = {})
public class ClayComponentDataProviderRegistryUtil {

	public static ClayComponentDataProviderRegistry getRegistry() {
		if (_instance == null) {
			return null;
		}

		return _instance._registry;
	}

	public ClayComponentDataProviderRegistryUtil() {
		_instance = this;
	}

	private static ClayComponentDataProviderRegistryUtil _instance;

	@Reference
	private ClayComponentDataProviderRegistry _registry;

}
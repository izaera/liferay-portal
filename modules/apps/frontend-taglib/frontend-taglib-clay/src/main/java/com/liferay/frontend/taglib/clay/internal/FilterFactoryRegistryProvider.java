/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package com.liferay.frontend.taglib.clay.internal;

import com.liferay.frontend.taglib.clay.data.provider.FilterFactoryRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = FilterFactoryRegistryProvider.class)
public class FilterFactoryRegistryProvider {

	public static FilterFactoryRegistry getRegistry() {
		if (_instance == null) {
			return null;
		}

		return _instance._registry;
	}

	public FilterFactoryRegistryProvider() {
		_instance = this;
	}

	private static FilterFactoryRegistryProvider _instance;

	@Reference
	private FilterFactoryRegistry _registry;

}
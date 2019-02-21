/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.frontend.taglib.clay.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributorRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentItemBuilder;
import com.liferay.frontend.taglib.clay.data.contributor.ClayTagMetaAttributeContributorRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.FilterFactoryRegistry;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.PaginationEntriesHelper;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = ServletContextUtil.class)
public class ServletContextUtil {

	public static ClayComponentItemBuilder getClayComponentItemBuilder() {
		return _instance._clayComponentItemBuilder;
	}

	public static ClayComponentDataContributorRegistry getDataContributorRegistry() {
		return _instance._dataContributorRegistry;
	}

	public static FilterFactoryRegistry getFilterFactoryRegistry() {
		return _instance._filterFactoryRegistry;
	}

	public static ClayTagMetaAttributeContributorRegistry getMetaAttributeContributorRegistry() {
		return _instance._metaAttributeContributorRegistry;
	}

	public static PaginationEntriesHelper getPaginationEntriesHelper() {
		return _instance._paginationEntriesHelper;
	}

	@Activate
	protected void activate() {
		_instance = this;
	}

	@Deactivate
	protected void deactivate() {
		_instance = null;
	}

	private static ServletContextUtil _instance;

	@Reference
	private ClayComponentItemBuilder _clayComponentItemBuilder;

	@Reference
	private ClayComponentDataContributorRegistry _dataContributorRegistry;

	@Reference
	private FilterFactoryRegistry _filterFactoryRegistry;

	@Reference
	private ClayTagMetaAttributeContributorRegistry
		_metaAttributeContributorRegistry;

	@Reference
	private PaginationEntriesHelper _paginationEntriesHelper;

}
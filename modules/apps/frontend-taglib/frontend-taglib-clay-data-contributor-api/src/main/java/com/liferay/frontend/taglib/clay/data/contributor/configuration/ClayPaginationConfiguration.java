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

package com.liferay.frontend.taglib.clay.data.contributor.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Rodolfo Roza Miranda
 */
@ExtendedObjectClassDefinition(category = "pagination")
@Meta.OCD(
	id = "com.liferay.frontend.taglib.clay.data.contributor.configuration.ClayPaginationConfiguration",
	localization = "content/Language", name = "clay-pagination-configuration-name"
)
public interface ClayPaginationConfiguration {

	@Meta.AD(deflt = "1", name = "current-page-default-value", required = false)
	public int currentPageDefaultValue();

	@Meta.AD(deflt = "delta", name = "default-delta-param", required = false)
	public String defaultDeltaParam();

	@Meta.AD(deflt = "5", name = "items-per-page-default-value", required = false)
	public int itemsPerPageDefaultValue();

	@Meta.AD(deflt = "200", name = "max-delta", required = false)
	public int maxDelta();

	@Meta.AD(deflt = "5,10,20,30,50,75", name = "page-delta-values", required = false)
	public Integer[] pageDeltaValues();

}
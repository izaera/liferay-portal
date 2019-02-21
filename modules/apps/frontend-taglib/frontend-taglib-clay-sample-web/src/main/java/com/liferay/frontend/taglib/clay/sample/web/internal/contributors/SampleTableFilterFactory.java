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

package com.liferay.frontend.taglib.clay.sample.web.internal.contributors;

import com.liferay.frontend.taglib.clay.data.contributor.Filter;
import com.liferay.frontend.taglib.clay.data.contributor.FilterFactory;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true, property = "contributor.name=SampleTable",
	service = FilterFactory.class
)
public class SampleTableFilterFactory implements FilterFactory {

	@Override
	public Filter create(HttpServletRequest httpServletRequest) {
		return new SampleFilter(false);
	}

}
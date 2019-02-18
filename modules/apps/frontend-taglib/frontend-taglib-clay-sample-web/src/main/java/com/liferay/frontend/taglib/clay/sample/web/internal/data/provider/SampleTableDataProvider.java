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

package com.liferay.frontend.taglib.clay.sample.web.internal.data.provider;

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProvider;
import com.liferay.frontend.taglib.clay.data.provider.Pagination;
import com.liferay.frontend.taglib.clay.sample.web.internal.display.context.TablesDisplayContext;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true,
	property = "clay.component.data.provider.key=SampleTableDataProvider",
	service = ClayComponentDataProvider.class
)
public class SampleTableDataProvider
	implements ClayComponentDataProvider<TablesDisplayContext.Item> {

	@Override
	public List<TablesDisplayContext.Item> getItems(
		HttpServletRequest request, Pagination pagination) {

		ArrayList<TablesDisplayContext.Item> items = new ArrayList<>();

		items.add(new TablesDisplayContext.Item("Lemon", 14, "Yellow", false));
		items.add(new TablesDisplayContext.Item("Lime", 11, "Green", false));
		items.add(
			new TablesDisplayContext.Item("Grapefruit", 40, "Yellow", false));

		return items;
	}

}
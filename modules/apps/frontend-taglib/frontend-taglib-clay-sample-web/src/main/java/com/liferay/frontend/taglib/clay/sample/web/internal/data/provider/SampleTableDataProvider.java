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
import com.liferay.frontend.taglib.clay.data.provider.Filter;
import com.liferay.frontend.taglib.clay.data.provider.Pagination;
import com.liferay.frontend.taglib.clay.sample.web.internal.model.Item;

import java.util.Arrays;
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
	implements ClayComponentDataProvider<Item> {

	public SampleTableDataProvider() {
		_items = Arrays.asList(
			new Item("Banana", 89, "yellow", false),
			new Item("Apple", 52, "red", true),
			new Item("Pear", 58, "green", true),
			new Item("Pomegranate", 68, "yellowish", false),
			new Item("Lemon", 14, "Yellow", false),
			new Item("Lime", 11, "Green", false),
			new Item("Grapefruit", 40, "Yellow", false));
	}

	@Override
	public int countItems(HttpServletRequest request, Filter filter) {
		return _items.size();
	}

	@Override
	public List<Item> getItems(
		HttpServletRequest request, Filter filter, Pagination pagination) {

		if (pagination == null) {
			return _items;
		}

		int endPosition = _items.size();

		if (pagination.getEndPosition() < _items.size()) {
			endPosition = pagination.getEndPosition();
		}

		return _items.subList(pagination.getStartPosition(), endPosition);
	}

	private final List<Item> _items;

}
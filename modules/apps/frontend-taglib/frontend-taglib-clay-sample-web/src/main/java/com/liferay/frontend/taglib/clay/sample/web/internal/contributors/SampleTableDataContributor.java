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

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributor;
import com.liferay.frontend.taglib.clay.data.contributor.Filter;
import com.liferay.frontend.taglib.clay.data.contributor.Pagination;
import com.liferay.frontend.taglib.clay.sample.web.internal.model.Item;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true, property = "contributor.name=SampleTable",
	service = ClayComponentDataContributor.class
)
public class SampleTableDataContributor
	implements ClayComponentDataContributor<Item> {

	public SampleTableDataContributor() {
		_items = Arrays.asList(
			new Item("Banana", 89, "yellow", false),
			new Item("Apple", 52, "red", true),
			new Item("Pear", 58, "green", true),
			new Item("Pomegranate", 68, "yellowish", false),
			new Item("Lemon", 14, "Yellow", false),
			new Item("Lime", 11, "Green", false),
			new Item("Grapefruit", 40, "Yellow", false),
			new Item("Orange", 47, "Orange", false),
			new Item("Pineapple", 50, "Yellow", false),
			new Item("Avocado", 160, "Green", false));
	}

	@Override
	public int countItems(HttpServletRequest request, Filter filter) {
		List<Item> filteredItems = _filter(_items, filter);

		return filteredItems.size();
	}

	@Override
	public List<Item> getItems(
		HttpServletRequest request, Filter filter, Pagination pagination) {

		List<Item> items = _filter(_items, filter);

		if (pagination != null) {
			return _getPaginatedItems(items, pagination);
		}

		return items;
	}

	private List<Item> _filter(List<Item> items, Filter filter) {
		if (filter == null) {
			return items;
		}

		SampleFilter sampleFilter = (SampleFilter)filter;

		Stream<Item> stream = items.stream();

		return stream.filter(
			item -> item.isSkinEdible() == sampleFilter.isSkinEdible()
		).collect(
			Collectors.toList()
		);
	}

	private List<Item> _getPaginatedItems(
		List<Item> items, Pagination pagination) {

		int endPosition = items.size();

		if (pagination.getEndPosition() < items.size()) {
			endPosition = pagination.getEndPosition();
		}

		return items.subList(pagination.getStartPosition(), endPosition);
	}

	private final List<Item> _items;

}
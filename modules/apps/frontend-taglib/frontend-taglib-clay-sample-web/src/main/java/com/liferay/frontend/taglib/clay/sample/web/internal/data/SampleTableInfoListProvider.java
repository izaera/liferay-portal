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

package com.liferay.frontend.taglib.clay.sample.web.internal.data;

import com.liferay.info.pagination.Pagination;
import com.liferay.info.provider.InfoListProvider;
import com.liferay.info.provider.InfoListProviderContext;
import com.liferay.info.sort.Sort;
import com.liferay.petra.string.StringPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = InfoListProvider.class)
public class SampleTableInfoListProvider
	implements InfoListProvider<Map<String, Object>> {

	public SampleTableInfoListProvider() {
		_items = Arrays.asList(
			_getItem("Blueberry", 57, 100), _getItem("Strawberry", 33, 100),
			_getItem("Raspberry", 53, 100));
	}

	@Override
	public List<Map<String, Object>> getInfoList(
		InfoListProviderContext infoListProviderContext) {

		return _items;
	}

	@Override
	public List<Map<String, Object>> getInfoList(
		InfoListProviderContext infoListProviderContext, Pagination pagination,
		Sort sort) {

		List<Map<String, Object>> items = new ArrayList(_items);

		if (sort != null) {
			items.sort(
				(map1, map2) -> {
					int result;

					Object field1 = map1.get(sort.getFieldName());
					Object field2 = map2.get(sort.getFieldName());

					if ((field1 == null) && (field2 == null)) {
						result = 0;
					}
					else if (field1 == null) {
						result = -1;
					}
					else if (field2 == null) {
						result = 1;
					}
					else {
						result = ((Comparable)field1).compareTo(field2);
					}

					if (sort.isReverse()) {
						return -result;
					}

					return result;
				});
		}

		if (pagination != null) {
			items = items.subList(
				pagination.getStart(),
				Math.min(_items.size(), pagination.getEnd()));
		}

		return items;
	}

	@Override
	public int getInfoListCount(
		InfoListProviderContext infoListProviderContext) {

		return _items.size();
	}

	@Override
	public String getLabel(Locale locale) {
		return StringPool.BLANK;
	}

	private Map<String, Object> _getItem(
		String name, int calories, int portion) {

		Map<String, Object> item = new HashMap<>();

		item.put("calories", calories);
		item.put("name", name);
		item.put("portion", portion);

		return item;
	}

	private final List<Map<String, Object>> _items;

}
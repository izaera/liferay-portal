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

package com.liferay.dataset.internal.ui.filter.checkbox;

import com.liferay.dataset.ui.filter.DatasetFilter;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributor;
import com.liferay.dataset.ui.filter.checkbox.BaseCheckboxDatasetFilter;
import com.liferay.dataset.ui.filter.checkbox.CheckboxDatasetFilterItem;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "dataset.filter.type=checkbox",
	service = DatasetFilterContextContributor.class
)
public class CheckBoxDatasetFilterContextContributor
	implements DatasetFilterContextContributor {

	@Override
	public Map<String, Object> getDatasetFilterContext(
		DatasetFilter datasetFilter, Locale locale) {

		if (datasetFilter instanceof BaseCheckboxDatasetFilter) {
			return _serialize((BaseCheckboxDatasetFilter)datasetFilter, locale);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseCheckboxDatasetFilter baseCheckboxDatasetFilter, Locale locale) {

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		ResourceBundle resourceBundle = ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());

		List<CheckboxDatasetFilterItem> checkboxDatasetFilterItems =
			baseCheckboxDatasetFilter.getCheckboxDatasetFilterItems(locale);

		for (CheckboxDatasetFilterItem checkboxDatasetFilterItem :
				checkboxDatasetFilterItems) {

			jsonArray.put(
				JSONUtil.put(
					"label",
					LanguageUtil.get(
						resourceBundle, checkboxDatasetFilterItem.getLabel())
				).put(
					"value", checkboxDatasetFilterItem.getValue()
				));
		}

		return HashMapBuilder.<String, Object>put(
			"items", jsonArray
		).build();
	}

	@Reference
	private JSONFactory _jsonFactory;

}
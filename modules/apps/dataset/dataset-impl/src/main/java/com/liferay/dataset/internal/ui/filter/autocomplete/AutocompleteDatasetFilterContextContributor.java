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

package com.liferay.dataset.internal.ui.filter.autocomplete;

import com.liferay.dataset.ui.filter.DatasetFilter;
import com.liferay.dataset.ui.filter.DatasetFilterContextContributor;
import com.liferay.dataset.ui.filter.autocomplete.BaseAutocompleteDatasetFilter;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Marco Leo
 */
@Component(
	property = "dataset.filter.type=autocomplete",
	service = DatasetFilterContextContributor.class
)
public class AutocompleteDatasetFilterContextContributor
	implements DatasetFilterContextContributor {

	@Override
	public Map<String, Object> getDatasetFilterContext(
		DatasetFilter datasetFilter, Locale locale) {

		if (datasetFilter instanceof BaseAutocompleteDatasetFilter) {
			return _serialize(
				(BaseAutocompleteDatasetFilter)datasetFilter, locale);
		}

		return Collections.emptyMap();
	}

	private Map<String, Object> _serialize(
		BaseAutocompleteDatasetFilter baseAutocompleteDatasetFilter,
		Locale locale) {

		return HashMapBuilder.<String, Object>put(
			"apiURL", baseAutocompleteDatasetFilter.getAPIURL()
		).put(
			"inputPlaceholder",
			LanguageUtil.get(
				locale, baseAutocompleteDatasetFilter.getPlaceholder())
		).put(
			"itemKey", baseAutocompleteDatasetFilter.getItemKey()
		).put(
			"itemLabel", baseAutocompleteDatasetFilter.getItemLabel()
		).put(
			"selectionType",
			() -> {
				if (baseAutocompleteDatasetFilter.isMultipleSelection()) {
					return "multiple";
				}

				return "single";
			}
		).build();
	}

}
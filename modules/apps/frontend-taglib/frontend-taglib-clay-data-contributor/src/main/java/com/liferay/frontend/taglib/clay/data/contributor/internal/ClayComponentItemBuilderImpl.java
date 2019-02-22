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

package com.liferay.frontend.taglib.clay.data.contributor.internal;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentActionContributor;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentActionContributorRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentItemBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = ClayComponentItemBuilder.class)
public class ClayComponentItemBuilderImpl implements ClayComponentItemBuilder {

	@Override
	public List<Object> build(
			HttpServletRequest request, String contributorName,
			List<Object> items)
		throws Exception {

		List<Object> newItems = new ArrayList<>();

		for (Object item : items) {
			ClayComponentItem clayComponentItem = new ClayComponentItem(item);

			_addActionItems(request, contributorName, clayComponentItem);

			newItems.add(clayComponentItem);
		}

		String newItemsJSON = _OBJECT_MAPPER.writeValueAsString(newItems);

		return (List<Object>)JSONFactoryUtil.looseDeserialize(newItemsJSON);
	}

	private void _addActionItems(
			HttpServletRequest request, String contributorName,
			ClayComponentItem item)
		throws PortalException {

		List<ClayComponentActionContributor> contributors =
			_actionContributorRegistry.getActionContributors(contributorName);

		if (contributors == null) {
			return;
		}

		long groupId = _getGroupId(request);

		for (ClayComponentActionContributor contributor : contributors) {
			item.addActionItems(contributor.getActions(request, groupId, item));
		}
	}

	private long _getGroupId(HttpServletRequest request) {
		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		return themeDisplay.getScopeGroupId();
	}

	private static final ObjectMapper _OBJECT_MAPPER = new ObjectMapper() {
		{
			configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
			enable(SerializationFeature.INDENT_OUTPUT);
		}
	};

	@Reference
	private ClayComponentActionContributorRegistry _actionContributorRegistry;

}
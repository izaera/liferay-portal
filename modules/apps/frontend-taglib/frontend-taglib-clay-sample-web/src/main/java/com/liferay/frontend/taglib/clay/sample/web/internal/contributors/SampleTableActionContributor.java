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

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentAction;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentActionContributor;
import com.liferay.portal.kernel.language.LanguageUtil;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(
	immediate = true,
	property = "contributor.name=SampleTable",
	service = ClayComponentActionContributor.class
)
public class
SampleTableActionContributor implements ClayComponentActionContributor {

	@Override
	public List<ClayComponentAction> getActions(
		HttpServletRequest request, long groupId, Object model) {

		List<ClayComponentAction> actions = new ArrayList<>();

		String delete = LanguageUtil.get(request, "delete");

		String edit = LanguageUtil.get(request, "edit");

		actions.add(new ClayComponentAction("", "trash", delete, true, true));
		actions.add(new ClayComponentAction("", "pencil", edit, true, false));

		return actions;
	}

}
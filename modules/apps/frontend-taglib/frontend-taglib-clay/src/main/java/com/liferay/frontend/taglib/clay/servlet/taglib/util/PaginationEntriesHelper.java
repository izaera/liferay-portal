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

package com.liferay.frontend.taglib.clay.servlet.taglib.util;

import com.liferay.frontend.taglib.clay.internal.model.ClayPaginationEntry;

import java.util.List;

import javax.portlet.PortletURL;

/**
 * @author Rodolfo Roza Miranda
 */
public interface PaginationEntriesHelper {

	public List<ClayPaginationEntry> getPaginationEntries(
		PortletURL portletURL, String namespace, String deltaParam);

}
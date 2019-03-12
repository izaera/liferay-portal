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

package com.liferay.frontend.taglib.clay.servlet.taglib.data;

/**
 * @author Marco Leo
 */
public class Pagination {

	public Pagination(int pageSize, int page) {
		_pageSize = pageSize;
		_page = page;
	}

	public int getEnd() {
		return _page * _pageSize;
	}

	public int getPage() {
		return _page;
	}

	public int getPageSize() {
		return _pageSize;
	}

	public int getStart() {
		return (_page - 1) * _pageSize;
	}

	private final int _page;
	private final int _pageSize;

}
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

package com.liferay.portal.kernel.portlet;

/**
 * @author Brian Wing Shun Chan
 */
public class PortletBagPool {

	public static PortletBag get(long companyId, String portletId) {
		return _portletBagPoolMap.get(companyId, portletId);
	}

	public static void put(
		long companyId, String portletId, PortletBag portletBag) {

		_portletBagPoolMap.put(companyId, portletId, portletBag);
	}

	public static PortletBag remove(long companyId, String portletId) {
		return _portletBagPoolMap.remove(companyId, portletId);
	}

	public static void reset(long companyId) {
		_portletBagPoolMap.clear(companyId);
	}

	private PortletBagPool() {
	}

	private static final CompanyPortletMap<PortletBag> _portletBagPoolMap =
		new CompanyPortletMap<>();

}
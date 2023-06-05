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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;

/**
 * @author Iván Zaera Avellón
 */
public class AmbientCompanyIdUtil {

	public static long getCompanyId() {
		long companyId = CompanyThreadLocal.getCompanyId();

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext != null) {
			companyId = serviceContext.getCompanyId();
		}

		if (companyId == CompanyConstants.SYSTEM) {
			Exception exception = new Exception(
				">>>>> Suspectful ambient companyId = " +
					CompanyConstants.SYSTEM + "<<<<<");

			exception.printStackTrace();
		}

		return companyId;
	}

}
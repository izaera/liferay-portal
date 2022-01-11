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

package com.liferay.remote.app.internal.upgrade.v2_4_0;

import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.remote.app.internal.instance.lifecycle.RemoteAppPortalInstanceLifecycleListener;

/**
 * @author Iván Zaera
 */
public class SampleRemoteAppEntryUpgradeProcess extends UpgradeProcess {

	public SampleRemoteAppEntryUpgradeProcess(
		CompanyLocalService companyLocalService,
		RemoteAppPortalInstanceLifecycleListener companyModelListener) {

		_companyLocalService = companyLocalService;
		_companyModelListener = companyModelListener;
	}

	@Override
	protected void doUpgrade() throws Exception {
		_companyLocalService.forEachCompany(
			company ->
				_companyModelListener.addSampleCustomElementRemoteAppEntry(
					company));
	}

	private final CompanyLocalService _companyLocalService;
	private final RemoteAppPortalInstanceLifecycleListener
		_companyModelListener;

}
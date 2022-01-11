package com.liferay.remote.app.internal.upgrade.v2_4_0;

import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.remote.app.internal.instance.lifecycle.RemoteAppPortalInstanceLifecycleListener;

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
	private final RemoteAppPortalInstanceLifecycleListener _companyModelListener;

}

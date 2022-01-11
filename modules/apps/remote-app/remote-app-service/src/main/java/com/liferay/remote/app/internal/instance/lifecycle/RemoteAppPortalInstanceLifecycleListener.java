package com.liferay.remote.app.internal.instance.lifecycle;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.remote.app.service.RemoteAppEntryLocalService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	service = {PortalInstanceLifecycleListener.class, RemoteAppPortalInstanceLifecycleListener.class}
)
public class RemoteAppPortalInstanceLifecycleListener extends
	BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceInitialized(Company company) {
		try {
			addSampleCustomElementRemoteAppEntry(company);
		}
		catch (PortalException portalException) {
			_log.error("Unable to add sample remote app", portalException);
		}
	}

	public void addSampleCustomElementRemoteAppEntry(Company company)
		throws PortalException {

		_remoteAppEntryLocalService
			.addOrUpdateCustomElementRemoteAppEntry(
				"SAMPLE_VANILLA_COUNTER",
				_userLocalService.getDefaultUserId(company.getCompanyId()),
				StringPool.BLANK, "vanilla-counter",
				"https://liferay.github.io/liferay-frontend-projects" +
				"/vanilla-counter/index.js",
				"See how a vanilla counter works as a remote app.",
				"vanilla_counter", false,
				Collections.singletonMap(
					LocaleUtil.getDefault(), "Vanilla Counter"),
				"category.remote-apps", "friendly-url-mapping=vanilla_counter",
				"https://liferay.github.io/liferay-frontend-projects",
				WorkflowConstants.STATUS_APPROVED);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RemoteAppPortalInstanceLifecycleListener.class);

	@Reference
	private RemoteAppEntryLocalService _remoteAppEntryLocalService;

	@Reference
	private UserLocalService _userLocalService;

}

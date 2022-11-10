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

package com.liferay.client.extension.web.internal.portlet.action;

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.PrintWriter;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
		"mvc.command.name=/client_extension_admin/get_manage_resources_summary"
	},
	service = MVCResourceCommand.class
)
public class GetManageResourcesSummaryMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		try {
			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put(
					"text",
					_language.format(
						_portal.getHttpServletRequest(resourceRequest),
						"x-folders-and-x-files-added",
						new Object[] {
							0,
							_clientExtensionRepository.getFileEntriesCount(
								ParamUtil.getLong(
									resourceRequest,
									"clientExtensionEntryId"),
								ClientExtensionRepository.Status.ALL)})
				));
		}
		catch (Exception exception) {
			String errorMessage = _language.get(
				_portal.getHttpServletRequest(resourceRequest),
				"an-unexpected-error-occurred");

			if (_log.isErrorEnabled()) {
				_log.error(exception);
			}

			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put("error", errorMessage));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		GetManageResourcesSummaryMVCResourceCommand.class);

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

}
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

import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.client.extension.service.ClientExtensionEntryService;
import com.liferay.client.extension.type.factory.CETFactory;
import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderURL;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import java.util.Locale;
import java.util.Map;

/**
 * @author Bruno Basto
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
		"mvc.command.name=/client_extension_admin/add_client_extension_entry"
	},
	service = MVCResourceCommand.class
)
public class AddClientExtensionEntryMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		try {
			ClientExtensionEntry clientExtensionEntry = _add(resourceRequest);

			PortletURL viewClientExtensionEntriesURL = PortletURLBuilder.create(
				_getRenderURL(resourceRequest)
			).setMVCRenderCommandName(
				"/"
			).buildPortletURL();

			PortletURL editClientExtensionEntryURL = PortletURLBuilder.create(
				_getRenderURL(resourceRequest)
			).setMVCRenderCommandName(
				"/client_extension_admin/edit_client_extension_entry"
			).setRedirect(
				viewClientExtensionEntriesURL
			).setParameter(
				"externalReferenceCode",
				clientExtensionEntry.getExternalReferenceCode()
			).buildPortletURL();

			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put(
					"redirect", editClientExtensionEntryURL
				));
		}
		catch (Exception exception) {
			String errorMessage = _language.get(
				_portal.getHttpServletRequest(resourceRequest),
				"an-unexpected-error-occurred");
			/*
			if (portalException instanceof NoSuchLayoutException) {
				errorMessage = "the-page-could-not-be-found";
			}
			else {
			*/
				if (_log.isErrorEnabled()) {
					_log.error(exception);
				}
			/*
			}
			*/

			JSONPortletResponseUtil.writeJSON(
				resourceRequest, resourceResponse,
				JSONUtil.put("error", errorMessage));
		}
	}

	private ClientExtensionEntry _add(ResourceRequest resourceRequest)
			throws PortalException {

		Map<Locale, String> nameMap = _localization.getLocalizationMap(
			resourceRequest, "name");

		String type = ParamUtil.getString(resourceRequest, "type");

		return _clientExtensionEntryService.addDraftClientExtensionEntry(
			StringPool.BLANK, nameMap, type);
	}

	private LiferayPortletURL _getRenderURL(ResourceRequest resourceRequest)
		throws WindowStateException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)resourceRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		LiferayPortletURL liferayPortletURL = PortletURLFactoryUtil.create(
			resourceRequest, portletDisplay.getId(), themeDisplay.getLayout(),
			PortletRequest.RENDER_PHASE);

		liferayPortletURL.setWindowState(LiferayWindowState.NORMAL);

		return liferayPortletURL;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AddClientExtensionEntryMVCResourceCommand.class);

	@Reference
	private CETFactory _cetFactory;

	@Reference
	private ClientExtensionEntryService _clientExtensionEntryService;

	@Reference
	private Localization _localization;

	@Reference
	private Portal _portal;

	@Reference
	private Language _language;

}
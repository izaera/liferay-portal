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
import com.liferay.client.extension.type.CET;
import com.liferay.client.extension.type.configuration.CETConfiguration;
import com.liferay.client.extension.type.factory.CETFactory;
import com.liferay.client.extension.web.internal.configuration.CETConfigurationImpl;
import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminWebKeys;
import com.liferay.client.extension.web.internal.display.context.EditClientExtensionEntryDisplayContext;
import com.liferay.client.extension.web.internal.display.context.EditClientExtensionEntryPartDisplayContext;
import com.liferay.client.extension.web.internal.model.ClientExtensionRepository;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Basto
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
		"mvc.command.name=/client_extension_admin/edit_client_extension_entry"
	},
	service = MVCActionCommand.class
)
public class EditClientExtensionEntryMVCActionCommand
	extends BaseMVCActionCommand {

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		try {
			UploadPortletRequest uploadPortletRequest =
				_portal.getUploadPortletRequest(actionRequest);

			File file = uploadPortletRequest.getFile("file");

			if (file != null) {
				String externalReferenceCode = uploadPortletRequest.getFileName(
					"file");

				CETConfiguration cetConfiguration = _addFileEntries(
					new ZipFile(file), externalReferenceCode);

				String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

				if (cmd.equals(Constants.ADD)) {
					_add(
						actionRequest, cetConfiguration, externalReferenceCode);
				}
				else if (cmd.equals(Constants.UPDATE)) {
					_update(actionRequest, cetConfiguration);
				}
			}
			else {
				String cmd = ParamUtil.getString(actionRequest, Constants.CMD);

				if (cmd.equals(Constants.ADD)) {
					_add(actionRequest);
				}
				else if (cmd.equals(Constants.UPDATE)) {
					_update(actionRequest);
				}
			}

			String redirect = ParamUtil.getString(actionRequest, "redirect");

			if (Validator.isNotNull(redirect)) {
				actionResponse.sendRedirect(redirect);
			}
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			SessionErrors.add(actionRequest, exception.getClass(), exception);

			CET cet = null;

			ClientExtensionEntry clientExtensionEntry =
				_fetchClientExtensionEntry(actionRequest);

			if (clientExtensionEntry != null) {
				cet = _cetFactory.create(clientExtensionEntry);
			}
			else {
				cet = _cetFactory.create(
					actionRequest, ParamUtil.getString(actionRequest, "type"));
			}

			actionRequest.setAttribute(
				ClientExtensionAdminWebKeys.
					EDIT_CLIENT_EXTENSION_ENTRY_DISPLAY_CONTEXT,
				new EditClientExtensionEntryDisplayContext(
					cet, clientExtensionEntry, actionRequest));
			actionRequest.setAttribute(
				ClientExtensionAdminWebKeys.
					EDIT_CLIENT_EXTENSION_ENTRY_PART_DISPLAY_CONTEXT,
				new EditClientExtensionEntryPartDisplayContext(
					cet, clientExtensionEntry, actionRequest));

			actionResponse.setRenderParameter(
				"mvcPath", "/admin/edit_client_extension_entry.jsp");
		}
	}

	private void _add(ActionRequest actionRequest) throws PortalException {
		String description = ParamUtil.getString(actionRequest, "description");
		Map<Locale, String> nameMap = LocalizationUtil.getLocalizationMap(
			actionRequest, "name");
		String sourceCodeURL = ParamUtil.getString(
			actionRequest, "sourceCodeURL");

		String type = ParamUtil.getString(actionRequest, "type");

		CET cet = _cetFactory.create(actionRequest, type);

		_clientExtensionEntryService.addClientExtensionEntry(
			StringPool.BLANK, description, nameMap,
			ParamUtil.getString(actionRequest, "properties"), sourceCodeURL,
			type, cet.getTypeSettings());
	}

	private void _add(
			ActionRequest actionRequest, CETConfiguration cetConfiguration,
			String externalReferenceCode)
		throws IOException, PortalException {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		CET cet = _cetFactory.create(
			cetConfiguration, themeDisplay.getCompanyId(),
			"UPLOAD:" + externalReferenceCode);

		_clientExtensionEntryService.addClientExtensionEntry(
			StringPool.BLANK, cet.getDescription(),
			Collections.singletonMap(
				LocaleUtil.getDefault(), cet.getName(LocaleUtil.getDefault())),
			_serialize(cet.getProperties()), cet.getSourceCodeURL(),
			cet.getType(), cet.getTypeSettings());
	}

	private CETConfiguration _addFileEntries(
			ZipFile zipFile, String externalReferenceCode)
		throws IOException, PortalException {

		CETConfigurationImpl cetConfigurationImpl = new CETConfigurationImpl();

		List<FileEntry> fileEntries = new ArrayList<>();

		Enumeration<? extends ZipEntry> entriesEnumeration = zipFile.entries();

		while (entriesEnumeration.hasMoreElements()) {
			ZipEntry zipEntry = entriesEnumeration.nextElement();

			if (zipEntry.isDirectory()) {
				continue;
			}

			String zipEntryName = zipEntry.getName();

			if (zipEntryName.endsWith(".client-extension-config.json")) {
				_processClientExtensionConfigJson(
					zipFile, zipEntry, cetConfigurationImpl);

				continue;
			}

			fileEntries.add(
				_clientExtensionRepository.createFileEntry(
					externalReferenceCode + StringPool.SLASH + zipEntryName,
					zipFile.getInputStream(zipEntry)));
		}

		zipFile.close();

		// TODO: select entry point CSS and JS URLs properly

		String cssURLs = StringUtil.merge(
			_clientExtensionRepository.getURLs(
				_getFileEntries(fileEntries, "styles.css")),
			StringPool.NEW_LINE);

		cetConfigurationImpl.addTypeSetting("cssURLs=" + cssURLs);

		String urls = StringUtil.merge(
			_clientExtensionRepository.getURLs(
				_getFileEntries(fileEntries, "index.js")),
			StringPool.NEW_LINE);

		cetConfigurationImpl.addTypeSetting("urls=" + urls);

		return cetConfigurationImpl;
	}

	private ClientExtensionEntry _fetchClientExtensionEntry(
			ActionRequest actionRequest)
		throws PortalException {

		String externalReferenceCode = ParamUtil.getString(
			actionRequest, "externalReferenceCode");

		if (Validator.isNull(externalReferenceCode)) {
			return null;
		}

		return _clientExtensionEntryService.
			fetchClientExtensionEntryByExternalReferenceCode(
				_portal.getCompanyId(actionRequest), externalReferenceCode);
	}

	private Collection<FileEntry> _getFileEntries(
		Collection<FileEntry> fileEntries, String fileName) {

		List<FileEntry> filteredFileEntries = new ArrayList<>();

		for (FileEntry fileEntry : fileEntries) {
			if (Objects.equals(fileEntry.getFileName(), fileName)) {
				filteredFileEntries.add(fileEntry);
			}
		}

		return filteredFileEntries;
	}

	private void _processClientExtensionConfigJson(
			ZipFile zipFile, ZipEntry zipEntry,
			CETConfigurationImpl cetConfigurationImpl)
		throws IOException, JSONException {

		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		StreamUtil.transfer(
			zipFile.getInputStream(zipEntry), byteArrayOutputStream);

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			byteArrayOutputStream.toString(StringPool.UTF8));

		Iterator<String> keysIterator = jsonObject.keys();

		String key = keysIterator.next();

		jsonObject = jsonObject.getJSONObject(key);

		cetConfigurationImpl.setBaseURL(StringPool.BLANK);
		cetConfigurationImpl.setDescription(
			jsonObject.getString("description"));
		cetConfigurationImpl.setName(jsonObject.getString("name"));
		cetConfigurationImpl.setSourceCodeURL(
			jsonObject.getString("sourceCodeURL"));
		cetConfigurationImpl.setType(jsonObject.getString("type"));

		JSONArray jsonArray = jsonObject.getJSONArray("typeSettings");

		for (int i = 0; i < jsonArray.length(); i++) {
			String string = jsonArray.getString(i);

			if (string.startsWith("urls=") || string.startsWith("cssURLs=")) {

				// TODO: select entry point CSS and JS URLs properly

				continue;
			}

			cetConfigurationImpl.addTypeSetting(string);
		}
	}

	private String _serialize(Properties properties) throws IOException {
		CharArrayWriter charArrayWriter = new CharArrayWriter();

		properties.store(charArrayWriter, StringPool.BLANK);

		String serializedProperties = charArrayWriter.toString();

		StringBuilder sb = new StringBuilder();

		String[] lines = serializedProperties.split(StringPool.NEW_LINE);

		for (String line : lines) {
			if (!line.startsWith(StringPool.POUND) &&
				!Validator.isBlank(line)) {

				sb.append(line);
			}
		}

		return sb.toString();
	}

	private void _update(ActionRequest actionRequest) throws PortalException {
		ClientExtensionEntry clientExtensionEntry = _fetchClientExtensionEntry(
			actionRequest);

		String description = ParamUtil.getString(actionRequest, "description");
		Map<Locale, String> nameMap = LocalizationUtil.getLocalizationMap(
			actionRequest, "name");
		String properties = ParamUtil.getString(actionRequest, "properties");
		String sourceCodeURL = ParamUtil.getString(
			actionRequest, "sourceCodeURL");

		CET cet = _cetFactory.create(
			actionRequest, clientExtensionEntry.getType());

		_clientExtensionEntryService.updateClientExtensionEntry(
			clientExtensionEntry.getClientExtensionEntryId(), description,
			nameMap, properties, sourceCodeURL, cet.getTypeSettings());
	}

	private void _update(
			ActionRequest actionRequest, CETConfiguration cetConfiguration)
		throws IOException, PortalException {

		ClientExtensionEntry clientExtensionEntry = _fetchClientExtensionEntry(
			actionRequest);

		CET cet = _cetFactory.create(
			cetConfiguration, clientExtensionEntry.getCompanyId(),
			clientExtensionEntry.getExternalReferenceCode());

		_clientExtensionEntryService.updateClientExtensionEntry(
			clientExtensionEntry.getClientExtensionEntryId(),
			cet.getDescription(),
			Collections.singletonMap(
				LocaleUtil.getDefault(), cet.getName(LocaleUtil.getDefault())),
			_serialize(cet.getProperties()), cet.getSourceCodeURL(),
			cet.getTypeSettings());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		EditClientExtensionEntryMVCActionCommand.class);

	@Reference
	private CETFactory _cetFactory;

	@Reference
	private ClientExtensionEntryService _clientExtensionEntryService;

	@Reference
	private ClientExtensionRepository _clientExtensionRepository;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Portal _portal;

}
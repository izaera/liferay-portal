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

package com.liferay.client.extension.web.internal.model;

import com.liferay.client.extension.web.internal.constants.ClientExtensionAdminPortletKeys;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepository;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = ClientExtensionRepository.class)
public class ClientExtensionRepository {

	public FileEntry createFileEntry(
			String fileEntryPath, InputStream inputStream)
		throws PortalException {

		int i = fileEntryPath.lastIndexOf(StringPool.SLASH);

		Folder folder = _createFolder(fileEntryPath.substring(0, i));

		long folderId = folder.getFolderId();

		String fileName = fileEntryPath.substring(i + 1);

		FileEntry fileEntry = _portletFileRepository.fetchPortletFileEntry(
			_repository.getGroupId(), folderId, fileName);

		if (fileEntry != null) {
			_portletFileRepository.deletePortletFileEntry(
				fileEntry.getFileEntryId());
		}

		return _portletFileRepository.addPortletFileEntry(
			/* TODO: ERC */ StringUtil.randomId(8), _repository.getGroupId(),
			_repository.getUserId(), ClientExtensionRepository.class.getName(),
			/* TODO: classPK */ 1, _repository.getPortletId(), folderId,
			inputStream, fileName,
			/* TODO: mime type */
			MimeTypesUtil.getExtensionContentType(
				FileUtil.getExtension(fileName)),
			false);
	}

	public FileEntry getFileEntry(String fileEntryPath) throws PortalException {
		if (fileEntryPath.startsWith(StringPool.SLASH)) {
			fileEntryPath = fileEntryPath.substring(1);
		}

		long folderId = _repository.getDlFolderId();

		Folder folder = _portletFileRepository.getPortletFolder(folderId);

		String[] parts = fileEntryPath.split(StringPool.SLASH);

		for (int i = 0; i < (parts.length - 1); i++) {
			folder = _portletFileRepository.getPortletFolder(
				_repository.getRepositoryId(), folder.getFolderId(), parts[i]);
		}

		return _portletFileRepository.getPortletFileEntry(
			_repository.getGroupId(), folder.getFolderId(),
			parts[parts.length - 1]);
	}

	public Collection<String> getURLs(Collection<FileEntry> fileEntries)
		throws PortalException {

		List<String> urls = new ArrayList<>();

		for (FileEntry fileEntry : fileEntries) {
			StringBuilder sb = new StringBuilder();

			sb.append(_BASE_URL);

			List<String> parts = new ArrayList<>();

			parts.add(fileEntry.getFileName());

			Folder folder = fileEntry.getFolder();

			while (true) {
				String name = folder.getName();

				if (name.equals(_repository.getPortletId())) {
					break;
				}

				parts.add(folder.getName());

				folder = folder.getParentFolder();
			}

			for (int i = parts.size() - 1; i >= 0; i--) {
				sb.append(StringPool.SLASH);
				sb.append(parts.get(i));
			}

			urls.add(sb.toString());
		}

		return urls;
	}

	@Activate
	protected void activate() throws PortalException {
		long companyId = _portal.getDefaultCompanyId();

		Company company = _companyLocalService.getCompany(companyId);

		long groupId = company.getGroupId();

		_repository = _portletFileRepository.fetchPortletRepository(
			groupId, ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN);

		if (_repository == null) {
			_repository = _portletFileRepository.addPortletRepository(
				groupId, ClientExtensionAdminPortletKeys.CLIENT_EXTENSION_ADMIN,
				_EMPTY_SERVICE_CONTEXT);
		}
	}

	private Folder _createFolder(String folderPath) throws PortalException {
		Folder folder = null;

		long folderId = _repository.getDlFolderId();

		String[] parts = folderPath.split(StringPool.SLASH);

		for (String part : parts) {
			folder = _ensureFolder(folderId, part);

			folderId = folder.getFolderId();
		}

		return folder;
	}

	private Folder _ensureFolder(long parentFolderId, String folderName)
		throws PortalException {

		try {
			return _portletFileRepository.getPortletFolder(
				_repository.getRepositoryId(), parentFolderId, folderName);
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(portalException);
			}

			return _portletFileRepository.addPortletFolder(
				_repository.getUserId(), _repository.getRepositoryId(),
				parentFolderId, folderName, _EMPTY_SERVICE_CONTEXT);
		}
	}

	private static final String _BASE_URL = "/o/cet-asset";

	private static final ServiceContext _EMPTY_SERVICE_CONTEXT =
		new ServiceContext();

	private static final Log _log = LogFactoryUtil.getLog(
		ClientExtensionRepository.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private PortletFileRepository _portletFileRepository;

	private Repository _repository;

}
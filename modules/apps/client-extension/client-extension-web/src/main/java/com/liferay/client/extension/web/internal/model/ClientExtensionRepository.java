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
import com.liferay.document.library.kernel.exception.NoSuchFileEntryException;
import com.liferay.document.library.kernel.exception.NoSuchFolderException;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
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
import com.liferay.portal.kernel.util.Portal;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portlet.documentlibrary.util.DLAppUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = ClientExtensionRepository.class)
public class ClientExtensionRepository {

	private static final String TMP_SUFFIX = ".tmp";

	public void deleteDraftFileEntries(long clientExtensionEntryId)
		throws PortalException {

		Folder draftFolder = _fetchClientExtensionEntryFolder(
			clientExtensionEntryId, Status.DRAFT);

		if (draftFolder == null) {
			return;
		}

		_dlAppLocalService.deleteFolder(draftFolder.getFolderId());
	}

	public void publishDraftFileEntries(long clientExtensionEntryId)
		throws PortalException {

		Folder draftFolder = _fetchClientExtensionEntryFolder(
			clientExtensionEntryId, Status.DRAFT);

		if (draftFolder == null) {
			return;
		}

		Folder publishedFolder = _ensureClientExtensionEntryFolder(
			clientExtensionEntryId, Status.PUBLISHED);

		// TODO: support folder paths

		for (FileEntry fileEntry :
				getFileEntries(clientExtensionEntryId, Status.DRAFT)) {

			FileEntry publishedFileEntry =
				_portletFileRepository.fetchPortletFileEntry(
					_repository.getGroupId(), publishedFolder.getFolderId(),
					fileEntry.getFileName());

			if (publishedFileEntry != null) {
				_portletFileRepository.deletePortletFileEntry(
					publishedFileEntry.getFileEntryId());
			}

			_dlAppLocalService.moveFileEntry(
				_repository.getUserId(), fileEntry.getFileEntryId(),
				publishedFolder.getFolderId(), _EMPTY_SERVICE_CONTEXT);
		}

		_dlAppLocalService.deleteFolder(draftFolder.getFolderId());
	}

	public enum Status {
		ALL, DRAFT, PUBLISHED,
	}

	public FileEntry createFileEntry(
			long clientExtensionEntryId, String fileName,
			InputStream inputStream, String mimeType, Status status)
		throws PortalException {

		Folder folder = _ensureClientExtensionEntryFolder(
			clientExtensionEntryId, status);

		FileEntry fileEntry = _portletFileRepository.fetchPortletFileEntry(
			_repository.getGroupId(), folder.getFolderId(), fileName);

		if (fileEntry != null) {
			_portletFileRepository.deletePortletFileEntry(
				fileEntry.getFileEntryId());
		}

		return _portletFileRepository.addPortletFileEntry(
			StringPool.BLANK, _repository.getGroupId(), _repository.getUserId(),
			ClientExtensionRepository.class.getName(), 0,
			_repository.getPortletId(), folder.getFolderId(), inputStream,
			fileName, mimeType, false);
	}

	public List<FileEntry> getFileEntries(
			long clientExtensionEntryId, Status status)
		throws PortalException {

		if (status == Status.ALL) {
			Map<String, FileEntry> fileEntryMap = new HashMap<>();

			for (FileEntry fileEntry :
				getFileEntries(clientExtensionEntryId, Status.DRAFT)) {

				fileEntryMap.put(fileEntry.getFileName(), fileEntry);
			}

			for (FileEntry fileEntry :
				getFileEntries(clientExtensionEntryId, Status.PUBLISHED)) {

				fileEntryMap.put(fileEntry.getFileName(), fileEntry);
			}

			return new ArrayList<>(fileEntryMap.values());
		}
		else {
			Folder folder = _fetchClientExtensionEntryFolder(
				clientExtensionEntryId, status);

			if (folder == null) {
				return Collections.emptyList();
			}

			return _portletFileRepository.getPortletFileEntries(
				_repository.getGroupId(), folder.getFolderId());
		}
	}

	public int getFileEntriesCount(long clientExtensionEntryId, Status status)
		throws PortalException {

		if (status == Status.ALL) {
			List<FileEntry> fileEntries =
				getFileEntries(clientExtensionEntryId, Status.ALL);

			return fileEntries.size();
		} else {
			Folder folder = _fetchClientExtensionEntryFolder(
				clientExtensionEntryId, status);

			if (folder == null) {
				return 0;
			}

			return _portletFileRepository.getPortletFileEntriesCount(
				_repository.getGroupId(), folder.getFolderId());
		}
	}

	public FileEntry getFileEntry(long fileEntryId) throws PortalException {
		return _portletFileRepository.getPortletFileEntry(fileEntryId);
	}

	public FileEntry getFileEntry(
			long clientExtensionEntryId, String filePath, Status status)
		throws PortalException {

		Folder folder = _fetchClientExtensionEntryFolder(
			clientExtensionEntryId, status);

		if (folder == null) {
			throw new NoSuchFolderException();
		}

		if (filePath.startsWith(StringPool.SLASH)) {
			filePath = filePath.substring(1);
		}

		DLFolder dlFolder = _dlFolderLocalService.getDLFolder(
			folder.getFolderId());

		String[] parts = filePath.split(StringPool.SLASH);

		for (int i = 0; i<parts.length-1; i++) {
			dlFolder = _dlFolderLocalService.getFolder(
				_repository.getGroupId(), dlFolder.getFolderId(), parts[i]);
		}

		return _portletFileRepository.getPortletFileEntry(
			_repository.getGroupId(), dlFolder.getFolderId(),
			parts[parts.length-1]);
	}

	public String getURL(FileEntry fileEntry) throws PortalException {
		StringBuilder sb = new StringBuilder();

		sb.append(_BASE_URL);

		List<String> parts = new ArrayList<>();

		parts.add(fileEntry.getFileName());

		Folder folder = fileEntry.getFolder();

		while (true) {
			String name = folder.getName();

			if (name.equals(_repository.getPortletId())) {
				// Remove the client extension entry folder part
				parts.remove(parts.size() - 1);

				break;
			}

			parts.add(folder.getName());

			folder = folder.getParentFolder();
		}

		parts.add(String.valueOf(fileEntry.getFileEntryId()));

		for (int i = parts.size() - 1; i >= 0; i--) {
			sb.append(StringPool.SLASH);
			sb.append(parts.get(i));
		}

		return sb.toString();
	}

	/**************************************************************************/

	public List<FileEntry> _getFileEntries(
			long folderId)
		throws PortalException {

		if (folderId == 0) {
			folderId = _repository.getDlFolderId();
		}

		Folder folder = _portletFileRepository.getPortletFolder(folderId);

		return _portletFileRepository.getPortletFileEntries(
			_repository.getGroupId(), folder.getFolderId());
	}

	public List<DLFolder> _getFolders(long folderId) {
		if (folderId == 0) {
			folderId = _repository.getDlFolderId();
		}

		List<DLFolder> folders =
			_dlFolderLocalService.getFolders(_repository.getGroupId(),
				folderId);

		return folders;
	}

	public Folder _getFolder(long folderId) throws PortalException {
		if (folderId == 0) {
			folderId = _repository.getDlFolderId();
		}

		Folder folder = _portletFileRepository.getPortletFolder(folderId);

		return folder;
	}

	/**************************************************************************/

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

	private Folder _ensureClientExtensionEntryFolder(
			long clientExtensionEntryId, Status status)
		throws PortalException {

		long folderId = _repository.getDlFolderId();

		Folder folder = _portletFileRepository.getPortletFolder(folderId);

		String folderName = _getFolderName(clientExtensionEntryId, status);

		try {
			return _portletFileRepository.getPortletFolder(
				_repository.getRepositoryId(), folder.getFolderId(),
				folderName);
		}
		catch (NoSuchFolderException noSuchFolderException) {
			return _portletFileRepository.addPortletFolder(
				_repository.getUserId(), _repository.getRepositoryId(),
					folder.getFolderId(), folderName, _EMPTY_SERVICE_CONTEXT);
		}
	}

	private Folder _fetchClientExtensionEntryFolder(
			long clientExtensionEntryId, Status status)
		throws PortalException {

		long folderId = _repository.getDlFolderId();

		Folder folder = _portletFileRepository.getPortletFolder(folderId);

		String folderName = _getFolderName(clientExtensionEntryId, status);

		try {
			return _portletFileRepository.getPortletFolder(
				_repository.getRepositoryId(), folder.getFolderId(),
				folderName);
		}
		catch (NoSuchFolderException noSuchFolderException) {
			return null;
		}
	}

	private String _getFolderName(long clientExtensionEntryId, Status status) {
		if (status == Status.DRAFT) {
			return clientExtensionEntryId + TMP_SUFFIX;
		}
		else if (status == Status.PUBLISHED) {
			return String.valueOf(clientExtensionEntryId);
		}

		throw new IllegalArgumentException(status.name());
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

	@Reference
	private DLFolderLocalService _dlFolderLocalService;

	private Repository _repository;

	@Reference
	private DLAppLocalService _dlAppLocalService;

}
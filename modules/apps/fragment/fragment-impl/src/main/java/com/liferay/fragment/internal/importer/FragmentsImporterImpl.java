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

package com.liferay.fragment.internal.importer;

import com.liferay.fragment.constants.FragmentEntryTypeConstants;
import com.liferay.fragment.constants.FragmentPortletKeys;
import com.liferay.fragment.exception.DuplicateFragmentCollectionKeyException;
import com.liferay.fragment.exception.DuplicateFragmentEntryKeyException;
import com.liferay.fragment.exception.FragmentCollectionNameException;
import com.liferay.fragment.importer.FragmentCollectionImport;
import com.liferay.fragment.importer.FragmentCollectionResourceImport;
import com.liferay.fragment.importer.FragmentEntryImport;
import com.liferay.fragment.importer.FragmentImportAdapter;
import com.liferay.fragment.importer.FragmentsImporter;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentCollectionModel;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.processor.FragmentEntryProcessorRegistry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentCollectionService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.fragment.service.FragmentEntryService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eudaldo Alonso
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = FragmentsImporter.class)
public class FragmentsImporterImpl implements FragmentsImporter {

	@Override
	public List<String> importCollection(
			long groupId, long userId, long fragmentCollectionId,
			FragmentImportAdapter adapter, boolean overwrite)
		throws Exception {

		_invalidEntries = new ArrayList<>();

		List<FragmentCollectionImport> folders = adapter.getCollections();

		List<FragmentCollection> collections = _importFragmentCollections(
			groupId, folders, overwrite);

		List<FragmentCollectionResourceImport> resources =
			adapter.getResources();

		_importResources(
			groupId, userId, fragmentCollectionId, resources, collections);

		List<FragmentEntryImport> entries = adapter.getFragmentEntries();

		_importFragmentEntries(
			groupId, userId, fragmentCollectionId, collections, entries,
			overwrite);

		return _invalidEntries;
	}

	private FragmentCollection _addFragmentCollection(
			long groupId, FragmentCollectionImport folder, boolean overwrite)
		throws PortalException {

		String key = folder.getKey();
		String name = folder.getName();
		String description = folder.getDescription();

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.fetchFragmentCollection(
				groupId, key);

		if (fragmentCollection == null) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			fragmentCollection =
				_fragmentCollectionService.addFragmentCollection(
					groupId, key, name, description, serviceContext);
		}
		else if (overwrite) {
			fragmentCollection =
				_fragmentCollectionService.updateFragmentCollection(
					fragmentCollection.getFragmentCollectionId(), name,
					description);
		}
		else {
			throw new DuplicateFragmentCollectionKeyException(key);
		}

		return fragmentCollection;
	}

	private FragmentEntry _addFragmentEntry(
			long groupId, long fragmentCollectionId, FragmentEntryImport entry,
			boolean overwrite)
		throws PortalException {

		String key = entry.getKey();

		FragmentEntry fragmentEntry =
			_fragmentEntryLocalService.fetchFragmentEntry(groupId, key);

		if ((fragmentEntry != null) && !overwrite) {
			throw new DuplicateFragmentEntryKeyException(key);
		}

		String html = entry.getHtml();
		String name = entry.getName();

		int status = _getStatusForFragmentHtml(name, html);

		String css = entry.getCss();
		String js = entry.getJs();
		int type = _getEntryType(entry);

		if (fragmentEntry == null) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			return _fragmentEntryService.addFragmentEntry(
				groupId, fragmentCollectionId, key, name, css, html, js, type,
				status, serviceContext);
		}

		return _fragmentEntryService.updateFragmentEntry(
			fragmentEntry.getFragmentEntryId(), name, css, html, js, status);
	}

	private void _addResourceToCollection(
			long groupId, long userId, long collectionId, long folderId,
			FragmentCollectionResourceImport resource)
		throws PortalException {

		String fileName = resource.getFileName();

		FileEntry fileEntry = PortletFileRepositoryUtil.fetchPortletFileEntry(
			groupId, folderId, fileName);

		if (fileEntry != null) {
			PortletFileRepositoryUtil.deletePortletFileEntry(
				fileEntry.getFileEntryId());
		}

		PortletFileRepositoryUtil.addPortletFileEntry(
			groupId, userId, FragmentCollection.class.getName(), collectionId,
			FragmentPortletKeys.FRAGMENT, folderId, resource.getBytes(),
			fileName, resource.getContentType(), false);
	}

	private Long _getCollectionId(
			long groupId, String key, long fragmentCollectionId,
			List<FragmentCollection> collections)
		throws PortalException {

		long defaultCollectionId = fragmentCollectionId;

		if ((fragmentCollectionId <= 0) && Validator.isNull(key)) {
			defaultCollectionId = _getDefaultFragmentCollectionId(groupId);
		}

		if (Validator.isNull(key)) {
			return defaultCollectionId;
		}

		Stream<FragmentCollection> stream = collections.stream();

		return stream.filter(
			c -> Objects.equals(key, c.getFragmentCollectionKey())
		).findFirst(
		).map(
			FragmentCollectionModel::getFragmentCollectionId
		).orElse(
			defaultCollectionId
		);
	}

	private long _getDefaultFragmentCollectionId(long groupId)
		throws PortalException {

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.fetchFragmentCollection(
				groupId, _DEFAULT_FRAGMENT_COLLECTION_KEY);

		if (fragmentCollection == null) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			String name = LanguageUtil.get(
				serviceContext.getRequest(), _DEFAULT_FRAGMENT_COLLECTION_KEY);

			fragmentCollection =
				_fragmentCollectionService.addFragmentCollection(
					groupId, _DEFAULT_FRAGMENT_COLLECTION_KEY, name,
					StringPool.BLANK, serviceContext);
		}

		return fragmentCollection.getFragmentCollectionId();
	}

	private int _getEntryType(FragmentEntryImport entry) {
		return FragmentEntryTypeConstants.getTypeFromLabel(
			StringUtil.toLowerCase(StringUtil.trim(entry.getTypeLabel())));
	}

	private int _getStatusForFragmentHtml(String name, String html) {
		try {
			_fragmentEntryProcessorRegistry.validateFragmentEntryHTML(html);

			return WorkflowConstants.STATUS_APPROVED;
		}
		catch (PortalException pe) {
			_log.error(pe, pe);

			_invalidEntries.add(name);

			return WorkflowConstants.STATUS_DRAFT;
		}
	}

	private List<FragmentCollection> _importFragmentCollections(
			long groupId, List<FragmentCollectionImport> folders,
			boolean overwrite)
		throws Exception {

		List<FragmentCollection> collections = new ArrayList<>();

		for (FragmentCollectionImport folder : folders) {
			if (Validator.isNull(folder.getName())) {
				throw new FragmentCollectionNameException();
			}

			FragmentCollection fragmentCollection = _addFragmentCollection(
				groupId, folder, overwrite);

			collections.add(fragmentCollection);
		}

		return collections;
	}

	private void _importFragmentEntries(
			long groupId, long userId, long fragmentCollectionId,
			List<FragmentCollection> collections,
			List<FragmentEntryImport> entries, boolean overwrite)
		throws PortalException {

		for (FragmentEntryImport entry : entries) {
			String key = entry.getCollectionKey();

			Long collectionId = _getCollectionId(
				groupId, key, fragmentCollectionId, collections);

			FragmentEntry fragmentEntry = _addFragmentEntry(
				groupId, collectionId, entry, overwrite);

			if (Validator.isNotNull(entry.getThumbnail())) {
				long fileEntryId = _importFragmentEntryThumbnail(
					groupId, userId, entry, fragmentEntry);

				_fragmentEntryLocalService.updateFragmentEntry(
					fragmentEntry.getFragmentEntryId(), fileEntryId);
			}
		}
	}

	private long _importFragmentEntryThumbnail(
			long groupId, long userId, FragmentEntryImport entry,
			FragmentEntry fragmentEntry)
		throws PortalException {

		long fragmentEntryId = fragmentEntry.getFragmentEntryId();

		Repository repository =
			PortletFileRepositoryUtil.fetchPortletRepository(
				groupId, FragmentPortletKeys.FRAGMENT);

		if (repository == null) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			repository = PortletFileRepositoryUtil.addPortletRepository(
				groupId, FragmentPortletKeys.FRAGMENT, serviceContext);
		}

		String fileName =
			fragmentEntryId + "_preview." + entry.getThumbnailExtension();

		long folderId = repository.getDlFolderId();

		FileEntry fileEntry = PortletFileRepositoryUtil.fetchPortletFileEntry(
			groupId, folderId, fileName);

		if (fileEntry != null) {
			PortletFileRepositoryUtil.deletePortletFileEntry(
				groupId, folderId, fileName);
		}

		fileEntry = PortletFileRepositoryUtil.addPortletFileEntry(
			groupId, userId, FragmentEntry.class.getName(), fragmentEntryId,
			FragmentPortletKeys.FRAGMENT, folderId, entry.getThumbnail(),
			fileName, entry.getThumbnailContentType(), false);

		return fileEntry.getFileEntryId();
	}

	private void _importResources(
			long groupId, long userId, long fragmentCollectionId,
			List<FragmentCollectionResourceImport> resources,
			List<FragmentCollection> collections)
		throws PortalException {

		if ((resources == null) || resources.isEmpty()) {
			return;
		}

		FragmentCollection fragmentCollection =
			_fragmentCollectionLocalService.fetchFragmentCollection(
				fragmentCollectionId);

		for (FragmentCollectionResourceImport resource : resources) {
			for (FragmentCollection collection : collections) {
				long folderId = collection.getResourcesFolderId();
				long collectionId = collection.getFragmentCollectionId();

				_addResourceToCollection(
					groupId, userId, collectionId, folderId, resource);
			}

			if (fragmentCollection != null) {
				long folderId = fragmentCollection.getResourcesFolderId();

				_addResourceToCollection(
					groupId, userId, fragmentCollectionId, folderId, resource);
			}
		}
	}

	private static final String _DEFAULT_FRAGMENT_COLLECTION_KEY = "imported";

	private static final Log _log = LogFactoryUtil.getLog(
		FragmentsImporterImpl.class);

	@Reference
	private FragmentCollectionLocalService _fragmentCollectionLocalService;

	@Reference
	private FragmentCollectionService _fragmentCollectionService;

	@Reference
	private FragmentEntryLocalService _fragmentEntryLocalService;

	@Reference
	private FragmentEntryProcessorRegistry _fragmentEntryProcessorRegistry;

	@Reference
	private FragmentEntryService _fragmentEntryService;

	private ArrayList<String> _invalidEntries;

}
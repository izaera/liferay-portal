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

package com.liferay.fragment.importer;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Eudaldo Alonso
 * @author Rodolfo Roza Miranda
 */
public class FragmentZipFile extends BaseFragmentImportAdapter {

	public FragmentZipFile(ZipFile zipFile) {
		_zipFile = zipFile;
	}

	@Override
	public List<FragmentCollectionImport> getCollections() throws Exception {
		List<FragmentCollectionImport> folders = new ArrayList<>();

		Enumeration<? extends ZipEntry> enumeration = _zipFile.entries();

		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();

			String filePath = zipEntry.getName();

			if (zipEntry.isDirectory() ||
				!isFragmentCollection(_getFileName(filePath))) {

				continue;
			}

			String key = _getKey(filePath);

			String name = key;

			String description = StringPool.BLANK;

			String collectionJSON = _getContent(filePath);

			if (Validator.isNotNull(collectionJSON)) {
				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
					collectionJSON);

				name = jsonObject.getString("name");
				description = jsonObject.getString("description");
			}

			folders.add(new FragmentCollectionImport(key, name, description));
		}

		return folders;
	}

	@Override
	public List<FragmentEntryImport> getFragmentEntries() throws Exception {
		return _getFragmentEntries(getCollections());
	}

	@Override
	public List<FragmentCollectionResourceImport> getResources()
		throws Exception {

		Enumeration<? extends ZipEntry> enumeration = _zipFile.entries();

		List<FragmentCollectionResourceImport> resources = new ArrayList<>();

		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();

			String path = zipEntry.getName();

			String[] paths = StringUtil.split(path, StringPool.FORWARD_SLASH);

			if (!ArrayUtil.contains(paths, "resources")) {
				continue;
			}

			String fileName = _getFileName(path);

			byte[] bytes;

			try (InputStream stream = _getInputStream(path)) {
				bytes = FileUtil.getBytes(stream);
			}

			String contentType = MimeTypesUtil.getContentType(fileName);

			resources.add(
				new FragmentCollectionResourceImport(
					fileName, bytes, contentType));
		}

		return resources;
	}

	private FragmentEntryImport _createEntry(
			String fileName, String collectionKey)
		throws Exception {

		String fragmentJSON = _getContent(fileName);

		String key = _getKey(fileName);
		String name = _getKey(fileName);
		String css = StringPool.BLANK;
		String html = StringPool.BLANK;
		String js = StringPool.BLANK;
		String typeLabel = StringPool.BLANK;

		String thumbnailExtension = StringPool.BLANK;
		String thumbnailContentType = StringPool.BLANK;

		byte[] thumbnail = null;

		if (Validator.isNotNull(fragmentJSON)) {
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				fragmentJSON);

			name = jsonObject.getString("name");
			css = _getFragmentEntryContent(
				fileName, jsonObject.getString("cssPath"));
			html = _getFragmentEntryContent(
				fileName, jsonObject.getString("htmlPath"));
			js = _getFragmentEntryContent(
				fileName, jsonObject.getString("jsPath"));
			typeLabel = jsonObject.getString("type");

			String thumbnailPath = jsonObject.getString("thumbnailPath");

			if (Validator.isNotNull(thumbnailPath)) {
				try (InputStream stream = _getFragmentEntryInputStream(
						fileName, thumbnailPath)) {

					thumbnail = FileUtil.getBytes(stream);
				}

				thumbnailExtension = FileUtil.getExtension(thumbnailPath);
				thumbnailContentType = MimeTypesUtil.getContentType(
					thumbnailPath);
			}
		}

		FragmentEntryImport entry = new FragmentEntryImport(
			key, collectionKey, name, css, html, js, typeLabel);

		entry.setThumbnail(thumbnail);
		entry.setThumbnailExtension(thumbnailExtension);
		entry.setThumbnailContentType(thumbnailContentType);

		return entry;
	}

	private String _getContent(String fileName) throws Exception {
		InputStream inputStream = _getInputStream(fileName);

		return StringUtil.read(inputStream);
	}

	private String _getFileName(String path) {
		int pos = path.lastIndexOf(CharPool.SLASH);

		if (pos > 0) {
			return path.substring(pos + 1);
		}

		return StringPool.BLANK;
	}

	private List<FragmentEntryImport> _getFragmentEntries(
			List<FragmentCollectionImport> folders)
		throws Exception {

		List<FragmentEntryImport> entries = new ArrayList<>();

		Enumeration<? extends ZipEntry> enumeration = _zipFile.entries();

		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();

			String path = zipEntry.getName();

			if (zipEntry.isDirectory() ||
				!isFragmentEntry(_getFileName(path))) {

				continue;
			}

			String[] paths = path.split(StringPool.SLASH);

			String collectionKey = null;

			for (FragmentCollectionImport folder : folders) {
				String key = folder.getKey();

				if (ArrayUtil.contains(paths, key)) {
					collectionKey = key;

					break;
				}
			}

			entries.add(_createEntry(path, collectionKey));
		}

		return entries;
	}

	private String _getFragmentEntryContent(String fileName, String contentPath)
		throws Exception {

		InputStream inputStream = _getFragmentEntryInputStream(
			fileName, contentPath);

		if (inputStream == null) {
			return StringPool.BLANK;
		}

		return StringUtil.read(inputStream);
	}

	private InputStream _getFragmentEntryInputStream(
			String fileName, String contentPath)
		throws Exception {

		if (contentPath.startsWith(StringPool.SLASH)) {
			return _getInputStream(contentPath.substring(1));
		}

		if (contentPath.startsWith("./")) {
			contentPath = contentPath.substring(2);
		}

		String path = fileName.substring(
			0, fileName.lastIndexOf(StringPool.SLASH));

		return _getInputStream(path + StringPool.SLASH + contentPath);
	}

	private InputStream _getInputStream(String fileName) throws Exception {
		ZipEntry zipEntry = _zipFile.getEntry(fileName);

		if (zipEntry == null) {
			return null;
		}

		return _zipFile.getInputStream(zipEntry);
	}

	private String _getKey(String fileName) {
		String path = fileName.substring(
			0, fileName.lastIndexOf(CharPool.SLASH));

		return path.substring(path.lastIndexOf(CharPool.SLASH) + 1);
	}

	private final ZipFile _zipFile;

}
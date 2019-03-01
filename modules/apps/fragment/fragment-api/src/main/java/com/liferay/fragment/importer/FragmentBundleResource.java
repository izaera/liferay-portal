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

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.MimeTypesUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.osgi.framework.Bundle;

/**
 * @author Rodolfo Roza Miranda
 */
public class FragmentBundleResource extends BaseFragmentImportAdapter {

	public FragmentBundleResource(Bundle bundle, String path) {
		this(bundle, path, _DEFAULT_RESOURCES_DIRECTORY);
	}

	public FragmentBundleResource(
		Bundle bundle, String path, String resourcesDirectory) {

		_bundle = bundle;
		_path = path;
		_resourcesDirectory = resourcesDirectory;
	}

	@Override
	public List<FragmentCollectionImport> getCollections() {
		return Collections.emptyList();
	}

	@Override
	public List<FragmentEntryImport> getFragmentEntries() throws Exception {
		String path = _path + "/fragments";

		Enumeration<URL> urls = _bundle.findEntries(path, "*.html", true);

		List<FragmentEntryImport> entries = new ArrayList<>();

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();

			String fileName = FileUtil.getShortFileName(url.getPath());
			String filePath = FileUtil.getPath(url.getPath());

			url = _bundle.getEntry(filePath + "/" + fileName);

			String shortFileName = FileUtil.getShortFileName(url.getPath());

			String fragmentEntryId = FileUtil.stripExtension(shortFileName);

			String fragmentEntryName = StringUtil.upperCaseFirstLetter(
				fragmentEntryId);

			String css = _getCss(filePath, fragmentEntryId);

			String html = StringUtil.read(url.openStream());

			String js = _getJs(filePath, fragmentEntryId);

			FragmentEntryImport entry = new FragmentEntryImport(
				fragmentEntryId, null, fragmentEntryName, css, html, js);

			_setEntryThumbnail(entry, filePath);

			entries.add(entry);
		}

		return entries;
	}

	@Override
	public List<FragmentCollectionResourceImport> getResources()
		throws Exception {

		List<FragmentCollectionResourceImport> resources = new ArrayList<>();

		Enumeration<URL> urls = _bundle.findEntries(
			_getResourcesPath(), StringPool.STAR, false);

		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();

			String fileName = FileUtil.getShortFileName(url.getPath());

			String contentType = MimeTypesUtil.getContentType(fileName);

			byte[] bytes;

			try (InputStream is = url.openStream()) {
				bytes = FileUtil.getBytes(is);
			}

			resources.add(
				new FragmentCollectionResourceImport(
					fileName, bytes, contentType));
		}

		return resources;
	}

	private String _getCss(String filePath, String fragmentEntryId)
		throws IOException {

		return _getEntryFileContent(filePath, fragmentEntryId, ".css");
	}

	private String _getEntryFileContent(
			String filePath, String fragmentEntryId, String extension)
		throws IOException {

		StringBundler sb = new StringBundler(4);

		sb.append(filePath);
		sb.append(StringPool.SLASH);
		sb.append(fragmentEntryId);
		sb.append(extension);

		URL url = _bundle.getEntry(sb.toString());

		if (url == null) {
			return StringPool.BLANK;
		}

		return StringUtil.read(url.openStream());
	}

	private String _getJs(String filePath, String fragmentEntryId)
		throws IOException {

		return _getEntryFileContent(filePath, fragmentEntryId, ".js");
	}

	private String _getResourcesPath() {
		return _path + StringPool.FORWARD_SLASH + _resourcesDirectory;
	}

	private void _setEntryThumbnail(FragmentEntryImport entry, String filePath)
		throws IOException {

		StringBundler sb = new StringBundler(4);

		sb.append(filePath);
		sb.append(StringPool.SLASH);
		sb.append(entry.getKey());
		sb.append(".jpg");

		String thumbnailFileName = sb.toString();

		URL thumbnailURL = _bundle.getEntry(thumbnailFileName);

		if (thumbnailURL == null) {
			return;
		}

		byte[] bytes;

		try (InputStream is = thumbnailURL.openStream()) {
			bytes = FileUtil.getBytes(is);
		}

		String extension = FileUtil.getExtension(thumbnailFileName);
		String contentType = MimeTypesUtil.getContentType(thumbnailFileName);

		entry.setThumbnail(bytes);
		entry.setThumbnailExtension(extension);
		entry.setThumbnailContentType(contentType);
	}

	private static final String _DEFAULT_RESOURCES_DIRECTORY = "images";

	private final Bundle _bundle;
	private final String _path;
	private final String _resourcesDirectory;

}
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

import com.liferay.fragment.constants.FragmentEntryTypeConstants;

/**
 * @author Rodolfo Roza Miranda
 */
public class FragmentEntryImport {

	public FragmentEntryImport(
		String key, String collectionKey, String name, String css, String html,
		String js) {

		this(
			key, collectionKey, name, css, html, js,
			FragmentEntryTypeConstants.TYPE_SECTION_LABEL);
	}

	public FragmentEntryImport(
		String key, String collectionKey, String name, String css, String html,
		String js, String typeLabel) {

		_key = key;
		_collectionKey = collectionKey;
		_name = name;
		_css = css;
		_html = html;
		_js = js;
		_typeLabel = typeLabel;
	}

	public String getCollectionKey() {
		return _collectionKey;
	}

	public String getCss() {
		return _css;
	}

	public String getHtml() {
		return _html;
	}

	public String getJs() {
		return _js;
	}

	public String getKey() {
		return _key;
	}

	public String getName() {
		return _name;
	}

	public byte[] getThumbnail() {
		return _thumbnail;
	}

	public String getThumbnailContentType() {
		return _thumbnailContentType;
	}

	public String getThumbnailExtension() {
		return _thumbnailExtension;
	}

	public String getTypeLabel() {
		return _typeLabel;
	}

	public void setThumbnail(byte[] thumbnail) {
		_thumbnail = thumbnail;
	}

	public void setThumbnailContentType(String thumbnailContentType) {
		_thumbnailContentType = thumbnailContentType;
	}

	public void setThumbnailExtension(String thumbnailExtension) {
		_thumbnailExtension = thumbnailExtension;
	}

	private final String _collectionKey;
	private final String _css;
	private final String _html;
	private final String _js;
	private final String _key;
	private final String _name;
	private byte[] _thumbnail;
	private String _thumbnailContentType;
	private String _thumbnailExtension;
	private final String _typeLabel;

}
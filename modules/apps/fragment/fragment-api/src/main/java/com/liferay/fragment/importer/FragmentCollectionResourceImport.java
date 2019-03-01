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

/**
 * @author Rodolfo Roza Miranda
 */
public class FragmentCollectionResourceImport {

	public FragmentCollectionResourceImport(
		String fileName, byte[] bytes, String contentType) {

		_fileName = fileName;
		_bytes = bytes;
		_contentType = contentType;
	}

	public byte[] getBytes() {
		return _bytes;
	}

	public String getContentType() {
		return _contentType;
	}

	public String getFileName() {
		return _fileName;
	}

	private final byte[] _bytes;
	private final String _contentType;
	private final String _fileName;

}
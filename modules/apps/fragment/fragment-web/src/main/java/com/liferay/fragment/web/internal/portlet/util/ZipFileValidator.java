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

package com.liferay.fragment.web.internal.portlet.util;

import com.liferay.fragment.constants.FragmentExportImportConstants;
import com.liferay.fragment.exception.InvalidFileException;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Eudaldo Alonso
 * @author Rodolfo Roza Miranda
 */
public class ZipFileValidator {

	public static void validateZipFile(ZipFile zipFile) throws PortalException {
		Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();

			if (_isFragmentCollection(zipEntry.getName()) ||
				_isFragmentEntry(zipEntry.getName())) {

				return;
			}
		}

		throw new InvalidFileException();
	}

	private static String _getFileName(String path) {
		int pos = path.lastIndexOf(CharPool.SLASH);

		if (pos > 0) {
			return path.substring(pos + 1);
		}

		return StringPool.BLANK;
	}

	private static boolean _isFragmentCollection(String fileName) {
		if (Objects.equals(
				_getFileName(fileName),
				FragmentExportImportConstants.FILE_NAME_COLLECTION_CONFIG)) {

			return true;
		}

		return false;
	}

	private static boolean _isFragmentEntry(String fileName) {
		if (Objects.equals(
				_getFileName(fileName),
				FragmentExportImportConstants.FILE_NAME_FRAGMENT_CONFIG)) {

			return true;
		}

		return false;
	}

}
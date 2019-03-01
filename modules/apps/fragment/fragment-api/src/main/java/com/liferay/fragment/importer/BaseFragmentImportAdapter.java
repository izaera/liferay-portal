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

import com.liferay.fragment.constants.FragmentExportImportConstants;

/**
 * @author Rodolfo Roza Miranda
 */
public abstract class BaseFragmentImportAdapter
	implements FragmentImportAdapter {

	protected boolean isFragmentCollection(String fileName) {
		return FragmentExportImportConstants.FILE_NAME_COLLECTION_CONFIG.equals(
			fileName);
	}

	protected boolean isFragmentEntry(String fileName) {
		return FragmentExportImportConstants.FILE_NAME_FRAGMENT_CONFIG.equals(
			fileName);
	}

}
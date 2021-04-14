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

package com.liferay.remote.app.admin.web.internal.frontend.taglib.clay.data.set.provider;

import com.liferay.dataset.data.DatasetDataFilter;
import com.liferay.dataset.data.DatasetDataPagination;
import com.liferay.dataset.data.DatasetDataProvider;
import com.liferay.frontend.taglib.clay.data.set.provider.ClayDataSetDataProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.remote.app.admin.web.internal.constants.RemoteAppAdminConstants;
import com.liferay.remote.app.admin.web.internal.frontend.taglib.clay.data.set.RemoteAppDatasetEntry;
import com.liferay.remote.app.model.RemoteAppEntry;
import com.liferay.remote.app.service.RemoteAppEntryLocalService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Bruno Basto
 */
@Component(
	immediate = true,
	property = "dataset.data.provider.key=" + RemoteAppAdminConstants.REMOTE_APP_ENTRY_DATA_SET_DISPLAY,
	service = DatasetDataProvider.class
)
public class RemoteAppEntryClayDataSetDataProvider
	implements DatasetDataProvider<RemoteAppDatasetEntry> {

	@Override
	public List<RemoteAppDatasetEntry> getItems(
			HttpServletRequest httpServletRequest,
			DatasetDataFilter datasetDataFilter,
			DatasetDataPagination datasetDataPagination, Sort sort)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		List<RemoteAppEntry> remoteAppEntries =
			_remoteAppEntryLocalService.searchRemoteAppEntries(
				themeDisplay.getCompanyId(), datasetDataFilter.getKeywords(),
				datasetDataPagination.getStartPosition(),
				datasetDataPagination.getEndPosition(),
				sort);

		Stream<RemoteAppEntry> stream = remoteAppEntries.stream();

		return stream.map(
			remoteAppEntry -> new RemoteAppDatasetEntry(
				remoteAppEntry, themeDisplay.getLocale())
		).collect(
			Collectors.toList()
		);
	}

	@Override
	public int getItemsCount(
			HttpServletRequest httpServletRequest,
			DatasetDataFilter datasetDataFilter)
		throws PortalException {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return _remoteAppEntryLocalService.searchRemoteAppEntriesCount(
			themeDisplay.getCompanyId(), datasetDataFilter.getKeywords());
	}

	@Reference
	private RemoteAppEntryLocalService _remoteAppEntryLocalService;

}
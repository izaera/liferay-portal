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

package com.liferay.dataset.internal.jaxrs.application;

import com.liferay.dataset.data.DatasetDataFilterFactory;
import com.liferay.dataset.data.DatasetDataFilterFactoryRegistry;
import com.liferay.dataset.data.DatasetDataPagination;
import com.liferay.dataset.data.DatasetDataProvider;
import com.liferay.dataset.data.DatasetDataProviderRegistry;
import com.liferay.dataset.internal.jaxrs.context.provider.DatasetDataPaginationContextProvider;
import com.liferay.dataset.internal.jaxrs.context.provider.SortContextProvider;
import com.liferay.dataset.internal.jaxrs.context.provider.ThemeDisplayContextProvider;
import com.liferay.dataset.internal.json.DatasetDataResponseJSONFactory;
import com.liferay.dataset.ui.ActiveViewSettingsProvider;
import com.liferay.dataset.ui.action.DatasetActionProvider;
import com.liferay.dataset.ui.action.DatasetActionProviderRegistry;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

/**
 * @author Marco Leo
 */
@Component(
	property = {
		JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/dataset-impl/app",
		JaxrsWhiteboardConstants.JAX_RS_NAME + "=Liferay.Dataset",
		"auth.verifier.auth.verifier.PortalSessionAuthVerifier.urls.includes=/*",
		"auth.verifier.guest.allowed=true", "liferay.oauth2=false"
	},
	service = Application.class
)
public class DatasetApplication extends Application {

	@GET
	@Path("/dataset/{datasetDataProviderKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDatasetData(
		@PathParam("datasetDataProviderKey") String datasetDataProviderKey,
		@QueryParam("groupId") long groupId, @QueryParam("plid") long plid,
		@QueryParam("portletId") String portletId,
		@Context HttpServletRequest httpServletRequest,
		@Context HttpServletResponse httpServletResponse,
		@Context DatasetDataPagination datasetDataPagination,
		@Context Sort sort, @Context ThemeDisplay themeDisplay,
		@Context UriInfo uriInfo) {

		DatasetDataProvider dataSetProvider =
			_datasetDataProviderRegistry.getDatasetDataProvider(
				datasetDataProviderKey);

		if ((dataSetProvider == null) && _log.isDebugEnabled()) {
			_log.debug(
				"No dataset data provider is associated with " +
					datasetDataProviderKey);
		}

		try {
			DatasetDataFilterFactory datasetDataFilterFactory =
				_datasetDataFilterFactoryRegistry.getDatasetDataFilterFactory(
					datasetDataProviderKey);

			List<DatasetActionProvider> datasetActionProviders =
				_datasetActionProviderRegistry.getDatasetActionProviders(
					datasetDataProviderKey);

			return Response.ok(
				_datasetDataResponseJSONFactory.serialize(
					datasetActionProviders, groupId, httpServletRequest,
					dataSetProvider.getItems(
						httpServletRequest,
						datasetDataFilterFactory.create(httpServletRequest),
						datasetDataPagination, sort),
					dataSetProvider.getItemsCount(
						httpServletRequest,
						datasetDataFilterFactory.create(httpServletRequest))),
				MediaType.APPLICATION_JSON
			).build();
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}

		return Response.status(
			Response.Status.NOT_FOUND
		).build();
	}

	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		singletons.add(_datasetDataPaginationContextProvider);
		singletons.add(_sortContextProvider);
		singletons.add(_themeDisplayContextProvider);
		singletons.add(this);

		return singletons;
	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/dataset/{id}/save-active-view-settings")
	@POST
	public Response saveActiveClayDataSetViewSettings(
		@PathParam("id") String id,
		@Context HttpServletRequest httpServletRequest,
		@Context HttpServletResponse httpServletResponse,
		@Context ThemeDisplay themeDisplay, @Context UriInfo uriInfo,
		String activeViewSettingsJSON) {

		try {
			String currentActiveViewSettingsJSON =
				_activeViewSettingsProvider.getActiveViewSettingsJSON(
					httpServletRequest, id);

			JSONObject currentActiveViewSettingsJSONObject =
				_jsonFactory.createJSONObject(currentActiveViewSettingsJSON);

			JSONObject activeViewSettingsJSONObject =
				_jsonFactory.createJSONObject(activeViewSettingsJSON);

			for (String key : activeViewSettingsJSONObject.keySet()) {
				currentActiveViewSettingsJSONObject.put(
					key, activeViewSettingsJSONObject.get(key));
			}

			_activeViewSettingsProvider.setActiveViewSettingsJSON(
				httpServletRequest, id,
				currentActiveViewSettingsJSONObject.toJSONString());

			return Response.ok(
			).build();
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}

		return Response.status(
			Response.Status.NOT_FOUND
		).build();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		DatasetApplication.class);

	@Reference
	private ActiveViewSettingsProvider _activeViewSettingsProvider;

	@Reference
	private DatasetActionProviderRegistry _datasetActionProviderRegistry;

	@Reference
	private DatasetDataFilterFactoryRegistry _datasetDataFilterFactoryRegistry;

	@Reference
	private DatasetDataPaginationContextProvider
		_datasetDataPaginationContextProvider;

	@Reference
	private DatasetDataProviderRegistry _datasetDataProviderRegistry;

	@Reference
	private DatasetDataResponseJSONFactory _datasetDataResponseJSONFactory;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private LayoutLocalService _layoutLocalService;

	@Reference
	private SortContextProvider _sortContextProvider;

	@Reference
	private ThemeDisplayContextProvider _themeDisplayContextProvider;

}
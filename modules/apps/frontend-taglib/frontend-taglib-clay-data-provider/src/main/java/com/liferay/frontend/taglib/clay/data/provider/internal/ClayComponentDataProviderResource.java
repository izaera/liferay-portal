/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package com.liferay.frontend.taglib.clay.data.provider.internal;

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProvider;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProviderRegistry;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentItemBuilder;
import com.liferay.frontend.taglib.clay.data.provider.Filter;
import com.liferay.frontend.taglib.clay.data.provider.FilterFactory;
import com.liferay.frontend.taglib.clay.data.provider.FilterFactoryRegistry;
import com.liferay.frontend.taglib.clay.data.provider.PaginationImpl;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(service = ClayComponentDataProviderResource.class)
public class ClayComponentDataProviderResource {

	@GET
	@Path("/clay-data-provider/{dataProviderKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
		@PathParam("dataProviderKey") String dataProviderKey,
		@QueryParam("plid") long plid,
		@QueryParam("portletId") String portletId, @Context UriInfo uriInfo,
		@QueryParam("pageSize") int pageSize, @QueryParam("page") int page,
		@Context HttpServletRequest request) {

		ClayComponentDataProvider provider = _dataProviderRegistry.get(
			dataProviderKey);

		try {
			PaginationImpl pagination = new PaginationImpl(pageSize, page);

			Filter filter = _getFilter(dataProviderKey, request);

			List items = provider.getItems(request, filter, pagination);

			items = _clayComponentItemBuilder.build(
				request, dataProviderKey, items);

			return Response.ok(
				items, MediaType.APPLICATION_JSON
			).build();
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return Response.status(
			Response.Status.NOT_FOUND
		).build();
	}

	private Filter _getFilter(
		String dataProviderKey, HttpServletRequest request) {

		FilterFactory factory = _filterFactoryRegistry.getFilterFactory(
			dataProviderKey);

		return factory.create(request);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentDataProviderResource.class);

	@Reference
	private ClayComponentItemBuilder _clayComponentItemBuilder;

	@Reference
	private ClayComponentDataProviderRegistry _dataProviderRegistry;

	@Reference
	private FilterFactoryRegistry _filterFactoryRegistry;

}
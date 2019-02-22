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

package com.liferay.frontend.taglib.clay.data.contributor.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributor;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributorRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentItemBuilder;
import com.liferay.frontend.taglib.clay.data.contributor.Filter;
import com.liferay.frontend.taglib.clay.data.contributor.FilterFactory;
import com.liferay.frontend.taglib.clay.data.contributor.FilterFactoryRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.PaginationImpl;
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
@Component(service = ClayComponentDataContributorResource.class)
public class ClayComponentDataContributorResource {

	@GET
	@Path("/clay-data-contributor/{contributorName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(
		@PathParam("contributorName") String contributorName,
		@QueryParam("plid") long plid,
		@QueryParam("portletId") String portletId, @Context UriInfo uriInfo,
		@QueryParam("pageSize") int pageSize, @QueryParam("page") int page,
		@Context HttpServletRequest request) {

		ClayComponentDataContributor contributor = _dataContributorRegistry.get(
			contributorName);

		try {
			PaginationImpl pagination = new PaginationImpl(pageSize, page);

			Filter filter = _getFilter(contributorName, request);

			List items = contributor.getItems(request, filter, pagination);

			items = _clayComponentItemBuilder.build(
				request, contributorName, items);

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
		String contributorName, HttpServletRequest request) {

		FilterFactory factory = _filterFactoryRegistry.getFilterFactory(
			contributorName);

		return factory.create(request);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ClayComponentDataContributorResource.class);

	@Reference
	private ClayComponentItemBuilder _clayComponentItemBuilder;

	@Reference
	private ClayComponentDataContributorRegistry _dataContributorRegistry;

	@Reference
	private FilterFactoryRegistry _filterFactoryRegistry;

}
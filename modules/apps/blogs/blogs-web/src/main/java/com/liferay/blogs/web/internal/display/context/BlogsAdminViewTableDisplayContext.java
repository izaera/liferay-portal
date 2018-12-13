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

package com.liferay.blogs.web.internal.display.context;

import com.liferay.blogs.web.internal.util.BlogsUtil;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.BaseTableDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Field;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Schema;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Arrays;
import java.util.Collection;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Iván Zaera Avellón
 */
public class BlogsAdminViewTableDisplayContext extends BaseTableDisplayContext {

	public BlogsAdminViewTableDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse) {

		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
	}

	@Override
	public Collection<String> getDependencies() {
		return Arrays.asList(
			"frontend-taglib-clay@2.0.0/cell_renderers/CellRenderers.es");
	}

	@Override
	public String getId() {
		return "myTable2";
	}

	@Override
	public String getSpritemap() {
		ThemeDisplay themeDisplay = (ThemeDisplay)_renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		return themeDisplay.getPathThemeImages() + "/lexicon/icons.svg";
	}

	@Override
	public Boolean isSelectable() {
		return true;
	}

	@Override
	public Boolean isShowActionsMenu() {
		return false;
	}

	protected void configureSchema(Schema schema) {
		schema.setInputValueField("entryId");

		Field titleField = new Field("editable", "title", "Title");

		titleField.addCustomProperty("type", "text");

		schema.addField(titleField);

		schema.addField(new Field(StringPool.BLANK, "userName", "Author"));

		Field allowPingbacksField = new Field(
			"editable", "allowPingbacks", "Allow Pingbacks");

		allowPingbacksField.addCustomProperty("type", "bool");

		schema.addField(allowPingbacksField);

		Field allowTrackbacksField = new Field(
			"editable", "allowTrackbacks", "Allow Trackbacks");

		allowTrackbacksField.addCustomProperty("type", "bool");

		schema.addField(allowTrackbacksField);

		Field coverImageUrlField = new Field(
			"editable", "coverImageURL", "Cover Image");

		coverImageUrlField.addCustomProperty("type", "image");

		schema.addField(coverImageUrlField);
	}

	@Override
	protected Collection<?> doGetItems() {
		try {
			PortletURL portletURL = _renderResponse.createRenderURL();

			PortletResponse portletResponse =
				(PortletResponse)_renderRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_RESPONSE);

			LiferayPortletResponse liferayPortletResponse =
				PortalUtil.getLiferayPortletResponse(portletResponse);

			SearchContainer entriesSearchContainer = null;

			entriesSearchContainer = new SearchContainer(
				_renderRequest,
				PortletURLUtil.clone(portletURL, liferayPortletResponse), null,
				"no-entries-were-found");

			String orderByCol = ParamUtil.getString(
				_renderRequest, "orderByCol", "title");
			String orderByType = ParamUtil.getString(
				_renderRequest, "orderByType", "asc");

			entriesSearchContainer.setOrderByComparator(
				BlogsUtil.getOrderByComparator(orderByCol, orderByType));

			PortletRequest portletRequest =
				(PortletRequest)_renderRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_REQUEST);

			LiferayPortletRequest liferayPortletRequest =
				PortalUtil.getLiferayPortletRequest(portletRequest);

			BlogEntriesDisplayContext blogEntriesDisplayContext =
				new BlogEntriesDisplayContext(liferayPortletRequest);

			blogEntriesDisplayContext.populateResults(entriesSearchContainer);

			return entriesSearchContainer.getResults();
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
		catch (PortletException pe) {
			throw new RuntimeException(pe);
		}
	}

	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;

}
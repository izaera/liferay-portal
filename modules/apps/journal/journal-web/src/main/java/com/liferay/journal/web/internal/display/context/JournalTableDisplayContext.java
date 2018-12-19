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

package com.liferay.journal.web.internal.display.context;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.BaseTableDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Field;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Schema;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.trash.TrashHelper;
import com.liferay.trash.util.TrashWebKeys;

import java.util.Collection;
import java.util.Date;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Iván Zaera Avellón
 */
public class JournalTableDisplayContext
	extends BaseTableDisplayContext<JournalArticle> {

	public JournalTableDisplayContext(RenderRequest renderRequest) {
		_renderRequest = renderRequest;

		_portletRequest = (PortletRequest)_renderRequest.getAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST);

		_request = PortalUtil.getHttpServletRequest(_portletRequest);
	}

	@Override
	public String getId() {
		return "myTable2";
	}

	protected void configureSchema(Schema schema) {
		schema.addField(new Field("main", "title", "Title"));

		Field descriptionField = new Field(
			StringPool.BLANK, "description", "Description");

		descriptionField.setEscaping(false);

		schema.addField(descriptionField);

		schema.addField(new Field(StringPool.BLANK, "userName", "Author"));

		schema.addField(new Field(StringPool.BLANK, "status", "Status"));

		schema.addField(
			new Field(StringPool.BLANK, "modifiedDate", "Modified Date"));

		schema.addField(
			new Field(StringPool.BLANK, "displayDate", "Display Date"));

		schema.addField(new Field(StringPool.BLANK, "type", "Type"));
	}

	@Override
	protected Collection<JournalArticle> doGetItems() {
		try {
			LiferayPortletRequest liferayPortletRequest =
				PortalUtil.getLiferayPortletRequest(_portletRequest);

			PortletResponse portletResponse =
				(PortletResponse)_renderRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_RESPONSE);

			LiferayPortletResponse liferayPortletResponse =
				PortalUtil.getLiferayPortletResponse(portletResponse);

			PortletPreferences portletPreferences =
				_portletRequest.getPreferences();

			TrashHelper trashHelper = (TrashHelper)_request.getAttribute(
				TrashWebKeys.TRASH_HELPER);

			JournalDisplayContext journalDisplayContext =
				new JournalDisplayContext(
					_request, liferayPortletRequest, liferayPortletResponse,
					portletPreferences, trashHelper);

			SearchContainer articleSearchContainer =
				journalDisplayContext.getSearchContainer(false);

			return articleSearchContainer.getResults();
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
	}

	@Override
	protected Object getFieldValue(Field field, JournalArticle journalArticle) {
		String fieldName = field.getFieldName();

		if (fieldName.equals("status")) {
			return WorkflowConstants.getStatusLabel(journalArticle.getStatus());
		}
		else if (fieldName.equals("modifiedDate")) {
			Date modifiedDate = journalArticle.getModifiedDate();

			return LanguageUtil.getTimeDescription(
				_request, System.currentTimeMillis() - modifiedDate.getTime(),
				true);
		}
		else if (fieldName.equals("displayDate")) {
			Date displayDate = journalArticle.getDisplayDate();

			return LanguageUtil.getTimeDescription(
				_request, System.currentTimeMillis() - displayDate.getTime(),
				true);
		}
		else if (fieldName.equals("type")) {
			DDMStructure ddmStructure = journalArticle.getDDMStructure();

			ThemeDisplay themeDisplay = (ThemeDisplay)_request.getAttribute(
				WebKeys.THEME_DISPLAY);

			return ddmStructure.getName(themeDisplay.getLocale());
		}

		return super.getFieldValue(field, journalArticle);
	}

	private final PortletRequest _portletRequest;
	private final RenderRequest _renderRequest;
	private final HttpServletRequest _request;

}
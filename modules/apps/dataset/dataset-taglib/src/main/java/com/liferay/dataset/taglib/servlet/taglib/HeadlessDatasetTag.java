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

package com.liferay.dataset.taglib.servlet.taglib;

import com.liferay.dataset.taglib.internal.json.DataSetViewsContextJSONFactory;
import com.liferay.dataset.taglib.internal.servlet.ServletContextUtil;
import com.liferay.dataset.taglib.internal.util.ServicesProvider;
import com.liferay.dataset.taglib.servlet.taglib.util.DatasetActionDropdownItem;
import com.liferay.dataset.ui.ActiveViewSettingsProvider;
import com.liferay.dataset.ui.filter.DatasetFilterSerializer;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.frontend.taglib.clay.data.set.model.ClayPaginationEntry;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SortItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.SortItemList;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PropsValues;
import com.liferay.taglib.util.IncludeTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author Marco Leo
 * @author Alessio Antonio Rendina
 */
public class HeadlessDatasetTag extends IncludeTag {

	@Override
	public int doStartTag() throws JspException {
		try {
			_appURL = PortalUtil.getPortalURL(request) + "/o/dataset-impl/app";

			if (_creationMenu == null) {
				_creationMenu = new CreationMenu();
			}

			NPMResolver npmResolver = ServicesProvider.getNPMResolver();

			if ((npmResolver != null) && Validator.isNull(_module)) {
				_module = npmResolver.resolveModuleName(
					"dataset-taglib/data_set_display/entry");
			}

			_setActiveViewSettingsJSON();
			_setDatasetViewsContext();
			_setDatasetFiltersContext();
			_setClayPaginationEntries();
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}

		return super.doStartTag();
	}

	public String getActionParameterName() {
		return _actionParameterName;
	}

	public String getApiURL() {
		return _apiURL;
	}

	public List<DropdownItem> getBulkActionDropdownItems() {
		return _bulkActionDropdownItems;
	}

	public CreationMenu getCreationMenu() {
		return _creationMenu;
	}

	public List<DatasetActionDropdownItem> getDatasetActionDropdownItems() {
		return _datasetActionDropdownItems;
	}

	public String getFormId() {
		return _formId;
	}

	public String getId() {
		return _id;
	}

	public int getItemsPerPage() {
		return _itemsPerPage;
	}

	public String getModule() {
		return _module;
	}

	public String getNamespace() {
		return _namespace;
	}

	public String getNestedItemsKey() {
		return _nestedItemsKey;
	}

	public String getNestedItemsReferenceKey() {
		return _nestedItemsReferenceKey;
	}

	public int getPageNumber() {
		return _pageNumber;
	}

	public PortletURL getPortletURL() {
		return _portletURL;
	}

	public List<String> getSelectedItems() {
		return _selectedItems;
	}

	public String getSelectedItemsKey() {
		return _selectedItemsKey;
	}

	public String getSelectionType() {
		return _selectionType;
	}

	public List<SortItem> getSortItemList() {
		return _sortItemList;
	}

	public String getStyle() {
		return _style;
	}

	public boolean isShowManagementBar() {
		return _showManagementBar;
	}

	public boolean isShowPagination() {
		return _showPagination;
	}

	public boolean isShowSearch() {
		return _showSearch;
	}

	public void setActionParameterName(String actionParameterName) {
		_actionParameterName = actionParameterName;
	}

	public void setApiURL(String apiURL) {
		_apiURL = apiURL;
	}

	public void setBulkActionDropdownItems(List<DropdownItem> bulkActions) {
		_bulkActionDropdownItems = bulkActions;
	}

	public void setCreationMenu(CreationMenu creationMenu) {
		_creationMenu = creationMenu;
	}

	public void setDatasetActionDropdownItems(
		List<DatasetActionDropdownItem> datasetActionDropdownItems) {

		_datasetActionDropdownItems = datasetActionDropdownItems;
	}

	public void setFormId(String formId) {
		_formId = formId;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setItemsPerPage(int itemsPerPage) {
		_itemsPerPage = itemsPerPage;
	}

	public void setNamespace(String namespace) {
		_namespace = namespace;
	}

	public void setNestedItemsKey(String nestedItemsKey) {
		_nestedItemsKey = nestedItemsKey;
	}

	public void setNestedItemsReferenceKey(String nestedItemsReferenceKey) {
		_nestedItemsReferenceKey = nestedItemsReferenceKey;
	}

	@Override
	public void setPageContext(PageContext pageContext) {
		_dataSetViewsContextJSONFactory =
			ServicesProvider.getDataSetViewsContextJSONFactory();
		_datasetFilterSerializer =
			ServicesProvider.getDatasetFilterSerializer();

		super.setPageContext(pageContext);

		servletContext = ServletContextUtil.getServletContext();
	}

	public void setPageNumber(int pageNumber) {
		_pageNumber = pageNumber;
	}

	public void setPortletURL(PortletURL portletURL) {
		_portletURL = portletURL;
	}

	public void setSelectedItems(List<String> selectedItems) {
		_selectedItems = selectedItems;
	}

	public void setSelectedItemsKey(String selectedItemsKey) {
		_selectedItemsKey = selectedItemsKey;
	}

	public void setSelectionType(String selectionType) {
		_selectionType = selectionType;
	}

	public void setShowManagementBar(boolean showManagementBar) {
		_showManagementBar = showManagementBar;
	}

	public void setShowPagination(boolean showPagination) {
		_showPagination = showPagination;
	}

	public void setShowSearch(boolean showSearch) {
		_showSearch = showSearch;
	}

	public void setSortItemList(SortItemList sortItemList) {
		_sortItemList = sortItemList;
	}

	public void setStyle(String style) {
		_style = style;
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_actionParameterName = null;
		_activeViewSettingsJSON = null;
		_apiURL = null;
		_appURL = null;
		_bulkActionDropdownItems = new ArrayList<>();
		_clayPaginationEntries = null;
		_creationMenu = new CreationMenu();
		_datasetActionDropdownItems = new ArrayList<>();
		_datasetFiltersContext = null;
		_datasetFilterSerializer = null;
		_datasetViewsContext = null;
		_dataSetViewsContextJSONFactory = null;
		_formId = null;
		_id = null;
		_itemsPerPage = 0;
		_module = null;
		_namespace = null;
		_nestedItemsKey = null;
		_nestedItemsReferenceKey = null;
		_pageNumber = 0;
		_paginationSelectedEntry = 0;
		_portletURL = null;
		_selectedItems = null;
		_selectedItemsKey = null;
		_selectionType = null;
		_showManagementBar = true;
		_showPagination = true;
		_showSearch = true;
		_sortItemList = new SortItemList();
		_style = "default";
	}

	@Override
	protected String getPage() {
		return _PAGE;
	}

	@Override
	protected void setAttributes(HttpServletRequest httpServletRequest) {
		request.setAttribute(
			"dataset:headless-dataset:actionParameterName",
			_actionParameterName);
		request.setAttribute(
			"dataset:headless-dataset:activeViewSettingsJSON",
			_activeViewSettingsJSON);
		request.setAttribute("dataset:headless-dataset:apiURL", _apiURL);
		request.setAttribute("dataset:headless-dataset:appURL", _appURL);
		request.setAttribute(
			"dataset:headless-dataset:bulkActionDropdownItems",
			_bulkActionDropdownItems);
		request.setAttribute(
			"dataset:headless-dataset:clayPaginationEntries",
			_clayPaginationEntries);
		request.setAttribute(
			"dataset:headless-dataset:creationMenu", _creationMenu);
		request.setAttribute(
			"dataset:headless-dataset:datasetActionDropdownItems",
			_datasetActionDropdownItems);
		request.setAttribute(
			"dataset:headless-dataset:datasetFiltersContext",
			_datasetFiltersContext);
		request.setAttribute(
			"dataset:headless-dataset:datasetViewsContext",
			_datasetViewsContext);
		request.setAttribute("dataset:headless-dataset:formId", _formId);
		request.setAttribute("dataset:headless-dataset:id", _id);
		request.setAttribute(
			"dataset:headless-dataset:itemsPerPage", _itemsPerPage);
		request.setAttribute("dataset:headless-dataset:module", _module);
		request.setAttribute("dataset:headless-dataset:namespace", _namespace);
		request.setAttribute(
			"dataset:headless-dataset:nestedItemsKey", _nestedItemsKey);
		request.setAttribute(
			"dataset:headless-dataset:nestedItemsReferenceKey",
			_nestedItemsReferenceKey);
		request.setAttribute(
			"dataset:headless-dataset:pageNumber", _pageNumber);
		request.setAttribute(
			"dataset:headless-dataset:paginationSelectedEntry",
			_paginationSelectedEntry);
		request.setAttribute(
			"dataset:headless-dataset:portletURL", _portletURL);
		request.setAttribute(
			"dataset:headless-dataset:selectedItems", _selectedItems);
		request.setAttribute(
			"dataset:headless-dataset:selectedItemsKey", _selectedItemsKey);
		request.setAttribute(
			"dataset:headless-dataset:selectionType", _selectionType);
		request.setAttribute(
			"dataset:headless-dataset:showManagementBar", _showManagementBar);
		request.setAttribute(
			"dataset:headless-dataset:showPagination", _showPagination);
		request.setAttribute(
			"dataset:headless-dataset:showSearch", _showSearch);
		request.setAttribute(
			"dataset:headless-dataset:sortItemList", _sortItemList);
		request.setAttribute("dataset:headless-dataset:style", _style);
	}

	private List<ClayPaginationEntry> _getClayPaginationEntries() {
		List<ClayPaginationEntry> clayPaginationEntries = new ArrayList<>();

		for (int curDelta : PropsValues.SEARCH_CONTAINER_PAGE_DELTA_VALUES) {
			if (curDelta > SearchContainer.MAX_DELTA) {
				continue;
			}

			clayPaginationEntries.add(new ClayPaginationEntry(null, curDelta));
		}

		return clayPaginationEntries;
	}

	private void _setActiveViewSettingsJSON() {
		ActiveViewSettingsProvider activeViewSettingsProvider =
			ServicesProvider.getActiveViewSettingsProvider();

		_activeViewSettingsJSON =
			activeViewSettingsProvider.getActiveViewSettingsJSON(request, _id);
	}

	private void _setClayPaginationEntries() {
		_clayPaginationEntries = _getClayPaginationEntries();

		Stream<ClayPaginationEntry> stream = _clayPaginationEntries.stream();

		ClayPaginationEntry clayPaginationEntry = stream.filter(
			entry -> entry.getLabel() == _itemsPerPage
		).findAny(
		).orElse(
			null
		);

		_paginationSelectedEntry = _clayPaginationEntries.indexOf(
			clayPaginationEntry);
	}

	private void _setDatasetFiltersContext() {
		_datasetFiltersContext = _datasetFilterSerializer.serialize(
			_id, PortalUtil.getLocale(request));
	}

	private void _setDatasetViewsContext() {
		_datasetViewsContext = _dataSetViewsContextJSONFactory.createJSONArray(
			_id, PortalUtil.getLocale(request));
	}

	private static final String _PAGE = "/headless_data_set_display/page.jsp";

	private static final Log _log = LogFactoryUtil.getLog(
		HeadlessDatasetTag.class);

	private String _actionParameterName;
	private String _activeViewSettingsJSON;
	private String _apiURL;
	private String _appURL;
	private List<DropdownItem> _bulkActionDropdownItems = new ArrayList<>();
	private List<ClayPaginationEntry> _clayPaginationEntries;
	private CreationMenu _creationMenu = new CreationMenu();
	private List<DatasetActionDropdownItem> _datasetActionDropdownItems =
		new ArrayList<>();
	private Object _datasetFiltersContext;
	private DatasetFilterSerializer _datasetFilterSerializer;
	private Object _datasetViewsContext;
	private DataSetViewsContextJSONFactory _dataSetViewsContextJSONFactory;
	private String _formId;
	private String _id;
	private int _itemsPerPage;
	private String _module;
	private String _namespace;
	private String _nestedItemsKey;
	private String _nestedItemsReferenceKey;
	private int _pageNumber;
	private int _paginationSelectedEntry;
	private PortletURL _portletURL;
	private List<String> _selectedItems;
	private String _selectedItemsKey;
	private String _selectionType;
	private boolean _showManagementBar = true;
	private boolean _showPagination = true;
	private boolean _showSearch = true;
	private SortItemList _sortItemList = new SortItemList();
	private String _style = "default";

}
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

package com.liferay.frontend.taglib.clay.servlet.taglib.soy;

import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProvider;
import com.liferay.frontend.taglib.clay.data.provider.ClayComponentDataProviderRegistry;
import com.liferay.frontend.taglib.clay.data.provider.Filter;
import com.liferay.frontend.taglib.clay.data.provider.FilterFactory;
import com.liferay.frontend.taglib.clay.data.provider.FilterFactoryRegistry;
import com.liferay.frontend.taglib.clay.data.provider.Pagination;
import com.liferay.frontend.taglib.clay.data.provider.PaginationImpl;
import com.liferay.frontend.taglib.clay.internal.FilterFactoryRegistryProvider;
import com.liferay.frontend.taglib.clay.internal.PaginationEntriesHelperProvider;
import com.liferay.frontend.taglib.clay.internal.js.loader.modules.extender.npm.NPMResolverProvider;
import com.liferay.frontend.taglib.clay.internal.model.ClayPaginationEntry;
import com.liferay.frontend.taglib.clay.servlet.taglib.data.provider.ClayComponentDataProviderRegistryUtil;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.TableDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Schema;
import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.table.Size;
import com.liferay.frontend.taglib.clay.servlet.taglib.soy.base.BaseClayTag;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.PaginationEntriesHelper;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

/**
 * @author Iván Zaera Avellón
 */
public class TableTag extends BaseClayTag {

	@Override
	public int doStartTag() {
		setComponentBaseName(
			"com.liferay.frontend.taglib.clay.ClayTaglibTable");
		setHydrate(true);
		setModuleBaseName("table");

		if (_tableDisplayContext != null) {
			_populateContext(_tableDisplayContext);
		}

		_setDataProviderAPI();
		_setItems();
		_setPagination();

		return super.doStartTag();
	}

	public TableDisplayContext getDisplayContext() {
		return _tableDisplayContext;
	}

	@Override
	public String getModule() {
		NPMResolver npmResolver = NPMResolverProvider.getNPMResolver();

		if (npmResolver == null) {
			return StringPool.BLANK;
		}

		return npmResolver.resolveModuleName(
			"frontend-taglib-clay/table/ClayTaglibTable.es");
	}

	public void setActionsMenuVariant(String actionsMenuVariant) {
		putValue("actionsMenuVariant", actionsMenuVariant);
	}

	public void setDataProviderKey(String dataProviderKey) {
		putValue("dataProviderKey", dataProviderKey);
	}

	public void setDeltaParam(String deltaParam) {
		putValue("deltaParam", deltaParam);
	}

	public void setDisableAJAX(boolean disableAJAX) {
		putValue("disableAJAX", disableAJAX);
	}

	public void setDisplayContext(TableDisplayContext tableDisplayContext) {
		_tableDisplayContext = tableDisplayContext;
	}

	public void setItems(Collection<?> items) {
		putValue("items", items);
	}

	public void setItemsPerPage(int itemsPerPage) {
		putValue("itemsPerPage", itemsPerPage);
	}

	public void setPageNumber(int pageNumber) {
		putValue("pageNumber", pageNumber);
	}

	public void setSchema(Schema schema) {
		Map<String, ?> schemaMap = null;

		if (schema != null) {
			schemaMap = schema.toMap();
		}

		putValue("schema", schemaMap);
	}

	public void setSelectable(Boolean selectable) {
		putValue("selectable", selectable);
	}

	public void setShowActionsMenu(Boolean showActionsMenu) {
		putValue("showActionsMenu", showActionsMenu);
	}

	public void setSize(Size size) {
		String sizeValue = null;

		if (size != null) {
			sizeValue = size.getValue();
		}

		putValue("size", sizeValue);
	}

	public void setTableClasses(String tableClasses) {
		putValue("tableClasses", tableClasses);
	}

	public void setUseDefaultClasses(Boolean useDefaultClasses) {
		putValue("useDefaultClasses", useDefaultClasses);
	}

	public void setWrapTable(Boolean wrapTable) {
		putValue("wrapTable", wrapTable);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();

		_tableDisplayContext = null;
	}

	private ClayComponentDataProvider _getDataProvider() {
		if (Validator.isNull(_getDataProviderKey())) {
			return null;
		}

		ClayComponentDataProviderRegistry registry = ClayComponentDataProviderRegistryUtil
			.getRegistry();

		if (registry == null) {
			return null;
		}

		return registry.get(_getDataProviderKey());
	}

	private String _getDataProviderKey() {
		return GetterUtil.getString(getContext().get("dataProviderKey"));
	}

	private String _getDeltaParam() {
		Object contextDeltaParam = getContext().get("deltaParam");

		return GetterUtil.getString(
			contextDeltaParam, SearchContainer.DEFAULT_DELTA_PARAM);
	}

	private Filter _getFilter() {
		FilterFactoryRegistry filterFactoryRegistry =
			FilterFactoryRegistryProvider.getRegistry();

		if (filterFactoryRegistry == null) {
			return null;
		}

		FilterFactory filterFactory = filterFactoryRegistry.getFilterFactory(
			_getDataProviderKey());

		return filterFactory.create(request);
	}

	private int _getItemsPerPage() {
		Object itemsPerPage = getContext().get("itemsPerPage");

		return GetterUtil.getInteger(
			itemsPerPage, _ITEMS_PER_PAGE_DEFAULT_VALUE);
	}

	private int _getPageNumber() {
		Object pageNumber = getContext().get("pageNumber");

		return GetterUtil.getInteger(pageNumber, _PAGE_NUMBER_DEFAULT_VALUE);
	}

	private List<ClayPaginationEntry> _getPaginationEntries(
		PaginationEntriesHelper paginationEntriesHelper) {

		String deltaParam = _getDeltaParam();

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		String portletId = String.valueOf(themeDisplay.getPlid());

		PortletURL portletURL = PortletURLFactoryUtil.create(
			request, portletId, PortletRequest.RENDER_PHASE);

		return paginationEntriesHelper.getPaginationEntries(
			portletURL, getNamespace(), deltaParam);
	}

	private int _getSelectedPaginationEntry(
		List<ClayPaginationEntry> paginationEntries) {

		int itemsPerPage = _getItemsPerPage();

		Stream<ClayPaginationEntry> stream = paginationEntries.stream();

		ClayPaginationEntry clayPaginationEntry = stream.filter(
			entry -> entry.getLabel() == itemsPerPage
		).findAny(
		).orElse(
			null
		);

		return paginationEntries.indexOf(clayPaginationEntry);
	}

	private void _populateContext(TableDisplayContext tableDisplayContext) {
		Map<String, Object> context = getContext();

		if (context.get("actionsMenuVariant") == null) {
			setActionsMenuVariant(tableDisplayContext.getActionsMenuVariant());
		}

		if (context.get("dependencies") == null) {
			Collection<String> dependencies =
				tableDisplayContext.getDependencies();

			if (dependencies != null) {
				setDependencies(new HashSet<>(dependencies));
			}
		}

		if (context.get("elementClasses") == null) {
			setElementClasses(tableDisplayContext.getElementClasses());
		}

		if (context.get("id") == null) {
			setId(tableDisplayContext.getId());
		}

		if (context.get("schema") == null) {
			setSchema(tableDisplayContext.getSchema());
		}

		if (context.get("selectable") == null) {
			setSelectable(tableDisplayContext.isSelectable());
		}

		if (context.get("showActionsMenu") == null) {
			setShowActionsMenu(tableDisplayContext.isShowActionsMenu());
		}

		if (context.get("items") == null) {
			setItems(tableDisplayContext.getItems());
		}

		if (context.get("size") == null) {
			setSize(tableDisplayContext.getSize());
		}

		if (context.get("spritemap") == null) {
			setSpritemap(tableDisplayContext.getSpritemap());
		}

		if (context.get("tableClasses") == null) {
			setTableClasses(tableDisplayContext.getTableClasses());
		}

		if (context.get("useDefaultClasses") == null) {
			setUseDefaultClasses(tableDisplayContext.isUseDefaultClasses());
		}

		if (context.get("wrapTable") == null) {
			setWrapTable(tableDisplayContext.isWrapTable());
		}
	}

	private void _setDataProviderAPI() {
		if (Validator.isNull(_getDataProviderKey())) {
			return;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		Layout layout = themeDisplay.getLayout();

		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		StringBundler sb = new StringBundler(7);

		sb.append(PortalUtil.getPortalURL(request));
		sb.append("/o/clay-data-provider/clay-data-provider/");
		sb.append(_getDataProviderKey());
		sb.append("?plid=");
		sb.append(layout.getPlid());
		sb.append("&portletId=");
		sb.append(portletDisplay.getId());

		putValue("dataProviderAPI", sb.toString());
	}

	private void _setItems() {
		ClayComponentDataProvider dataProvider = _getDataProvider();

		if (dataProvider == null) {
			return;
		}

		try {
			int itemsPerPage = _getItemsPerPage();
			int pageNumber = _getPageNumber();

			Pagination pagination = new PaginationImpl(
				itemsPerPage, pageNumber);

			Filter filter = _getFilter();

			List items = dataProvider.getItems(request, filter, pagination);

			setItems(items);

			int totalItems = dataProvider.countItems(request, filter);

			putValue("currentPage", pageNumber);
			putValue("pageSize", itemsPerPage);
			putValue("totalItems", totalItems);
		}
		catch (PortalException pe) {
			if (_log.isErrorEnabled()) {
				_log.error(pe, pe);
			}
		}
	}

	private void _setPagination() {
		if (Validator.isNull(_getDataProviderKey())) {
			return;
		}

		PaginationEntriesHelper paginationEntriesHelper =
			PaginationEntriesHelperProvider.getHelper();

		if (paginationEntriesHelper == null) {
			return;
		}

		List<ClayPaginationEntry> paginationEntries = _getPaginationEntries(
			paginationEntriesHelper);

		putValue("paginationEntries", paginationEntries);

		int paginationSelectedEntry = _getSelectedPaginationEntry(
			paginationEntries);

		putValue("paginationSelectedEntry", paginationSelectedEntry);
	}

	private static final int _ITEMS_PER_PAGE_DEFAULT_VALUE = 5;

	private static final int _PAGE_NUMBER_DEFAULT_VALUE = 1;

	private static final Log _log = LogFactoryUtil.getLog(TableTag.class);

	private TableDisplayContext _tableDisplayContext;

}
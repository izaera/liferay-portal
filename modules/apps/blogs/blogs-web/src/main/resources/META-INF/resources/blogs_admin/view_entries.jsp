<%--
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
--%>

<%@ include file="/blogs_admin/init.jsp" %>

<%
String entriesNavigation = ParamUtil.getString(request, "entriesNavigation");

int delta = ParamUtil.getInteger(request, SearchContainer.DEFAULT_DELTA_PARAM);
String orderByCol = ParamUtil.getString(request, "orderByCol", "title");
String orderByType = ParamUtil.getString(request, "orderByType", "asc");

PortletURL portletURL = renderResponse.createRenderURL();

portletURL.setParameter("mvcRenderCommandName", "/blogs/view");

if (delta > 0) {
	portletURL.setParameter("delta", String.valueOf(delta));
}

portletURL.setParameter("orderBycol", orderByCol);
portletURL.setParameter("orderByType", orderByType);

portletURL.setParameter("entriesNavigation", entriesNavigation);

SearchContainer entriesSearchContainer = new SearchContainer(renderRequest, PortletURLUtil.clone(portletURL, liferayPortletResponse), null, "no-entries-were-found");

entriesSearchContainer.setOrderByComparator(BlogsUtil.getOrderByComparator(orderByCol, orderByType));

BlogEntriesDisplayContext blogEntriesDisplayContext = new BlogEntriesDisplayContext(liferayPortletRequest);

blogEntriesDisplayContext.populateResults(entriesSearchContainer);

BlogEntriesManagementToolbarDisplayContext blogEntriesManagementToolbarDisplayContext = new BlogEntriesManagementToolbarDisplayContext(liferayPortletRequest, liferayPortletResponse, request, currentURLObj, trashHelper);

String displayStyle = blogEntriesManagementToolbarDisplayContext.getDisplayStyle();
%>

<clay:management-toolbar
	actionDropdownItems="<%= blogEntriesManagementToolbarDisplayContext.getActionDropdownItems() %>"
	clearResultsURL="<%= blogEntriesManagementToolbarDisplayContext.getSearchActionURL() %>"
	componentId="blogEntriesManagementToolbar"
	creationMenu="<%= blogEntriesManagementToolbarDisplayContext.getCreationMenu() %>"
	disabled="<%= entriesSearchContainer.getTotal() <= 0 %>"
	filterDropdownItems="<%= blogEntriesManagementToolbarDisplayContext.getFilterDropdownItems() %>"
	itemsTotal="<%= entriesSearchContainer.getTotal() %>"
	searchActionURL="<%= blogEntriesManagementToolbarDisplayContext.getSearchActionURL() %>"
	searchContainerId="blogEntries"
	searchFormName="searchFm"
	showInfoButton="<%= false %>"
	sortingOrder="<%= blogEntriesManagementToolbarDisplayContext.getOrderByType() %>"
	sortingURL="<%= String.valueOf(blogEntriesManagementToolbarDisplayContext.getSortingURL()) %>"
	viewTypeItems="<%= blogEntriesManagementToolbarDisplayContext.getViewTypes() %>"
/>

<portlet:actionURL name="/blogs/edit_entry" var="restoreTrashEntriesURL">
	<portlet:param name="<%= Constants.CMD %>" value="<%= Constants.RESTORE %>" />
</portlet:actionURL>

<liferay-trash:undo
	portletURL="<%= restoreTrashEntriesURL %>"
/>

<div class="container-fluid container-fluid-max-xl main-content-body">
	<aui:form action="<%= portletURL.toString() %>" method="get" name="fm">
		<aui:input name="<%= Constants.CMD %>" type="hidden" />
		<aui:input name="redirect" type="hidden" value="<%= portletURL.toString() %>" />
		<aui:input name="deleteEntryIds" type="hidden" />

		<liferay-asset:categorization-filter
			assetType="entries"
			portletURL="<%= portletURL %>"
		/>

		<%
		Map<String, String> editableTextCellData = new HashMap();
		editableTextCellData.put("contentrenderer", "editable");

		Map<String, String> editableNumberCellData = new HashMap();
		editableNumberCellData.put("contentrenderer", "editable");
		editableNumberCellData.put("type", "number");

		Map<String, String> editableBooleanCellData = new HashMap();
		editableBooleanCellData.put("contentrenderer", "editable");
		editableBooleanCellData.put("type", "bool");

		Map<String, String> editableImageCellData = new HashMap();
		editableBooleanCellData.put("contentrenderer", "editable");
		editableBooleanCellData.put("type", "image");
		%>

		<div class="sheet-section" style="background-color:#fff; padding:1rem; margin-bottom: 1rem;">
			<aui:fieldset id="tableColumns" label="Table Filter and Columns" markupView="lexicon">
				<div class="form-group-autofit">
					<aui:input wrapperCssClass="form-group-item" checked="<%= true %>" data="<%= editableTextCellData %>" name="title" type="checkbox" value="title" />
					<aui:input wrapperCssClass="form-group-item" checked="<%= true %>" name="author" type="checkbox" value="author" />
					<aui:input wrapperCssClass="form-group-item" checked="<%= true %>" data="<%= editableImageCellData %>" name="coverImageURL" type="checkbox" value="coverImageURL" />
					<aui:input wrapperCssClass="form-group-item" checked="<%= true %>" data="<%= editableBooleanCellData %>" name="allowPingbacks" type="checkbox" value="allowPingbacks" />
					<aui:input wrapperCssClass="form-group-item" checked="<%= true %>" data="<%= editableBooleanCellData %>" name="allowTrackbacks" type="checkbox" value="allowTrackbacks" />
				</div>

				<div class="form-group-autofit">
					<aui:input wrapperCssClass="form-group-item" name="filter" id="filterInput" />
				</div>
			</aui:fieldset>
		</div>

		<liferay-ui:search-container
			id="blogEntries"
			rowChecker="<%= new EmptyOnClickRowChecker(renderResponse) %>"
			searchContainer="<%= entriesSearchContainer %>"
		>
			<liferay-ui:search-container-row
				className="com.liferay.blogs.model.BlogsEntry"
				escapedModel="<%= true %>"
				keyProperty="entryId"
				modelVar="entry"
			>
				<liferay-portlet:renderURL varImpl="rowURL">
					<portlet:param name="mvcRenderCommandName" value="/blogs/edit_entry" />
					<portlet:param name="redirect" value="<%= entriesSearchContainer.getIteratorURL().toString() %>" />
					<portlet:param name="entryId" value="<%= String.valueOf(entry.getEntryId()) %>" />
				</liferay-portlet:renderURL>

				<%
				Map<String, Object> rowData = new HashMap<>();

				rowData.put("actions", String.join(StringPool.COMMA, blogEntriesManagementToolbarDisplayContext.getAvailableActionDropdownItems(entry)));

				row.setData(rowData);
				%>

				<%@ include file="/blogs_admin/entry_search_columns.jspf" %>
			</liferay-ui:search-container-row>

			<c:choose>
				<c:when test='<%= displayStyle.equals("list") %>'>
					<%
					JSONDeserializer jsonDeserializer = JSONFactoryUtil.createJSONDeserializer();
					JSONSerializer jsonSerializer = JSONFactoryUtil.createJSONSerializer();

					String emptyResultsMessage = entriesSearchContainer.getEmptyResultsMessage();
					String emptyResultsMessageCssClass = entriesSearchContainer.getEmptyResultsMessageCssClass();
					List<String> headerNames = entriesSearchContainer.getHeaderNames();
					List<String> normalizedHeaderNames = entriesSearchContainer.getNormalizedHeaderNames();
					Map orderableHeaders = entriesSearchContainer.getOrderableHeaders();
					List resultRows = entriesSearchContainer.getResultRows();
					String summary = entriesSearchContainer.getSummary();

					List<ResultRowSplitterEntry> resultRowSplitterEntries = new ArrayList<ResultRowSplitterEntry>();

					resultRowSplitterEntries.add(new ResultRowSplitterEntry(StringPool.BLANK, resultRows));

					List<com.liferay.portal.kernel.dao.search.ResultRow> firstResultRows = Collections.emptyList();

					if (!resultRowSplitterEntries.isEmpty()) {
						ResultRowSplitterEntry firstResultRowSplitterEntry = resultRowSplitterEntries.get(0);

						firstResultRows = firstResultRowSplitterEntry.getResultRows();
					}

					List<Object> items = new ArrayList();

					for (ResultRowSplitterEntry resultRowSplitterEntry : resultRowSplitterEntries) {
						List<com.liferay.portal.kernel.dao.search.ResultRow> curResultRows = resultRowSplitterEntry.getResultRows();

						for (int i = 0; i < curResultRows.size(); i++) {
							com.liferay.portal.kernel.dao.search.ResultRow row = (com.liferay.portal.kernel.dao.search.ResultRow)curResultRows.get(i);

							items.add(row.getObject());
						}
					}

					StringBundler sb = new StringBundler();

					sb.append("{");
					sb.append("\t\"fields\": [{");
					sb.append("\t\t\"contentRenderer\": \"editable\",");
					sb.append("\t\t\"type\": \"text\",");
					sb.append("\t\t\"fieldName\": \"title\",");
					sb.append("\t\t\"label\": \"Title\"");
					sb.append("\t},{");
					sb.append("\t\t\"fieldName\": \"userName\",");
					sb.append("\t\t\"label\": \"Author\"");
					sb.append("\t},{");
					sb.append("\t\t\"contentRenderer\": \"editable\",");
					sb.append("\t\t\"type\": \"bool\",");
					sb.append("\t\t\"fieldName\": \"allowPingbacks\",");
					sb.append("\t\t\"label\": \"Allow Pingbacks\"");
					sb.append("\t},{");
					sb.append("\t\t\"contentRenderer\": \"editable\",");
					sb.append("\t\t\"type\": \"bool\",");
					sb.append("\t\t\"fieldName\": \"allowTrackbacks\",");
					sb.append("\t\t\"label\": \"Allow Trackbacks\"");
					sb.append("\t},{");
					sb.append("\t\t\"contentRenderer\": \"editable\",");
					sb.append("\t\t\"type\": \"image\",");
					sb.append("\t\t\"fieldName\": \"coverImageURL\",");
					sb.append("\t\t\"label\": \"Cover Image\"");
					sb.append("\t}],");
					sb.append("\t\"inputValueField\": \"entryId\"");
					sb.append("}");

					Map<String, Object> context = new HashMap<>();

					context.put("items", items);
					context.put("schema", jsonDeserializer.deserialize(sb.toString()));
					context.put("selectable", true);
					context.put("showActionsMenu", false);
					context.put("spritemap", themeDisplay.getPathThemeImages() + "/lexicon/icons.svg");

					Set<String> customCellRenderers = new HashSet();

					customCellRenderers.add("frontend-taglib-clay@2.0.0/cell_renderers/CellRenderers.es");
					%>

					<soy:component-renderer
						context="<%= context %>"
						componentId="myTable"
						dependencies="<%= customCellRenderers %>"
						module="clay-table/src/ClayTable"
						templateNamespace="ClayTable.render"
					/>

					<liferay-ui:search-paginator
						id='myTableIteratorBottom'
						markupView="lexicon"
						searchContainer="<%= entriesSearchContainer %>"
					/>

					<aui:script require="metal-dom/src/all/dom as dom">
						var sortingOrder = true;

						var tableColumnsContainer = document.getElementById('tableColumns');

						var filter = function(query, table) {
							Liferay.Service(
								'/blogs.blogsentry/get-group-entries',
								{
									groupId: themeDisplay.getScopeGroupId(),
									status: 0,
									max: Number.MAX_VALUE
								},
								function(obj) {
									table.items = obj.filter(
										item => item.title.includes(query)
									);
								}
							);
						};

						Liferay.componentReady('myTable')
							.then(
								myTable => {
									dom.delegate(
										document.getElementById('tableColumns'),
										'click',
										'input[type=checkbox]',
										function(event) {
											myTable.schema.fields = Array.from(
												tableColumnsContainer.querySelectorAll('input:checked')
											).map(
												field => {
													return {
														type: field.dataset.type,
														contentRenderer: field.dataset.contentrenderer,
														fieldName: field.value,
														label: field.name.substring(name.lastIndexOf('_') + 1)
													};
												}
											);

											myTable.schema = myTable.schema;
										}
									);

									var filterInput = document.getElementById('<portlet:namespace />filterInput');

									filterInput.addEventListener(
										'input',
										event => {
											filter(event.target.value, myTable);
										}
									);

									myTable.on(
										'sortingButtonClicked',
										evt => {
											const cellIndex = evt.currentTarget.parentElement.cellIndex;

											myTable.items = myTable.items.sort(
												(item1, item2) => sortingOrder ? item1.title <= item2.title : item1.title > item2.title
											);

											sortingOrder = !sortingOrder;
										}
									);
								}
							);
					</aui:script>
				</c:when>
				<c:otherwise>
					<liferay-ui:search-iterator
						displayStyle="<%= displayStyle %>"
						markupView="lexicon"
					/>
				</c:otherwise>
			</c:choose>
		</liferay-ui:search-container>
	</aui:form>
</div>

<aui:script>
	var deleteEntries = function() {
		if (<%= trashHelper.isTrashEnabled(scopeGroupId) %> || confirm('<liferay-ui:message key="are-you-sure-you-want-to-delete-the-selected-entries" />')) {
			var form = document.getElementById('<portlet:namespace />fm');

			if (form) {
				form.setAttribute('method', 'post');

				var cmd = form.querySelector('#<portlet:namespace /><%= Constants.CMD %>');

				if (cmd) {
					cmd.setAttribute('value', '<%= trashHelper.isTrashEnabled(scopeGroupId) ? Constants.MOVE_TO_TRASH : Constants.DELETE %>');
				}

				var deleteEntryIds = form.querySelector('#<portlet:namespace />deleteEntryIds');

				if (deleteEntryIds) {
					deleteEntryIds.setAttribute('value', Liferay.Util.listCheckedExcept(form, '<portlet:namespace />allRowIds'));
				}

				submitForm(form, '<portlet:actionURL name="/blogs/edit_entry" />');
			}
		}
	};

	var ACTIONS = {
		'deleteEntries': deleteEntries
	};

	Liferay.componentReady('blogEntriesManagementToolbar').then(
		function(managementToolbar) {
			managementToolbar.on(
				'actionItemClicked',
				function(event) {
					var itemData = event.data.item.data;

					if (itemData && itemData.action && ACTIONS[itemData.action]) {
						ACTIONS[itemData.action]();
					}
				}
			);
		}
	);
</aui:script>
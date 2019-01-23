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

<%@ include file="/asset_tags_selector/init.jsp" %>

<%

class MultiSelectItem {
	private String label;
	private String value;

	MultiSelectItem(String label, String value) {
		this.label = label;
		this.value = value;
	}

	String getLabel() {
		return label;
	}

	String getValue() {
		return value;
	}

	void setLabel(String label) {
		this.label = label;
	}

	void setValue(String value) {
		this.value = value;
	}
}


PortletRequest portletRequest = (PortletRequest)request.getAttribute(JavaConstants.JAVAX_PORTLET_REQUEST);
PortletResponse portletResponse = (PortletResponse)request.getAttribute(JavaConstants.JAVAX_PORTLET_RESPONSE);
String namespace = AUIUtil.getNamespace(portletRequest, portletResponse);

String addCallback = GetterUtil.getString((String)request.getAttribute("liferay-asset:asset-tags-selector:addCallback"));
boolean allowAddEntry = GetterUtil.getBoolean((String)request.getAttribute("liferay-asset:asset-tags-selector:allowAddEntry"));
boolean autoFocus = GetterUtil.getBoolean((String)request.getAttribute("liferay-asset:asset-tags-selector:autoFocus"));
String eventName = (String)request.getAttribute("liferay-asset:asset-tags-selector:eventName");
long[] groupIds = (long[])request.getAttribute("liferay-asset:asset-tags-selector:groupIds");
String hiddenInput = (String)request.getAttribute("liferay-asset:asset-tags-selector:hiddenInput");
String id = GetterUtil.getString((String)request.getAttribute("liferay-asset:asset-tags-selector:id"));
PortletURL portletURL = (PortletURL)request.getAttribute("liferay-asset:asset-tags-selector:portletURL");
String removeCallback = GetterUtil.getString((String)request.getAttribute("liferay-asset:asset-tags-selector:removeCallback"));
String tagNamesSeparatedWithCommas = GetterUtil.getString((String)request.getAttribute("liferay-asset:asset-tags-selector:tagNames"));

List<String> tagNames = Arrays.asList(StringUtil.split(tagNamesSeparatedWithCommas));

//List<HashMap<String, String>> selectedItems = new ArrayList<>();
List<Object> selectedItems = new ArrayList<>();
for (String tagName : tagNames){

	//MultiSelectItem item = new MultiSelectItem(tagName, tagName);
	//selectedItems.add(item);


	HashMap<String, String> item = new HashMap<>();
	item.put("label", tagName);
	item.put("myValue", tagName);
	selectedItems.add(item);
}
String inputName = namespace + hiddenInput;
System.out.println("--------------------");
System.out.println(selectedItems);
System.out.println("--------------------");
System.out.println(inputName);
System.out.println("--------------------\n" +
	tagNames + "\n--------------------\n" +
	hiddenInput + "\n--------------------\n" +
	eventName);
%>

<h4>
	<liferay-ui:message key="tags" />
</h4>

<clay:multi-select
	componentId="myMultiselect"
	dataSource="https://jsonplaceholder.typicode.com/users"
	helpText="Amazing help text"
	labelLocator="name"
	valueLocator="name"
	inputName="<%= inputName %>"
	selectedItems="<%= selectedItems %>"
/>

<aui:script use="liferay-asset-taglib-tags-selector">
	Liferay.componentReady('myMultiselect').then(
		function(multiSelect) {
			multiSelect.on(
				'buttonClicked',
				function(event) {
					const selectedTagNames = multiSelect.selectedItems
						.map(item => item.value).join();
					debugger;
					_showMultiSelectPopUp(
						selectedTagNames,
						event,
						function(event) {
							multiSelect.selectedItems = event.selectedItems;
						}
					);
				}
			);
		}
	);

	const _showMultiSelectPopUp = function(selectedTagNames, event, callback) {
		event.preventDefault();

		const uri = A.Lang.sub(
			decodeURIComponent("<%= portletURL %>"),
			{
				selectedTagNames: selectedTagNames
			}
		);

		const itemSelectorDialog = new A.LiferayItemSelectorDialog(
			{
				eventName: "<%= eventName %>",
				on: {
					selectedItemChange: function(event) {
						var selectedItem = event.newVal;

						if (selectedItem) {
							event.selectedItems = [];
							A.Array.each(
								selectedItem.items.split(','),
								function(value) {
									event.selectedItems = [];
									if(_hasContent(value)){
										event.selectedItems.push(_createMultiSelectItemObject(value));
									}
								}
							);
							if (callback) {
								callback(event);
							}
						}
					}
				},
				'strings.add': Liferay.Language.get('done'),
				title: Liferay.Language.get('tags'),
				url: uri
			}
		);

		itemSelectorDialog.open();
	}

	const _hasContent = function(value) {
		return value !== undefined && value !== "" && value!== null;
	}
</aui:script>
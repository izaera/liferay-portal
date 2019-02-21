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

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import com.liferay.frontend.taglib.clay.data.provider.ClayComponentAction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rodolfo Roza Miranda
 */
public class ClayComponentItem {

	public ClayComponentItem(Object item) {
		_item = item;
		_actionItems = new ArrayList<>();
	}

	public void addActionItems(List<ClayComponentAction> actionItems) {
		this._actionItems.addAll(actionItems);
	}

	public List<ClayComponentAction> getActionItems() {
		return _actionItems;
	}

	@JsonUnwrapped
	public Object getItem() {
		return _item;
	}

	private final List<ClayComponentAction> _actionItems;
	private final Object _item;

}
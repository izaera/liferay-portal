package com.liferay.frontend.taglib.clay.servlet.taglib.util;

import com.liferay.frontend.taglib.clay.internal.model.ClayPaginationEntry;

import java.util.List;

import javax.portlet.PortletURL;

/**
 * @author Rodolfo Roza Miranda
 */
public interface PaginationEntriesHelper {

	public List<ClayPaginationEntry> getPaginationEntries(
		PortletURL portletURL, String namespace, String deltaParam);

}
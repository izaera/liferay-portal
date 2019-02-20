package com.liferay.frontend.taglib.clay.internal;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.PaginationEntriesHelper;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = {})
public class PaginationEntriesHelperProvider {

	public static PaginationEntriesHelper getHelper() {
		if (_instance == null) {
			return null;
		}

		return _instance._helper;
	}

	public PaginationEntriesHelperProvider() {
		_instance = this;
	}

	private static PaginationEntriesHelperProvider _instance;

	@Reference
	private PaginationEntriesHelper _helper;

}
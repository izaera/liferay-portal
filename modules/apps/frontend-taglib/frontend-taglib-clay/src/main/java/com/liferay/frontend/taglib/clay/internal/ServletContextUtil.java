package com.liferay.frontend.taglib.clay.internal;

import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentDataContributorRegistry;
import com.liferay.frontend.taglib.clay.data.contributor.ClayComponentItemBuilder;
import com.liferay.frontend.taglib.clay.data.contributor.FilterFactoryRegistry;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.PaginationEntriesHelper;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = ServletContextUtil.class)
public class ServletContextUtil {

	public static ClayComponentItemBuilder getClayComponentItemBuilder() {
		return _instance._clayComponentItemBuilder;
	}

	public static FilterFactoryRegistry getFilterFactoryRegistry() {
		return _instance._filterFactoryRegistry;
	}

	public static PaginationEntriesHelper getPaginationEntriesHelper() {
		return _instance._paginationEntriesHelper;
	}

	public static ClayComponentDataContributorRegistry getDataContributorRegistry() {
		return _instance._dataContributorRegistry;
	}

	@Activate
	protected void activate() {
		_instance = this;
	}

	@Deactivate
	protected void deactivate() {
		_instance = null;
	}

	private static ServletContextUtil _instance;

	@Reference
	private ClayComponentItemBuilder _clayComponentItemBuilder;

	@Reference
	private FilterFactoryRegistry _filterFactoryRegistry;

	@Reference
	private PaginationEntriesHelper _paginationEntriesHelper;

	@Reference
	private ClayComponentDataContributorRegistry _dataContributorRegistry;

}
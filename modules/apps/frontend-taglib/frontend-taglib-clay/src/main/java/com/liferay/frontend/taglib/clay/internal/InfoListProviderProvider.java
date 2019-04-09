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

package com.liferay.frontend.taglib.clay.internal;

import com.liferay.info.provider.InfoListProvider;
import com.liferay.info.provider.InfoListProviderTracker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = {})
public class InfoListProviderProvider {

	public static <T> InfoListProvider<T> getInfoListProvider(
		String className) {

		if (_infoListProviderProvider == null) {
			_log.error(
				"Unable to get list of Info list providers for class name " +
					className);

			return null;
		}

		InfoListProviderTracker infoListProviderTracker =
			_infoListProviderProvider._infoListProviderTracker;

		return infoListProviderTracker.getInfoListProvider(className);
	}

	public InfoListProviderProvider() {
		_infoListProviderProvider = this;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		InfoListProviderProvider.class);

	private static InfoListProviderProvider _infoListProviderProvider;

	@Reference
	private InfoListProviderTracker _infoListProviderTracker;

}
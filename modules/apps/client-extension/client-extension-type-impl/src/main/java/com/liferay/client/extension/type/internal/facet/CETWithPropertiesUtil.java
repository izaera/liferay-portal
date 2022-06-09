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

package com.liferay.client.extension.type.internal.facet;

import com.liferay.client.extension.model.ClientExtensionEntry;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;

import java.io.IOException;

import java.util.Properties;

/**
 * @author Iván Zaera Avellón
 */
public class CETWithPropertiesUtil {

	public static Properties getProperties(
		ClientExtensionEntry clientExtensionEntry) {

		if (clientExtensionEntry != null) {
			try {
				return PropertiesUtil.load(
					clientExtensionEntry.getProperties());
			}
			catch (IOException ioException) {
				ReflectionUtil.throwException(ioException);
			}
		}

		return new Properties();
	}

}
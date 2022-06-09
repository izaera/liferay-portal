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

package com.liferay.client.extension.type.util;

import com.liferay.client.extension.constants.ClientExtensionEntryConstants;
import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.CamelCaseUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Iván Zaera Avellón
 */
public class ClientExtensionEntryTypeUtil {

	public static String getLabel(String type) {
		return CamelCaseUtil.fromCamelCase(type, CharPool.DASH);
	}

	public static String getLabelAdd(String type) {
		return "add-" + CamelCaseUtil.fromCamelCase(type, CharPool.DASH);
	}

	public static String getLabelNew(String type) {
		return "new-" + CamelCaseUtil.fromCamelCase(type, CharPool.DASH);
	}

	public static Collection<String> getTypes() {
		return Collections.unmodifiableCollection(_types);
	}

	private static final Collection<String> _types = new ArrayList<String>() {
		{
			Class<ClientExtensionEntryConstants> clazz =
				ClientExtensionEntryConstants.class;

			for (Field field : clazz.getDeclaredFields()) {
				String name = field.getName();

				if (name.startsWith("TYPE_") &&
					((field.getModifiers() & Modifier.STATIC) != 0)) {

					try {
						add((String)field.get(null));
					}
					catch (IllegalAccessException illegalAccessException) {
						throw new RuntimeException(illegalAccessException);
					}
				}
			}
		}
	};

}
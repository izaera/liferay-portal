/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.frontend.hashed.files;

/**
 * @author Iván Zaera Avellón
 */
public enum CachingLevel {

	DO_NOT_USE_HASHES("do-not-use-hashes"),
	USE_ONE_HASH_PER_WEB_CONTEXT("use-one-hash-per-web-context");

	public static CachingLevel fromValue(String value) {
		for (CachingLevel cachingLevel : values()) {
			if (value.equals(cachingLevel.getValue())) {
				return cachingLevel;
			}
		}

		throw new IllegalArgumentException(value);
	}

	public String getValue() {
		return _value;
	}

	private CachingLevel(String value) {
		_value = value;
	}

	private final String _value;

}
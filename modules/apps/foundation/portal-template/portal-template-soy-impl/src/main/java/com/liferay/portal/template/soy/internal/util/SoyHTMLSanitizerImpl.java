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

package com.liferay.portal.template.soy.internal.util;

import com.google.template.soy.data.SanitizedContent;
import com.google.template.soy.data.UnsafeSanitizedContentOrdainer;

import com.liferay.portal.template.soy.utils.SoyHTMLSanitizer;

import org.osgi.service.component.annotations.Component;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shuyang Zhou
 */
@Component(immediate = true)
public class SoyHTMLSanitizerImpl implements SoyHTMLSanitizer {

	@Override
	public Object sanitize(String value) {
		return UnsafeSanitizedContentOrdainer.ordainAsSafe(
			value, SanitizedContent.ContentKind.HTML);
	}

	@Override
	public Object sanitize(String value, ContentKind contentKind) {
		return UnsafeSanitizedContentOrdainer.ordainAsSafe(
			value, _contentKindMap.get(contentKind));
	}

	private static final Map<ContentKind, SanitizedContent.ContentKind>
		_contentKindMap = new EnumMap<>(ContentKind.class);

	static {
		_contentKindMap.put(
			ContentKind.ATTRIBUTES, SanitizedContent.ContentKind.ATTRIBUTES);
		_contentKindMap.put(ContentKind.CSS, SanitizedContent.ContentKind.CSS);
		_contentKindMap.put(
			ContentKind.HTML, SanitizedContent.ContentKind.HTML);
		_contentKindMap.put(ContentKind.JS, SanitizedContent.ContentKind.JS);
		_contentKindMap.put(
			ContentKind.TEXT, SanitizedContent.ContentKind.TEXT);
		_contentKindMap.put(
			ContentKind.TRUSTED_RESOURCE_URI,
			SanitizedContent.ContentKind.TRUSTED_RESOURCE_URI);
		_contentKindMap.put(ContentKind.URI, SanitizedContent.ContentKind.URI);
	}

}
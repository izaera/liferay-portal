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

package com.liferay.portal.configuration.metatype.definitions.annotations.internal.extension;

import com.liferay.portal.configuration.metatype.annotations.ExtendedAttributeDefinition;
import com.liferay.portal.configuration.metatype.extension.ExtensionProcessor;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Raymond Augé
 */
@Component(service = ExtensionProcessor.class)
public class ExtendedAttributeDefinitionExtensionProcessor
	implements ExtensionProcessor<ExtendedAttributeDefinition> {

	@Override
	public Class<ExtendedAttributeDefinition> handlesType() {
		return ExtendedAttributeDefinition.class;
	}

	@Override
	public void process(
		com.liferay.portal.configuration.metatype.definitions.
			ExtendedAttributeDefinition definition,
		ExtendedAttributeDefinition extendedAttributeDefinition) {

		Map<String, String> map = definition.getExtensionAttributes(
			ExtendedAttributeDefinition.XML_NAMESPACE);

		map.put(
			"description-arguments",
			StringUtil.merge(
				extendedAttributeDefinition.descriptionArguments()));
		map.put(
			"name-arguments",
			StringUtil.merge(extendedAttributeDefinition.nameArguments()));
		map.put(
			"required-input",
			String.valueOf(extendedAttributeDefinition.requiredInput()));
	}

}
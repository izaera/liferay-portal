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

package com.liferay.portal.configuration.metatype.definitions.annotations.internal;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.definitions.ExtendedAttributeDefinition;
import com.liferay.portal.configuration.metatype.extension.ExtensionProcessor;
import com.liferay.portal.kernel.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAttribute;

import org.osgi.service.metatype.AttributeDefinition;

/**
 * @author Iván Zaera
 * @author Raymond Augé
 */
public class AnnotationsExtendedAttributeDefinition
	implements ExtendedAttributeDefinition {

	@SuppressWarnings("rawtypes")
	public AnnotationsExtendedAttributeDefinition(
		Class<?> configurationBeanClass,
		AttributeDefinition attributeDefinition,
		ServiceTrackerMap<Class<? extends Annotation>, ExtensionProcessor>
			extensionProcessorServiceTrackerMap) {

		_configurationBeanClass = configurationBeanClass;
		_attributeDefinition = attributeDefinition;
		_extensionProcessorServiceTrackerMap =
			extensionProcessorServiceTrackerMap;
		_prefix = Stream.of(
			_configurationBeanClass.getFields()
		).filter(
			f -> f.getName(
			).equals(
				"PREFIX_"
			)
		).findFirst(
		).map(
			f -> {
				try {
					return (String)f.get(_configurationBeanClass);
				}
				catch (ReflectiveOperationException roe) {
					return null;
				}
			}
		).orElse(
			StringPool.BLANK
		);

		if (configurationBeanClass != null) {
			_processExtendedMetatypeFields();
		}
	}

	@Override
	public int getCardinality() {
		return _attributeDefinition.getCardinality();
	}

	@Override
	public String[] getDefaultValue() {
		return _attributeDefinition.getDefaultValue();
	}

	@Override
	public String getDescription() {
		return _attributeDefinition.getDescription();
	}

	@Override
	public Map<String, String> getExtensionAttributes(String uri) {
		return _extensionAttributes.computeIfAbsent(
			uri, key -> new HashMap<>());
	}

	@Override
	public Set<String> getExtensionUris() {
		return _extensionAttributes.keySet();
	}

	@Override
	public String getID() {
		return _attributeDefinition.getID();
	}

	@Override
	public String getName() {
		return _attributeDefinition.getName();
	}

	@Override
	public String[] getOptionLabels() {
		return _attributeDefinition.getOptionLabels();
	}

	@Override
	public String[] getOptionValues() {
		return _attributeDefinition.getOptionValues();
	}

	@Override
	public int getType() {
		return _attributeDefinition.getType();
	}

	@Override
	public String validate(String value) {
		return _attributeDefinition.validate(value);
	}

	private String _mangled(String id) {
		String mangled = id;

		if ((_prefix.length() > 0) && mangled.startsWith(_prefix)) {
			mangled = mangled.substring(_prefix.length());
		}

		mangled = StringUtil.replace(mangled, '_', "__");
		mangled = StringUtil.replace(mangled, '.', '_');
		mangled = StringUtil.replace(mangled, '$', "$$");
		mangled = StringUtil.replace(mangled, '-', "$_$");

		return mangled;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void _processExtendedMetatypeFields() {
		List<Annotation> extensionAnnotations = Stream.of(
			_configurationBeanClass.getMethods()
		).filter(
			m -> {
				String methodName = m.getName();

				if (methodName.equals(_attributeDefinition.getID()) ||
					methodName.equals(_mangled(_attributeDefinition.getID()))) {

					return true;
				}

				return false;
			}
		).findFirst(
		).map(
			Method::getAnnotations
		).map(
			Arrays::stream
		).orElse(
			Stream.empty()
		).filter(
			ann -> ann.annotationType(
			).isAnnotationPresent(
				XmlAttribute.class
			)
		).collect(
			Collectors.toList()
		);

		for (Annotation annotation : extensionAnnotations) {
			ExtensionProcessor extensionProcessor =
				_extensionProcessorServiceTrackerMap.getService(
					annotation.annotationType());

			if (extensionProcessor != null) {
				extensionProcessor.process(this, annotation);
			}
		}
	}

	private final AttributeDefinition _attributeDefinition;
	private final Class<?> _configurationBeanClass;
	private final Map<String, Map<String, String>> _extensionAttributes =
		new HashMap<>();

	@SuppressWarnings("rawtypes")
	private final ServiceTrackerMap
		<Class<? extends Annotation>, ExtensionProcessor>
			_extensionProcessorServiceTrackerMap;

	private final String _prefix;

}
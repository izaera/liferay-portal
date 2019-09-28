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

import com.liferay.osgi.service.tracker.collections.map.ServiceReferenceMapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.configuration.metatype.definitions.ExtendedMetaTypeInformation;
import com.liferay.portal.configuration.metatype.definitions.ExtendedMetaTypeService;
import com.liferay.portal.configuration.metatype.extension.ExtensionProcessor;

import java.lang.annotation.Annotation;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.MetaTypeService;

/**
 * @author Iván Zaera
 * @author Raymond Augé
 */
@Component(service = ExtendedMetaTypeService.class)
public class AnnotationsExtendedMetaTypeService
	implements ExtendedMetaTypeService {

	@Override
	public ExtendedMetaTypeInformation getMetaTypeInformation(Bundle bundle) {
		return new AnnotationsExtendedMetaTypeInformation(
			bundle, _metaTypeService.getMetaTypeInformation(bundle),
			_extensionProcessorServiceTrackerMap);
	}

	@Activate
	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void activate(final BundleContext bundleContext) {
		_extensionProcessorServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, ExtensionProcessor.class, null,
				new ServiceReferenceMapper
					<Class<? extends Annotation>, ExtensionProcessor>() {

					@Override
					public void map(
						ServiceReference<ExtensionProcessor> serviceReference,
						Emitter<Class<? extends Annotation>> emitter) {

						ExtensionProcessor panelCategory =
							bundleContext.getService(serviceReference);

						try {
							emitter.emit(panelCategory.handlesType());
						}
						finally {
							bundleContext.ungetService(serviceReference);
						}
					}

				});
	}

	@Deactivate
	protected void deactivate() {
		_extensionProcessorServiceTrackerMap.close();
	}

	@Reference(unbind = "-")
	protected void setMetaTypeService(MetaTypeService metaTypeService) {
		_metaTypeService = metaTypeService;
	}

	@SuppressWarnings("rawtypes")
	private ServiceTrackerMap<Class<? extends Annotation>, ExtensionProcessor>
		_extensionProcessorServiceTrackerMap;

	private MetaTypeService _metaTypeService;

}
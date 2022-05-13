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

package com.liferay.client.extension.internal.upgrade;

import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import org.osgi.service.component.annotations.Component;

/**
 * @author Iván Zaera
 */
@Component(immediate = true, service = UpgradeStepRegistrator.class)
public class ClientExtensionServiceUpgrade implements UpgradeStepRegistrator {

	@Override
	public void register(Registry registry) {
		registry.register(
			"3.0.0", "3.1.0",
			new com.liferay.client.extension.internal.upgrade.v3_1_0.
				ClientExtensionEntryUpgradeProcess());

		registry.register(
			"3.1.0", "3.2.0",
			new com.liferay.client.extension.internal.upgrade.v3_2_0.
				ClientExtensionEntryUpgradeProcess());
	}

}
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

package com.liferay.remote.app.admin.web.internal;

import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistryUpdate;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.remote.app.admin.web.internal.portlet.RemoteAppPortlet;
import com.liferay.remote.app.model.RemoteAppEntry;
import com.liferay.remote.app.service.RemoteAppEntryLocalService;

import java.io.InputStream;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = RemoteAppPortletRegistrar.class)
public class RemoteAppPortletRegistrar {

	public void registerPortlet(RemoteAppEntry remoteAppEntry) {
		_registerPortlet(remoteAppEntry);
	}

	public void unregisterPortlet(RemoteAppEntry remoteAppEntry) {
		_unregisterPortlet(remoteAppEntry.getRemoteAppEntryId());
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_jsPackage = _npmResolver.getJSPackage();

		if (_log.isInfoEnabled()) {
			_log.info("Starting remote app entries");
		}

		for (RemoteAppEntry remoteAppEntry :
				remoteAppEntryLocalService.getRemoteAppEntries(
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			registerPortlet(remoteAppEntry);
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_log.isInfoEnabled()) {
			_log.info("Stopping remote app entries");
		}

		for (long remoteAppEntryId : _remoteAppPortlets.keySet()) {
			_unregisterPortlet(remoteAppEntryId);
		}
	}

	@Reference
	protected RemoteAppEntryLocalService remoteAppEntryLocalService;

	private static String _loadTemplate(String name) {
		try (InputStream inputStream =
				RemoteAppPortletRegistrar.class.getResourceAsStream(
					"dependencies/" + name)) {

			return StringUtil.read(inputStream);
		}
		catch (Exception exception) {
			_log.error("Unable to read template " + name, exception);
		}

		return StringPool.BLANK;
	}

	private void _registerPortlet(RemoteAppEntry remoteAppEntry) {
		NPMRegistryUpdate update = _npmRegistry.update();

		String nameDefaultLanguageId = LocalizationUtil.getDefaultLanguageId(
			remoteAppEntry.getName());

		String moduleName = remoteAppEntry.getName(nameDefaultLanguageId);

		if (moduleName.startsWith(StringPool.SLASH)) {
			moduleName = moduleName.substring(1);
		}

		update.registerJSModule(
			_jsPackage, moduleName, Collections.emptyList(),
			StringUtil.replace(
				_TPL_JAVA_SCRIPT, new String[] {
					"[$PACKAGE$]", "[$MODULE$]", "[$URL$]"
				},
				new String[] {
					_jsPackage.getResolvedId(), moduleName,
					remoteAppEntry.getUrl()
				}),
			null);

		update.finish();
		/*
		RemoteAppPortlet remoteAppPortlet = new RemoteAppPortlet(
			remoteAppEntry);

		long remoteAppEntryId = remoteAppEntry.getRemoteAppEntryId();

		RemoteAppPortlet existingRemoteAppPortlet =
			_remoteAppPortlets.putIfAbsent(remoteAppEntryId, remoteAppPortlet);

		if (existingRemoteAppPortlet != null) {
			throw new IllegalStateException(
				"Remote app entry " + remoteAppEntryId +
					" is already registered");
		}

		remoteAppPortlet.register(_bundleContext);

		if (_log.isInfoEnabled()) {
			_log.info("Started remote app entry " + remoteAppPortlet.getName());
		}
		*/
	}

	private void _unregisterPortlet(long remoteAppEntryId) {
		/*
		RemoteAppPortlet remoteAppPortlet = _remoteAppPortlets.remove(
			remoteAppEntryId);

		if (remoteAppPortlet != null) {
			remoteAppPortlet.unregister();

			if (_log.isInfoEnabled()) {
				_log.info(
					"Stopped remote app entry " + remoteAppPortlet.getName());
			}
		}
		*/
	}

	private static final String _TPL_JAVA_SCRIPT;

	private static final Log _log = LogFactoryUtil.getLog(
		RemoteAppPortletRegistrar.class);

	static {
		_TPL_JAVA_SCRIPT = _loadTemplate("amd.module.js.tpl");
	}

	private BundleContext _bundleContext;
	private JSPackage _jsPackage;

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private NPMResolver _npmResolver;

	private final ConcurrentMap<Long, RemoteAppPortlet> _remoteAppPortlets =
		new ConcurrentHashMap<>();

}
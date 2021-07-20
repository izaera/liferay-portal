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

import com.liferay.frontend.js.loader.modules.extender.npm.JSModule;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistryUpdate;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.remote.app.admin.web.internal.portlet.JSPortlet;
import com.liferay.remote.app.admin.web.internal.portlet.RemoteAppPortlet;
import com.liferay.remote.app.constants.RemoteAppConstants;
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

	public void register(RemoteAppEntry remoteAppEntry) {
		NPMRegistryUpdate update = _npmRegistry.update();

		_register(update, remoteAppEntry);

		update.finish();
	}

	public void unregister(RemoteAppEntry remoteAppEntry) {
		NPMRegistryUpdate update = _npmRegistry.update();

		_unregister(update, remoteAppEntry.getRemoteAppEntryId());

		update.finish();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_jsPackage = _npmResolver.getJSPackage();

		if (_log.isInfoEnabled()) {
			_log.info("Starting remote app entries");
		}

		NPMRegistryUpdate update = _npmRegistry.update();

		for (RemoteAppEntry remoteAppEntry :
				remoteAppEntryLocalService.getRemoteAppEntries(
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			_register(update, remoteAppEntry);
		}

		update.finish();
	}

	@Deactivate
	protected void deactivate() {
		if (_log.isInfoEnabled()) {
			_log.info("Stopping remote app entries");
		}

		NPMRegistryUpdate update = _npmRegistry.update();

		for (long remoteAppEntryId : _remoteAppPortlets.keySet()) {
			_unregister(update, remoteAppEntryId);
		}

		update.finish();
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

	private void _register(
		NPMRegistryUpdate update, RemoteAppEntry remoteAppEntry) {

		String type = remoteAppEntry.getType();

		if (type.equals(RemoteAppConstants.TYPE_JS_PORTLET)) {
			String moduleName =
				"js-portlet/" + remoteAppEntry.getRemoteAppEntryId();

			_jsModules.put(
				remoteAppEntry.getRemoteAppEntryId(),
				update.registerJSModule(
					_jsPackage, moduleName, Collections.emptyList(),
					StringUtil.replace(
						_TPL_JS_PORTLET,
						new String[] {"[$MODULE$]", "[$PACKAGE$]", "[$URL$]"},
						new String[] {
							moduleName, _jsPackage.getResolvedId(),
							remoteAppEntry.getUrl()
						}),
					null));

			_registerJSPortlet(remoteAppEntry, moduleName);
		}
		else if (type.equals(RemoteAppConstants.TYPE_REMOTE_APP)) {
			_registerRemoteAppPortlet(remoteAppEntry);
		}
	}

	private void _registerJSPortlet(
		RemoteAppEntry remoteAppEntry, String moduleName) {

		JSPortlet jsPortlet = new JSPortlet(
			_jsPackage, moduleName, remoteAppEntry);

		long remoteAppEntryId = remoteAppEntry.getRemoteAppEntryId();

		JSPortlet existingJSPortlet = _jsPortlets.putIfAbsent(
			remoteAppEntryId, jsPortlet);

		if (existingJSPortlet != null) {
			throw new IllegalStateException(
				"Remote app entry " + remoteAppEntryId +
					" is already registered");
		}

		jsPortlet.register(_bundleContext);

		if (_log.isInfoEnabled()) {
			_log.info("Started js portlet " + jsPortlet.getName());
		}
	}

	private void _registerRemoteAppPortlet(RemoteAppEntry remoteAppEntry) {
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
			_log.info(
				"Started remote app portlet " + remoteAppPortlet.getName());
		}
	}

	private void _unregister(NPMRegistryUpdate update, long remoteAppEntryId) {
		JSModule jsModule = _jsModules.remove(remoteAppEntryId);

		if (jsModule != null) {
			update.unregisterJSModule(jsModule);
		}

		if (_jsPortlets.containsKey(remoteAppEntryId)) {
			_unregisterJSPortlet(remoteAppEntryId);
		}

		if (_remoteAppPortlets.containsKey(remoteAppEntryId)) {
			_unregisterRemoteAppPortlet(remoteAppEntryId);
		}
	}

	private void _unregisterJSPortlet(long remoteAppEntryId) {
		JSPortlet jsPortlet = _jsPortlets.remove(remoteAppEntryId);

		if (jsPortlet != null) {
			jsPortlet.unregister();

			if (_log.isInfoEnabled()) {
				_log.info("Stopped JS portlet " + jsPortlet.getName());
			}
		}
	}

	private void _unregisterRemoteAppPortlet(long remoteAppEntryId) {
		RemoteAppPortlet remoteAppPortlet = _remoteAppPortlets.remove(
			remoteAppEntryId);

		if (remoteAppPortlet != null) {
			remoteAppPortlet.unregister();

			if (_log.isInfoEnabled()) {
				_log.info(
					"Stopped remote app portlet " + remoteAppPortlet.getName());
			}
		}
	}

	private static final String _TPL_JS_PORTLET = _loadTemplate(
		"js-portlet.js.tpl");

	private static final Log _log = LogFactoryUtil.getLog(
		RemoteAppPortletRegistrar.class);

	private BundleContext _bundleContext;
	private final ConcurrentMap<Long, JSModule> _jsModules =
		new ConcurrentHashMap<>();
	private JSPackage _jsPackage;
	private final ConcurrentMap<Long, JSPortlet> _jsPortlets =
		new ConcurrentHashMap<>();

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private NPMResolver _npmResolver;

	private final ConcurrentMap<Long, RemoteAppPortlet> _remoteAppPortlets =
		new ConcurrentHashMap<>();

}
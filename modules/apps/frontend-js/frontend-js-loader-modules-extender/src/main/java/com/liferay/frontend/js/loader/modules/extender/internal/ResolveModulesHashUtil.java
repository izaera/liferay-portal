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

package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterMasterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterRequest;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.StringBundler;

import java.lang.reflect.Method;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Iván Zaera Avellón
 */
public class ResolveModulesHashUtil {

	public static void addListener(Listener listener) {
		_listeners.add(listener);

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Notifying listener ", listener.toString(),
					" of hash update: ", _hash));
		}

		listener.onHashUpdate(_hash);
	}

	public static void removeListener(Listener listener) {
		_listeners.remove(listener);
	}

	public static void triggerUpdate() {
		if (ClusterMasterExecutorUtil.isMaster()) {
			if (_log.isDebugEnabled()) {
				_log.debug("Hash update triggered in master");
			}

			updateMasterHash();
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Hash update triggered in slave: delegating to master");
			}

			ClusterMasterExecutorUtil.executeOnMaster(
				_updateMasterHashMethodHandler);
		}
	}

	public interface Listener {

		public void onHashUpdate(String hash);

	}

	protected static synchronized void updateMasterHash() {
		if (!ClusterMasterExecutorUtil.isMaster()) {
			_log.error("Ignoring request to update master hash in slave node");

			return;
		}

		_hash = String.valueOf(UUID.randomUUID());
		_lastSeenPriority++;

		if (_log.isDebugEnabled()) {
			_log.debug("Updated master hash to: " + _hash);
		}

		_listeners.forEach(
			listener -> {
				if (_log.isDebugEnabled()) {
					_log.debug(
						StringBundler.concat(
							"Notifying listener ", listener.toString(),
							" of hash update: ", _hash));
				}

				listener.onHashUpdate(_hash);
			});

		if (_log.isDebugEnabled()) {
			_log.debug("Notifying slaves about hash update: " + _hash);
		}

		ClusterExecutorUtil.execute(
			ClusterRequest.createMulticastRequest(
				new MethodHandler(
					_updateSlaveHashMethod, _lastSeenPriority, _hash),
				true));
	}

	protected static synchronized void updateSlaveHash(
		long priority, String hash) {

		if (ClusterMasterExecutorUtil.isMaster()) {
			_log.error("Ignoring request to update slave hash in master node");

			return;
		}

		if (priority < _lastSeenPriority) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					StringBundler.concat(
						"Ignoring request to update slave due to priority: ",
						String.valueOf(priority), " < ",
						String.valueOf(_lastSeenPriority)));
			}

			return;
		}

		_hash = hash;
		_lastSeenPriority = priority;

		if (_log.isDebugEnabled()) {
			_log.debug("Updated slave hash to: " + _hash);
		}

		_listeners.forEach(
			listener -> {
				if (_log.isDebugEnabled()) {
					_log.debug(
						StringBundler.concat(
							"Notifying listener ", listener.toString(),
							" of hash update: ", _hash));
				}

				listener.onHashUpdate(_hash);
			});
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ResolveModulesHashUtil.class);

	private static volatile String _hash;
	private static volatile long _lastSeenPriority;
	private static volatile Set<Listener> _listeners =
		ConcurrentHashMap.newKeySet();
	private static final MethodHandler _updateMasterHashMethodHandler;
	private static final Method _updateSlaveHashMethod;

	static {
		try {
			Class<ResolveModulesHashUtil> clazz = ResolveModulesHashUtil.class;

			_updateMasterHashMethodHandler = new MethodHandler(
				clazz.getDeclaredMethod("updateMasterHash"));
			_updateSlaveHashMethod = clazz.getDeclaredMethod(
				"updateSlaveHash", long.class, String.class);
		}
		catch (NoSuchMethodException noSuchMethodException) {
			throw new RuntimeException(noSuchMethodException);
		}

		_hash = String.valueOf(UUID.randomUUID());
	}

}
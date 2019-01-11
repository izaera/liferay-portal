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

package com.liferay.frontend.js.loader.modules.extender.internal.resolution;

import com.liferay.frontend.js.loader.modules.extender.internal.cfggen.JSConfigGeneratorModule;
import com.liferay.frontend.js.loader.modules.extender.internal.cfggen.JSConfigGeneratorModulesTracker;
import com.liferay.frontend.js.loader.modules.extender.npm.JSModuleAlias;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.petra.string.StringPool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = JSModulesNameMapper.class)
public class JSModulesNameMapper {

	public String mapModule(String module) {
		return mapModule(module, null);
	}

	public String mapModule(String module, Map<String, String> contextMap) {
		if (_cache.containsKey(module)) {
			return _cache.get(module);
		}

		String matchModule = module;

		if (contextMap != null) {
			matchModule = _map(matchModule, contextMap);
		}

		String resolved = _map(matchModule, null);

		_cache.put(module, resolved);

		return resolved;
	}

	private Map<String, String> _getExactMatchContextMap() {
		Map<String, String> exactMatchContextMap = new HashMap<>();

		for (JSPackage jsPackage : _npmRegistry.getResolvedJSPackages()) {
			String jsPackageResolvedId = jsPackage.getResolvedId();

			String mainModulePath =
				jsPackageResolvedId + StringPool.SLASH +
					jsPackage.getMainModuleName();

			exactMatchContextMap.put(jsPackageResolvedId, mainModulePath);

			for (JSModuleAlias jsModuleAlias : jsPackage.getJSModuleAliases()) {
				String aliasPath =
					jsPackageResolvedId + StringPool.SLASH +
						jsModuleAlias.getAlias();

				String moduleNamePath =
					jsPackageResolvedId + StringPool.SLASH +
						jsModuleAlias.getModuleName();

				exactMatchContextMap.put(aliasPath, moduleNamePath);
			}
		}

		return exactMatchContextMap;
	}

	private Map<String, String> _getPartialMatchContextMap() {
		if (_jsConfigGeneratorModulesTracker.getLastModified() >
				_jsLoaderModulesTrackerLastModified) {

			_partialMatchContextMap.clear();
			_cache.clear();

			Collection<JSConfigGeneratorModule> jsConfigGeneratorModules =
				_jsConfigGeneratorModulesTracker.getJSConfigGeneratorModules();

			Function<JSConfigGeneratorModule, String> valueMapper =
				m -> m.getName() + StringPool.AT + m.getVersion();

			Stream<JSConfigGeneratorModule> jsConfigGeneratorModulesStream =
				jsConfigGeneratorModules.stream();

			Map<String, String> map = jsConfigGeneratorModulesStream.collect(
				Collectors.toMap(JSConfigGeneratorModule::getName, valueMapper)
			);

			_partialMatchContextMap.putAll(map);

			_partialMatchContextMap.putAll(_npmRegistry.getGlobalAliases());

			_jsLoaderModulesTrackerLastModified =
				_jsConfigGeneratorModulesTracker.getLastModified();
		}

		return _partialMatchContextMap;
	}

	private String _map(String module, Map<String, String> map) {
		Map<String, String> exactMap;
		Map<String, String> partialMap;

		if (map == null) {
			exactMap = _getExactMatchContextMap();
			partialMap = _getPartialMatchContextMap();
		}
		else {
			exactMap = map;
			partialMap = map;
		}

		for (Map.Entry<String, String> entry : exactMap.entrySet()) {
			String alias = entry.getKey();
			String aliasValue = entry.getValue();

			if (alias.equals(module)) {
				return aliasValue;
			}
		}

		for (Map.Entry<String, String> entry : partialMap.entrySet()) {
			String alias = entry.getKey();
			String aliasValue = entry.getValue();

			if (alias.equals(module) ||
				module.startsWith(alias + StringPool.SLASH)) {

				return aliasValue + module.substring(alias.length());
			}
		}

		return module;
	}

	private final Map<String, String> _cache = new ConcurrentHashMap<>();

	@Reference
	private JSConfigGeneratorModulesTracker _jsConfigGeneratorModulesTracker;

	private long _jsLoaderModulesTrackerLastModified = 0L;

	@Reference
	private NPMRegistry _npmRegistry;

	private final Map<String, String> _partialMatchContextMap =
		new ConcurrentHashMap<>();

}
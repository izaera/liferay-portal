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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Rodolfo Roza Miranda
 */
public class JSModuleContext {

	public void addProcessedModule(String module) {
		_processedModules.add(module);
	}

	public void addResolvedModule(String alias) {
		_resolvedModules.add(0, alias);
	}

	public Map<String, String> getConfigMap() {
		return _configMap;
	}

	public Map<String, Map<String, String>> getModuleMap() {
		return _moduleMap;
	}

	public Map<String, String> getPathMap() {
		return _pathMap;
	}

	public List<String> getResolvedModules() {
		ArrayList<String> copy = new ArrayList<>(_resolvedModules);

		Collections.reverse(copy);

		return copy;
	}

	public boolean processedModule(String module) {
		return _processedModules.contains(module);
	}

	public void putConfig(String module, String mappedModule) {
		_configMap.put(module, mappedModule);
	}

	public void putModuleDependencyMap(
		String alias, Map<String, String> dependenciesMap) {

		_moduleMap.put(alias, dependenciesMap);
	}

	public void putPath(String alias, String path) {
		_pathMap.put(alias, path);
	}

	private final Map<String, String> _configMap = new ConcurrentHashMap<>();
	private final Map<String, Map<String, String>> _moduleMap =
		new ConcurrentHashMap<>();
	private final Map<String, String> _pathMap = new ConcurrentHashMap<>();
	private final Set<String> _processedModules = new ConcurrentSkipListSet<>();
	private final List<String> _resolvedModules = new ArrayList<>();

}
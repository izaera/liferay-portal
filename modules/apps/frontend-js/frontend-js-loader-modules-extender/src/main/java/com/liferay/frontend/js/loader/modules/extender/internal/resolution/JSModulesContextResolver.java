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
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.adapter.JSLoaderModuleAdapter;
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.adapter.JSModuleAdapter;
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.adapter.NPMRegistryModuleAdapter;
import com.liferay.frontend.js.loader.modules.extender.npm.JSModule;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMRegistry;
import com.liferay.portal.kernel.util.Portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rodolfo Roza Miranda
 */
@Component(immediate = true, service = JSModulesContextResolver.class)
public class JSModulesContextResolver {

	public JSModuleContext resolve(List<String> modules) {
		JSModuleContext context = new JSModuleContext();

		for (String module : modules) {
			_resolve(module, context);
		}

		return context;
	}

	@Reference(unbind = "-")
	public void setJsConfigGeneratorModulesTracker(
		JSConfigGeneratorModulesTracker jsConfigGeneratorModulesTracker) {

		_jsConfigGeneratorModulesTracker = jsConfigGeneratorModulesTracker;
	}

	@Reference(unbind = "-")
	public void setMapper(JSModulesNameMapper mapper) {
		_mapper = mapper;
	}

	@Reference(unbind = "-")
	public void setNpmRegistry(NPMRegistry npmRegistry) {
		_npmRegistry = npmRegistry;
	}

	@Reference(unbind = "-")
	public void setPortal(Portal portal) {
		_portal = portal;
	}

	private ArrayList<JSModuleAdapter> _getAllModules() {
		Collection<JSConfigGeneratorModule> jsConfigGeneratorModules =
			_jsConfigGeneratorModulesTracker.getJSLoaderModules();

		Stream<JSConfigGeneratorModule> jsLoaderModuleStream =
			jsConfigGeneratorModules.stream();

		List<JSLoaderModuleAdapter> jsLoaderModuleAdapters =
			jsLoaderModuleStream.map(
				m -> new JSLoaderModuleAdapter(m, _portal)
			).collect(
				Collectors.toList()
			);

		Collection<JSModule> resolvedJSModules =
			_npmRegistry.getResolvedJSModules();

		Stream<JSModule> resolvedJSModulesStream = resolvedJSModules.stream();

		List<NPMRegistryModuleAdapter> npmRegistryModules =
			resolvedJSModulesStream.map(
				m -> new NPMRegistryModuleAdapter(m, _npmRegistry, _portal)
			).collect(
				Collectors.toList()
			);

		ArrayList<JSModuleAdapter> allModules = new ArrayList<>();

		allModules.addAll(jsLoaderModuleAdapters);
		allModules.addAll(npmRegistryModules);

		return allModules;
	}

	private String _mapModuleName(String module) {
		return _mapper.mapModule(module);
	}

	private String _mapModuleName(
		String module, Map<String, String> contextMap) {

		return _mapper.mapModule(module, contextMap);
	}

	private void _processModule(
		JSModuleAdapter adapter, JSModuleContext context) {

		if (adapter == null) {
			return;
		}

		Collection<String> dependencies = adapter.getDependencies();

		String alias = adapter.getAlias();

		Map<String, String> dependenciesMap = new ConcurrentHashMap<>();

		for (String dependency : dependencies) {
			if (!ModuleNameUtil.isReservedModuleName(dependency)) {
				String resolvedPath = ModuleNameUtil.resolvePath(
					alias, dependency);

				String mappedModuleName = _mapModuleName(
					resolvedPath, adapter.getMap());

				dependenciesMap.put(dependency, mappedModuleName);

				if (!context.processedModule(mappedModuleName)) {
					context.addProcessedModule(mappedModuleName);

					_processModule(mappedModuleName, context);

					context.addResolvedModule(mappedModuleName);
				}
			}
		}

		context.putPath(alias, adapter.getPath());
		context.putModuleDependencyMap(alias, dependenciesMap);
	}

	private void _processModule(String module, JSModuleContext context) {
		JSModule jsModule = _npmRegistry.getResolvedJSModule(module);

		if (jsModule != null) {
			_processModule(
				new NPMRegistryModuleAdapter(jsModule, _npmRegistry, _portal),
				context);
		}
	}

	private void _resolve(String module, JSModuleContext context) {
		String mappedModule = _mapModuleName(module);

		ArrayList<JSModuleAdapter> allModules = _getAllModules();

		JSModuleAdapter adapter = null;

		for (JSModuleAdapter m : allModules) {
			String alias = m.getAlias();

			if (alias.equals(mappedModule)) {
				adapter = m;

				break;
			}
		}

		if (adapter != null) {
			context.putConfig(module, mappedModule);

			_processModule(adapter, context);

			context.addResolvedModule(adapter.getAlias());
		}
		else {
			context.addResolvedModule(
				":ERROR: Module " + module + " not found");
		}
	}

	private JSConfigGeneratorModulesTracker _jsConfigGeneratorModulesTracker;
	private JSModulesNameMapper _mapper;
	private NPMRegistry _npmRegistry;
	private Portal _portal;

}
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

import com.liferay.frontend.js.loader.modules.extender.internal.config.generator.JSConfigGeneratorModule;
import com.liferay.frontend.js.loader.modules.extender.internal.config.generator.JSConfigGeneratorPackage;
import com.liferay.frontend.js.loader.modules.extender.internal.config.generator.JSConfigGeneratorPackageTracker;
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.descriptor.ConfigGeneratorModuleDescriptor;
import com.liferay.frontend.js.loader.modules.extender.internal.resolution.descriptor.NPMRegistryModuleDescriptor;
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
@Component(immediate = true, service = JSModulesResolver.class)
public class JSModulesResolver {

	public JSModulesResolution resolve(List<String> modules) {
		JSModulesResolution jsModulesResolution = new JSModulesResolution();

		List<JSModuleDescriptor> jsModuleDescriptors =
			_getAllJSModuleDescriptors();

		for (String module : modules) {
			_resolve(jsModuleDescriptors, module, jsModulesResolution);
		}

		return jsModulesResolution;
	}

	private JSModuleDescriptor _findJSModuleDescriptor(
		List<JSModuleDescriptor> jsModuleDescriptors, String mappedModule) {

		for (JSModuleDescriptor jsModuleDescriptor : jsModuleDescriptors) {
			if (mappedModule.equals(jsModuleDescriptor.getAlias())) {
				return jsModuleDescriptor;
			}
		}

		return null;
	}

	private List<JSModuleDescriptor> _getAllJSModuleDescriptors() {
		Collection<JSConfigGeneratorPackage> jsConfigGeneratorPackages =
			_jsConfigGeneratorPackageTracker.getJSConfigGeneratorPackages();

		Stream<JSConfigGeneratorPackage> jsConfigGeneratorPackagesStream =
			jsConfigGeneratorPackages.stream();

		List<ConfigGeneratorModuleDescriptor> configGeneratorModuleDescriptors =
			jsConfigGeneratorPackagesStream.reduce(
				new ArrayList<>(),
				(arrayList, pkg) -> {
					for (JSConfigGeneratorModule jsConfigGeneratorModule :
							pkg.getUnversionedModules()) {

						arrayList.add(
							new ConfigGeneratorModuleDescriptor(
								jsConfigGeneratorModule));
					}

					return arrayList;
				},
				(arrayList1, arrayList2) -> {
					ArrayList<ConfigGeneratorModuleDescriptor> result =
						new ArrayList<>(arrayList1);

					result.addAll(arrayList2);

					return result;
				});

		Collection<JSModule> resolvedJSModules =
			_npmRegistry.getResolvedJSModules();

		Stream<JSModule> resolvedJSModulesStream = resolvedJSModules.stream();

		List<NPMRegistryModuleDescriptor> npmRegistryModuleDescriptors =
			resolvedJSModulesStream.map(
				m -> new NPMRegistryModuleDescriptor(m, _npmRegistry)
			).collect(
				Collectors.toList()
			);

		ArrayList<JSModuleDescriptor> jsModuleDescriptors = new ArrayList<>();

		jsModuleDescriptors.addAll(configGeneratorModuleDescriptors);
		jsModuleDescriptors.addAll(npmRegistryModuleDescriptors);

		return jsModuleDescriptors;
	}

	private String _mapModuleName(String module) {
		return _jsModulesNameMapper.mapModule(module);
	}

	private String _mapModuleName(
		String module, Map<String, String> contextMap) {

		return _jsModulesNameMapper.mapModule(module, contextMap);
	}

	private void _processModule(
		JSModuleDescriptor jsModuleDescriptor,
		JSModulesResolution jsModulesResolution) {

		if (jsModuleDescriptor == null) {
			return;
		}

		Collection<String> dependencies = jsModuleDescriptor.getDependencies();

		String alias = jsModuleDescriptor.getAlias();

		Map<String, String> dependenciesMap = new ConcurrentHashMap<>();

		for (String dependency : dependencies) {
			if (ModuleNameUtil.isReservedModuleName(dependency)) {
				continue;
			}

			String resolvedPath = ModuleNameUtil.resolvePath(alias, dependency);

			String mappedModuleName = _mapModuleName(
				resolvedPath, jsModuleDescriptor.getMap());

			dependenciesMap.put(dependency, mappedModuleName);

			if (!jsModulesResolution.isProcessedModule(mappedModuleName)) {
				jsModulesResolution.addProcessedModule(mappedModuleName);

				_processModule(mappedModuleName, jsModulesResolution);

				jsModulesResolution.addResolvedModule(mappedModuleName);
			}
		}

		jsModulesResolution.putPath(alias, jsModuleDescriptor.getPath());
		jsModulesResolution.putModuleDependencyMap(alias, dependenciesMap);
	}

	private void _processModule(
		String module, JSModulesResolution jsModulesResolution) {

		JSModule jsModule = _npmRegistry.getResolvedJSModule(module);

		if (jsModule != null) {
			_processModule(
				new NPMRegistryModuleDescriptor(jsModule, _npmRegistry),
				jsModulesResolution);
		}
	}

	private void _resolve(
		List<JSModuleDescriptor> jsModuleDescriptors, String module,
		JSModulesResolution jsModulesResolution) {

		String mappedModule = _mapModuleName(module);

		JSModuleDescriptor jsModuleDescriptor = _findJSModuleDescriptor(
			jsModuleDescriptors, mappedModule);

		if (jsModuleDescriptor != null) {
			jsModulesResolution.putConfig(module, mappedModule);

			_processModule(jsModuleDescriptor, jsModulesResolution);

			jsModulesResolution.addResolvedModule(
				jsModuleDescriptor.getAlias());
		}
		else {
			jsModulesResolution.addResolvedModule(
				":ERROR: Module " + module + " not found");
		}
	}

	@Reference
	private JSConfigGeneratorPackageTracker _jsConfigGeneratorPackageTracker;

	@Reference
	private JSModulesNameMapper _jsModulesNameMapper;

	@Reference
	private NPMRegistry _npmRegistry;

	@Reference
	private Portal _portal;

}
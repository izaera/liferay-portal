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

import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.petra.string.StringPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rodolfo Roza Miranda
 */
public class PathResolver {

	/**
	 * Resolve dependency path based on current module's path.
	 * @param currentModulePath
	 * @param dependency
	 * @return the full path of the dependency if it is local, the dependency otherwise
	 * @review
	 */
	public static String resolvePath(
		String currentModulePath, String dependency) {

		if (!ModuleNameUtil.isLocalModuleName(dependency)) {
			return dependency;
		}

		List<String> dependencyParts = _getDirNameParts(dependency);

		List<String> currentModulePathParts = _getDirNameParts(
			currentModulePath);

		for (int i = 0; i < dependencyParts.size(); i++) {
			String dependencyPart = dependencyParts.get(i);

			if (dependencyPart.equals(".")) {
				continue;
			}

			if (dependencyPart.equals("..")) {
				if (!currentModulePathParts.isEmpty()) {
					currentModulePathParts.remove(
						currentModulePathParts.size() - 1);
				}
				else {
					currentModulePathParts.addAll(
						dependencyParts.subList(i, dependencyParts.size()));

					break;
				}
			}
			else {
				currentModulePathParts.add(dependencyPart);
			}
		}

		currentModulePathParts.add(_getBaseName(dependency));

		return String.join(StringPool.SLASH, currentModulePathParts);
	}

	private static String _getBaseName(String dependency) {
		int i = dependency.lastIndexOf(StringPool.SLASH);

		return dependency.substring(i + 1);
	}

	private static List<String> _getDirNameParts(String modulePath) {
		List<String> modulePathParts = new ArrayList<>(
			Arrays.asList(modulePath.split(StringPool.SLASH)));

		modulePathParts.remove(modulePathParts.size() - 1);

		return modulePathParts;
	}

}
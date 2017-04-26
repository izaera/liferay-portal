package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.portal.kernel.util.StringPool;

public class ModuleNameUtil {

	public static String toModuleName(String fileName) {
		return fileName.substring(0, fileName.length() - 3);
	}

	public static String toFileName(String moduleName) {
		return moduleName + ".js";
	}

	public static String getPackageName(String moduleName) {
		int i = moduleName.indexOf(StringPool.SLASH);

		if (i == -1) {
			i = moduleName.length();
		}

		return moduleName.substring(0, i);
	}

	public static String getPackagePath(String moduleName) {
		int i = moduleName.indexOf(StringPool.SLASH);

		if (i == -1) {
			return null;
		}

		return moduleName.substring(i + 1);
	}
}

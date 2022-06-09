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

package com.liferay.client.extension.constants;

/**
 * Constants referring to ClientExtensionEntry types must refer to string values
 * in camel case format.
 *
 * Their names must be all uppercase and start with the prefix "TYPE_".
 *
 * This is because we need to use reflection to enumerate these constants in
 * some parts of the code.
 *
 * @review
 * @author Iván Zaera Avellón
 */
public class ClientExtensionEntryConstants {

	public static final String TYPE_CUSTOM_ELEMENT = "customElement";

	public static final String TYPE_GLOBAL_CSS = "globalCSS";

	public static final String TYPE_GLOBAL_JS = "globalJS";

	public static final String TYPE_IFRAME = "iframe";

	public static final String TYPE_THEME_CSS = "themeCSS";

	public static final String TYPE_THEME_FAVICON = "themeFavicon";

	public static final String TYPE_THEME_JS = "themeJS";

}
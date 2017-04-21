package com.liferay.frontend.js.loader.modules.extender.registry.definitions;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link JSBundleObject} which contents can be requested using a URL
 */
public interface JSBundleAsset extends JSBundleObject {
	public String getURL();
	public InputStream openStream() throws IOException;
}

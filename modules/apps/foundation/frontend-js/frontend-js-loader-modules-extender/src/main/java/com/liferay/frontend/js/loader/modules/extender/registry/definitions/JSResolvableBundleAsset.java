package com.liferay.frontend.js.loader.modules.extender.registry.definitions;

/**
 * A {@link JSBundleAsset} which can be resolved from a pool of objects of the
 * same type.
 *
 * For example: several
 * {@link com.liferay.frontend.js.loader.modules.extender.registry.JSModule}
 * objects may point to the same physical asset living in different bundles and
 * each one of them can be requested using its {@link JSBundleAsset} URL.
 *
 * But, only the resolved instance chosen by a predefined algorithm from among
 * all is the one that can be requested through the
 * {@link JSResolvableBundleAsset} URL.
 *
 * This is useful to disambiguate duplicated assets.
 */
public interface JSResolvableBundleAsset extends JSBundleAsset {
	public String getResolvedURL();
	public String getResolvedId();
}

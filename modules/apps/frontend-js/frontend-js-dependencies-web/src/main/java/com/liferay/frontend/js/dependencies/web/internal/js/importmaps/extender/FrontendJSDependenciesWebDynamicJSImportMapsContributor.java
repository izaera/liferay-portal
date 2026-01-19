/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.frontend.js.dependencies.web.internal.js.importmaps.extender;

import com.liferay.frontend.js.importmaps.extender.DynamicJSImportMapsContributor;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.frontend.hashed.files.HashedFilesRegistry;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilder;
import com.liferay.portal.url.builder.AbsolutePortalURLBuilderFactory;
import com.liferay.portal.url.builder.ESModuleAbsolutePortalURLBuilder;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.Writer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(service = DynamicJSImportMapsContributor.class)
public class FrontendJSDependenciesWebDynamicJSImportMapsContributor
	implements DynamicJSImportMapsContributor {

	@Override
	public void writeGlobalImports(
			HttpServletRequest httpServletRequest, Writer writer)
		throws IOException {

//		writer.write("\"@liferay/frontend-js-api\": \"");
//
//		AbsolutePortalURLBuilder absolutePortalURLBuilder =
//			_absolutePortalURLBuilderFactory.getAbsolutePortalURLBuilder(
//				httpServletRequest);
//
//		writer.write(
//			"/o/js/-/frontend-js-dependencies-web("+
//			_hashedFilesRegistry.getServletContextHash("frontend-js-dependencies-web") +
//			")/exports/@liferay$js-api.js"
//		);
//
//		writer.write("\", \"@liferay/frontend-js-api/data-set\": \"");
//
//		writer.write(
//			"/o/js/-/frontend-js-dependencies-web(" +
//			_hashedFilesRegistry.getServletContextHash("frontend-js-dependencies-web") +
//			")/__liferay__/exports/@liferay$js-api$data-set.js"
//		);
//
//		writer.write(StringPool.QUOTE);

		boolean first = true;

		for (String moduleName : _MODULE_NAMES) {
			if (!first) {
				writer.write(", ");
			}
			else {
				first = false;
			}

			writer.write(StringPool.QUOTE);
			writer.write(moduleName);
			writer.write("\": \"");

			String escapedModuleName = StringUtil.replace(
				moduleName, CharPool.FORWARD_SLASH, CharPool.DOLLAR);

			writer.write(
				"/o/js/-/frontend-js-dependencies-web(" +
				_hashedFilesRegistry.getServletContextHash("frontend-js-dependencies-web") +
				")/__liferay__/exports/" + escapedModuleName +
				".js");

			writer.write(StringPool.QUOTE);
		}

	}

	@Override
	public void writeScopedImports(
		HttpServletRequest httpServletRequest, Writer writer) {
	}

	@Reference
	private AbsolutePortalURLBuilderFactory _absolutePortalURLBuilderFactory;

	@Reference
	private HashedFilesRegistry _hashedFilesRegistry;


	private static final String[] _MODULE_NAMES = {
		"cropperjs/dist/cropper.css",
		"graphql-hooks-memcache",
		"graphql-hooks",
		"graphql",
		"highlight.js/styles/monokai-sublime.css",
		"qrcode",
		"react-dropzone",
		"react-transition-group",
		"uuid",
		"react-flow-renderer",
		"react-helmet",
		"graphiql",
		"graphiql/graphiql.css",
		"axe-core",
		"clipboard",
		"cropperjs",
		"dagre",
		"dom-align",
		"fuzzy",
		"highlight.js",
		"highlight.js/lib/core",
		"highlight.js/lib/languages/java",
		"highlight.js/lib/languages/javascript",
		"highlight.js/lib/languages/plaintext",
		"liferay-ckeditor",
		"moment",
		"moment/min/moment-with-locales",
		"numeral",
		"object-hash",
		"qs",
		"react-text-mask",
		"text-mask-addons",
		"text-mask-core"
	};

}
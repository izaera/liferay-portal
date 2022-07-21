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

package com.liferay.frontend.js.web.internal.servlet.taglib.aui;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.servlet.taglib.aui.AMDRequire;
import com.liferay.portal.kernel.servlet.taglib.aui.ESImport;
import com.liferay.portal.kernel.servlet.taglib.aui.JSFragment;
import com.liferay.portal.kernel.servlet.taglib.aui.PortletData;
import com.liferay.portal.kernel.servlet.taglib.aui.PortletDataRenderer;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

/**
 * @author Iván Zaera Avellón
 */
@Component(immediate = true, service = PortletDataRenderer.class)
public class PortletDataRendererImpl implements PortletDataRenderer {

	@Override
	public void write(Collection<PortletData> portletDatas, Writer writer)
		throws IOException {

		Collection<JSFragment> jsFragments = _getJSFragments(portletDatas);

		// Write global declaration fragments

		writer.write("<script type=\"text/javascript\">\n");

		for (JSFragment jsFragment : jsFragments) {
			if (jsFragment.isGlobalDeclaration()) {
				writer.write(jsFragment.getCode());
				writer.write(StringPool.NEW_LINE);
			}
		}

		writer.write("</script>\n");

		writer.write("<script type=\"module\">\n");

		// Write ES prolog

		Map<String, Integer> usedVariables = new HashMap<>();

		Map<ESImport, ESImport> esImportMap = _computeESImportMap(
			jsFragments, usedVariables);

		if (!esImportMap.isEmpty()) {
			for (ESImport esImport : esImportMap.values()) {
				writer.write("import {");
				writer.write(esImport.getSymbol());

				String alias = esImport.getAlias();

				if (!alias.equals(esImport.getSymbol())) {
					writer.write(" as ");
					writer.write(alias);
				}

				writer.write("} from '");
				writer.write(esImport.getModule());
				writer.write("';\n");
			}
		}

		// Write AMD prolog

		Map<AMDRequire, AMDRequire> amdRequireMap = _computeAMDRequireMap(
			jsFragments, usedVariables);

		if (!amdRequireMap.isEmpty()) {
			writer.write("Liferay.Loader.require(\n");

			for (AMDRequire amdRequire : amdRequireMap.values()) {
				writer.write("  '");
				writer.write(amdRequire.getModule());
				writer.write("',\n");
			}

			writer.write("function(");

			String delimiter = StringPool.BLANK;

			for (AMDRequire amdRequire : amdRequireMap.values()) {
				writer.write(delimiter);
				writer.write(amdRequire.getAlias());

				delimiter = StringPool.COMMA_AND_SPACE;
			}

			writer.write(") {\n");
			writer.write("try {\n");
		}

		// Write AUI prolog

		Set<String> auiUseSet = _computeAUIUseSet(jsFragments);

		if (!auiUseSet.isEmpty()) {
			writer.write("AUI().use(\n");

			for (String auiUse : auiUseSet) {
				writer.write("  '");
				writer.write(auiUse);
				writer.write("',\n");
			}

			writer.write("function(A) {\n");
		}

		// Write actual JS code

		writer.write(
			_computeExecutableCode(amdRequireMap, esImportMap, jsFragments));

		// Write AUI epilog

		if (!auiUseSet.isEmpty()) {
			writer.write("});\n");
		}

		// Write AMD epilog

		if (!amdRequireMap.isEmpty()) {
			writer.write("} catch (err) {\n");
			writer.write("\tconsole.error(err);\n");
			writer.write("}\n");

			writer.write("});\n");
		}

		writer.write("\n</script>");
	}

	private Map<AMDRequire, AMDRequire> _computeAMDRequireMap(
		Collection<JSFragment> jsFragments,
		Map<String, Integer> usedVariables) {

		Map<AMDRequire, AMDRequire> amdRequireMap = new HashMap<>();

		for (JSFragment jsFragment : jsFragments) {
			Collection<AMDRequire> amdRequires = jsFragment.getAMDRequires();

			if ((amdRequires == null) || amdRequires.isEmpty()) {
				continue;
			}

			for (AMDRequire amdRequire : amdRequires) {
				if (amdRequireMap.containsKey(amdRequire)) {
					continue;
				}

				String variable = amdRequire.getAlias();

				if (usedVariables.containsKey(variable)) {
					int index = usedVariables.get(variable);

					usedVariables.put(variable, index + 1);

					variable += index;
				}
				else {
					usedVariables.put(variable, 1);
				}

				amdRequireMap.put(
					amdRequire,
					new AMDRequire(amdRequire.getModule(), variable));
			}
		}

		return amdRequireMap;
	}

	private Set<String> _computeAUIUseSet(Collection<JSFragment> jsFragments) {
		Set<String> auiUseSet = new HashSet<>();

		for (JSFragment jsFragment : jsFragments) {
			auiUseSet.addAll(jsFragment.getAUIUses());
		}

		return auiUseSet;
	}

	private Map<ESImport, ESImport> _computeESImportMap(
		Collection<JSFragment> jsFragments,
		Map<String, Integer> usedVariables) {

		Map<ESImport, ESImport> esImportMap = new HashMap<>();

		for (JSFragment jsFragment : jsFragments) {
			Collection<ESImport> esImports = jsFragment.getESImports();

			if ((esImports == null) || esImports.isEmpty()) {
				continue;
			}

			for (ESImport esImport : esImports) {
				if (esImportMap.containsKey(esImport)) {
					continue;
				}

				String variable = esImport.getAlias();

				if (usedVariables.containsKey(variable)) {
					int index = usedVariables.get(variable);

					usedVariables.put(variable, index + 1);

					variable += index;
				}
				else {
					usedVariables.put(variable, 0);
				}

				esImportMap.put(
					esImport,
					new ESImport(
						esImport.getSymbol(), variable, esImport.getModule()));
			}
		}

		return esImportMap;
	}

	private String _computeExecutableCode(
		Map<AMDRequire, AMDRequire> amdRequireMap,
		Map<ESImport, ESImport> esImportMap,
		Collection<JSFragment> jsFragments) {

		StringBundler sb = new StringBundler();

		for (JSFragment jsFragment : jsFragments) {
			String code = jsFragment.getCode();

			if (Validator.isNull(code) || jsFragment.isGlobalDeclaration()) {
				continue;
			}

			List<AMDRequire> amdRequires = jsFragment.getAMDRequires();
			List<String> auiUses = jsFragment.getAUIUses();

			boolean legacyJSFragment = false;

			if (!amdRequires.isEmpty() || !auiUses.isEmpty()) {
				legacyJSFragment = true;
			}

			if (legacyJSFragment) {
				sb.append("(function() {\n");
			}
			else {
				sb.append("{\n");
			}

			// Map AMD requires to their requested aliases

			List<AMDRequire> fragmentAMDRequires = jsFragment.getAMDRequires();

			for (AMDRequire fragmentAMDRequire : fragmentAMDRequires) {
				AMDRequire amdRequire = amdRequireMap.get(fragmentAMDRequire);

				if (!Objects.equals(
						amdRequire.getAlias(), fragmentAMDRequire.getAlias())) {

					sb.append("const ");
					sb.append(fragmentAMDRequire.getAlias());
					sb.append(" = ");
					sb.append(amdRequire.getAlias());
					sb.append(";\n");
				}
			}

			// Map ES imports to their requested aliases

			List<ESImport> fragmentESImports = jsFragment.getESImports();

			for (ESImport fragmentESImport : fragmentESImports) {
				ESImport esImport = esImportMap.get(fragmentESImport);

				if (!Objects.equals(
						esImport.getAlias(), fragmentESImport.getAlias())) {

					sb.append("const ");
					sb.append(fragmentESImport.getAlias());
					sb.append(" = ");
					sb.append(esImport.getAlias());
					sb.append(";\n");
				}
			}

			sb.append(code);

			if (!code.endsWith(StringPool.NEW_LINE)) {
				sb.append(StringPool.NEW_LINE);
			}

			if (legacyJSFragment) {
				sb.append("})();\n");
			}
			else {
				sb.append("}\n");
			}
		}

		return sb.toString();
	}

	private Collection<JSFragment> _getJSFragments(
		Collection<PortletData> portletDatas) {

		List<JSFragment> jsFragments = new ArrayList<>();

		for (PortletData portletData : portletDatas) {
			jsFragments.addAll(portletData.getJSFragments());
		}

		return jsFragments;
	}

}
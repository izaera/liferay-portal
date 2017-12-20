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

package com.liferay.frontend.taglib.chart.servlet.taglib.soy.base;

import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.frontend.taglib.chart.internal.js.loader.modules.extender.npm.NPMResolverProvider;
import com.liferay.frontend.taglib.soy.servlet.taglib.TemplateRendererTag;
import com.liferay.portal.kernel.servlet.taglib.util.OutputData;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.ServletRequest;

/**
 * @author Chema Balsas
 */
public abstract class BaseChartTag extends TemplateRendererTag {

	public BaseChartTag(String moduleBaseName) {
		_moduleBaseName = moduleBaseName;
	}

	@Override
	public int doStartTag() {
		setTemplateNamespace("Chart.render");

		_outputStylesheetLink();
		_outputTilesSVG();

		return super.doStartTag();
	}

	@Override
	public String getModule() {
		NPMResolver npmResolver = NPMResolverProvider.getNPMResolver();

		if (npmResolver == null) {
			return StringPool.BLANK;
		}

		return npmResolver.resolveModuleName(
			"clay-charts/lib/" + _moduleBaseName);
	}

	public void setColumns(Object columns) {
		putValue("columns", columns);
	}

	public void setGroups(Object groups) {
		putValue("groups", groups);
	}

	public void setId(String id) {
		putValue("id", id);
	}

	private OutputData _getOutputData() {
		ServletRequest servletRequest = getRequest();

		OutputData outputData = (OutputData)servletRequest.getAttribute(
			WebKeys.OUTPUT_DATA);

		if (outputData == null) {
			outputData = new OutputData();

			servletRequest.setAttribute(WebKeys.OUTPUT_DATA, outputData);
		}

		return outputData;
	}

	private void _outputStylesheetLink() {
		OutputData outputData = _getOutputData();

		NPMResolver npmResolver = NPMResolverProvider.getNPMResolver();

		if (npmResolver == null) {
			return;
		}

		String cssPath = npmResolver.resolveModuleName(
			"clay-charts/lib/css/main.css");

		StringBundler sb = new StringBundler(5);

		sb.append("<link href=\"");
		sb.append(PortalUtil.getPathModule());
		sb.append("/frontend-taglib-chart/node_modules/");
		sb.append(cssPath);
		sb.append("\" rel=\"stylesheet\">");

		outputData.addData(_OUTPUT_CSS_KEY, WebKeys.PAGE_TOP, sb);
	}

	private void _outputTilesSVG() {
		OutputData outputData = _getOutputData();

		NPMResolver npmResolver = NPMResolverProvider.getNPMResolver();

		if (npmResolver == null) {
			return;
		}

		String svgPath = npmResolver.resolveModuleName(
			"clay-charts/src/svg/patterns.svg");

		StringBundler sb = new StringBundler();

		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><defs><!-- circles --><pattern id=\"circles\" patternUnits=\"userSpaceOnUse\" width=\"16\" height=\"16\" style=\"fill:rgba(0,0,0,0.3)\"><circle cx=\"4\" cy=\"4\" r=\"2\"/><circle cx=\"12\" cy=\"12\" r=\"2\"/></pattern>" +
"<!-- diagonal left large --><pattern id=\"diagonal-left-large\" patternUnits=\"userSpaceOnUse\" width=\"10\" height=\"10\" style=\"fill:rgba(0,0,0,0.3)\"><polygon points=\"9.3,0 10,0.7 10,0 \"/><polygon points=\"0,0 0,0.7 9.3,10 10,10 10,9.3 0.7,0 \"/><polygon points=\"0,9.3 0,10 0.7,10 \"/></pattern>" +
"<!-- diagonal left small --><pattern id=\"diagonal-left-small\" patternUnits=\"userSpaceOnUse\" width=\"5\" height=\"5\" style=\"fill:rgba(0,0,0,0.3)\"><polygon points=\"5,0 4.3,0 5,0.7 \"/><polygon points=\"0.7,0 0,0 0,0.7 4.3,5 5,5 5,4.3 \"/><polygon points=\"0,4.3 0,5 0.7,5 \"/></pattern>" +
"<!-- diagonal right large --><pattern id=\"diagonal-right-large\" patternUnits=\"userSpaceOnUse\" width=\"10\" height=\"10\" style=\"fill:rgba(0,0,0,0.3)\"><polygon points=\"10,9.3 9.3,10 10,10 \"/><polygon points=\"10,0 9.3,0 0,9.3 0,10 0.7,10 10,0.7 \"/><polygon points=\"0.7,0 0,0 0,0.7 \"/></pattern>" +
"<!-- diagonal right small --><pattern id=\"diagonal-right-small\" patternUnits=\"userSpaceOnUse\" width=\"5\" height=\"5\" style=\"fill:rgba(0,0,0,0.3)\"><polygon points=\"5,4.3 4.3,5 5,5 \"/><polygon points=\"5,0 4.3,0 0,4.3 0,5 0.7,5 5,0.7 \"/><polygon points=\"0.7,0 0,0 0,0.7 \"/></pattern>" +
"<!-- horizontal large --><pattern id=\"horizontal-large\" patternUnits=\"userSpaceOnUse\" width=\"8\" height=\"8\" style=\"fill:rgba(0,0,0,0.3)\"><rect y=\"7\" width=\"8\" height=\"1\"/></pattern>" +
"<!-- horizontal small --><pattern id=\"horizontal-small\" patternUnits=\"userSpaceOnUse\" width=\"4\" height=\"4\" style=\"fill:rgba(0,0,0,0.3)\"><rect y=\"3\" width=\"4\" height=\"1\"/></pattern>" +
"<!-- vertical large --><pattern id=\"vertical-large\" patternUnits=\"userSpaceOnUse\" width=\"8\" height=\"8\" style=\"fill:rgba(0,0,0,0.3)\"><rect x=\"7\" y=\"0\" width=\"1\" height=\"8\"/></pattern>" +
"<!-- vertical small --><pattern id=\"vertical-small\" patternUnits=\"userSpaceOnUse\" width=\"4\" height=\"4\" style=\"fill:rgba(0,0,0,0.3)\"><rect x=\"3\" width=\"1\" height=\"4\"/></pattern></defs></svg>";

		sb.append(svg);

		outputData.addData(_OUTPUT_SVG_KEY, WebKeys.PAGE_TOP, sb);
	}

	private static final String _OUTPUT_CSS_KEY = BaseChartTag.class.getName() + "_CSS";
	private static final String _OUTPUT_SVG_KEY = BaseChartTag.class.getName() + "_SVG";

	private final String _moduleBaseName;

}
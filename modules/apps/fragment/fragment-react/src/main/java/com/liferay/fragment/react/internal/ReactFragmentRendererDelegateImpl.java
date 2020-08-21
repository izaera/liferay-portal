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

package com.liferay.fragment.react.internal;

import com.liferay.fragment.model.FragmentEntryLink;
import com.liferay.fragment.renderer.FragmentRendererDelegate;
import com.liferay.frontend.js.loader.modules.extender.npm.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.npm.ModuleNameUtil;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.portal.template.react.renderer.ComponentDescriptor;
import com.liferay.portal.template.react.renderer.ReactRenderer;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Collections;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Iván Zaera Avellón
 */
@Component(
	property = "fragmentType=react", service = FragmentRendererDelegate.class
)
public class ReactFragmentRendererDelegateImpl
	implements FragmentRendererDelegate {

	@Override
	public String renderFragmentEntryLink(
			FragmentEntryLink fragmentEntryLink,
			HttpServletRequest httpServletRequest)
		throws IOException {

		Writer writer = new CharArrayWriter();

		long fragmentEntryLinkId = fragmentEntryLink.getFragmentEntryLinkId();

		_reactRenderer.renderReact(
			new ComponentDescriptor(
				ModuleNameUtil.getModuleResolvedId(
					_jsPackage, "fragmentEntryLink/" + fragmentEntryLinkId),
				"fragment" + fragmentEntryLinkId, Collections.emptyList(),
				true),
			new HashMap<>(), httpServletRequest, writer);

		return writer.toString();
	}

	@Activate
	protected void activate() {
		_jsPackage = _npmResolver.getJSPackage();
	}

	private JSPackage _jsPackage;

	@Reference
	private NPMResolver _npmResolver;

	@Reference
	private ReactRenderer _reactRenderer;

}
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

package com.liferay.frontend.taglib.icon.servlet.taglib;

import com.liferay.petra.string.StringPool;
import com.liferay.taglib.BaseBodyTagSupport;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * @author Iván Zaera
 */
public class IconTag extends BaseBodyTagSupport {

	@Override
	public int doEndTag() throws JspException {
		JspWriter jspWriter = pageContext.getOut();

		try {
			jspWriter.write("<svg>");
			jspWriter.write("<use xlink:href=");
			jspWriter.write(StringPool.QUOTE);
			jspWriter.write(StringPool.POUND);
			jspWriter.write(getId());
			jspWriter.write(StringPool.QUOTE);
			jspWriter.write(" />");
			jspWriter.write("</svg>");
		}
		catch (IOException ioe) {
			throw new JspException(ioe);
		}

		return EVAL_PAGE;
	}

	public String getId() {
		return _id;
	}

	@Override
	public void release() {
		_id = null;

		super.release();
	}

	public void setId(String id) {
		_id = id;
	}

	private String _id;

}
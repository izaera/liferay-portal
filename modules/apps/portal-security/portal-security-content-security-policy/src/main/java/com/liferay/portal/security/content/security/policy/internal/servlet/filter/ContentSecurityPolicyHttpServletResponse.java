/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.content.security.policy.internal.servlet.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Olivér Kecskeméty
 */
public class ContentSecurityPolicyHttpServletResponse
	extends HttpServletResponseWrapper {

	public ContentSecurityPolicyHttpServletResponse(
		HttpServletResponse httpServletResponse) {

		super(httpServletResponse);

		_byteArrayOutputStream = new ByteArrayOutputStream(
			httpServletResponse.getBufferSize());
	}

	@Override
	public void flushBuffer() throws IOException {
		super.flushBuffer();

		if (_printWriter != null) {
			_printWriter.flush();
		}
		else if (_servletOutputStream != null) {
			_servletOutputStream.flush();
		}
	}

	public String getContent() throws IOException {
		if (_printWriter != null) {
			_printWriter.close();
		}
		else if (_servletOutputStream != null) {
			_servletOutputStream.close();
		}

		return _byteArrayOutputStream.toString(getCharacterEncoding());
	}

	@Override
	public ServletOutputStream getOutputStream() {
		if (_printWriter != null) {
			throw new IllegalStateException(
				"Get writer has already been called");
		}

		if (_servletOutputStream == null) {
			_servletOutputStream = new ServletOutputStream() {

				@Override
				public void close() throws IOException {
					_byteArrayOutputStream.close();
				}

				@Override
				public void flush() throws IOException {
					_byteArrayOutputStream.flush();
				}

				@Override
				public boolean isReady() {
					return _servletOutputStream.isReady();
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
					_servletOutputStream.setWriteListener(writeListener);
				}

				@Override
				public void write(int b) {
					_byteArrayOutputStream.write(b);
				}

			};
		}

		return _servletOutputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (_servletOutputStream != null) {
			throw new IllegalStateException(
				"Get output stream has already been called");
		}

		if (_printWriter == null) {
			_printWriter = new PrintWriter(
				new OutputStreamWriter(
					_byteArrayOutputStream, getCharacterEncoding()));
		}

		return _printWriter;
	}

	private final ByteArrayOutputStream _byteArrayOutputStream;
	private PrintWriter _printWriter;
	private ServletOutputStream _servletOutputStream;

}
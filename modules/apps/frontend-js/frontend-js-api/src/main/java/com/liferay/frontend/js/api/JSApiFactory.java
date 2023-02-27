package com.liferay.frontend.js.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface JSApiFactory<T> {

	public T createInstance(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse);

}

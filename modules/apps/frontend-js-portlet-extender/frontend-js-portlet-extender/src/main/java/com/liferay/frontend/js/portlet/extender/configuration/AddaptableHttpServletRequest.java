package com.liferay.frontend.js.portlet.extender.configuration;

import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class AddaptableHttpServletRequest extends HttpServletRequestWrapper {

  public AddaptableHttpServletRequest(HttpServletRequest request) {
    super(request);
  }

  public void addParameter(String name, String value) {
    params.put(name, value);
  }

  public String getParameter(String name) {
    // if we added one, return that one
    if (params.get(name) != null) {
      return params.get(name).toString();
    }
    // otherwise return what's in the original request
    HttpServletRequest req = (HttpServletRequest) super.getRequest();
    return req.getParameter(name);
  }

  private HashMap params = new HashMap();

}

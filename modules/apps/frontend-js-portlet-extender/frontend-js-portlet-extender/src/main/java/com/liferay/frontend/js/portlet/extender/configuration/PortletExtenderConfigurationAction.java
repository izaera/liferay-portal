package com.liferay.frontend.js.portlet.extender.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import java.io.IOException;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Douglas Prandini
 */
@Component(
	configurationPid = "PortletExtenderConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true,
	property = {"javax.portlet.name=" + "ReactPortlet"},
	service = ConfigurationAction.class
)
public class PortletExtenderConfigurationAction
	extends DefaultConfigurationAction {

	@Override
	  public void include(
	      PortletConfig portletConfig, HttpServletRequest request,
	      HttpServletResponse response)
	      throws Exception {

	    PortletContext portletContext = portletConfig.getPortletContext();

	    ServletContext servletContext = getServletContext(request);

	    ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);

	    PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

	    PortletConfig ReactPortletConfig = PortalUtil.getPortletConfig(themeDisplay.getCompanyId(), portletDisplay.getPortletResource(), servletContext);

	    RequestDispatcher requestDispatcher2 =
		servletContext.getRequestDispatcher(getJspPath(request));

	    request.setAttribute(
		PortletExtenderConfiguration.class.getName(),
		_portletExtenderConfiguration);

	    //Set Inputs attributes
	    request.setAttribute("configuration","test");

	    RequestDispatcher requestDispatcher = (RequestDispatcher) portletContext
		.getRequestDispatcher(getJspPath(request));

	    try {
	      requestDispatcher.include(request, response);
	    } catch (ServletException se) {
	      throw new IOException(
		  "Unable to include " + getJspPath(request), se);
	    }
	  }

	 @Override
	 public void processAction(
	      PortletConfig portletConfig, ActionRequest actionRequest,
	      ActionResponse actionResponse)
	      throws Exception {

	    String favoriteColor = ParamUtil.getString(actionRequest, "favoriteColor");
	    String favoriteColor3 = ParamUtil.getString(actionRequest, "favoriteColor3");

	    setPreference(actionRequest, "favoriteColor", favoriteColor);
	    setPreference(actionRequest, "favoriteColor3", favoriteColor3);

	    super.processAction(portletConfig, actionRequest, actionResponse);
  	}

	@Activate
	@Modified
	protected void activate(Map<Object, Object> properties) {
		_portletExtenderConfiguration = ConfigurableUtil.createConfigurable(
				PortletExtenderConfiguration.class, properties);
	}

	private volatile PortletExtenderConfiguration
			_portletExtenderConfiguration;

}

package com.liferay.frontend.js.portlet.extender.configuration;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.model.Portlet;
import com.liferay.portal.kernel.portlet.ConfigurationAction;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.portlet.PortletConfigFactory;
import com.liferay.portal.kernel.portlet.PortletConfigFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletContextFactoryUtil;
import com.liferay.portal.kernel.service.PortletLocalServiceUtil;
import com.liferay.portal.kernel.service.PortletServiceUtil;
import com.liferay.portal.kernel.service.persistence.PortletUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portlet.configuration.kernel.util.PortletConfigurationUtil;
import java.util.Dictionary;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
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
			PortletConfig portletConfig, HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws Exception {

		httpServletRequest.setAttribute(
			PortletExtenderConfiguration.class.getName(),
				_portletExtenderConfiguration);

		Dictionary<String, Object> properties = new HashMapDictionary<>();
		properties.put("config-jsp", "meu-grande-ovo.jsp");

		PortletConfigFactory portletConfigFactory = PortletConfigFactoryUtil.getPortletConfigFactory();
		String id = PortalUtil.getPortletId(httpServletRequest);
		Long longid = PortalUtil.getPlidFromPortletId(PortalUtil.getScopeGroupId(httpServletRequest), id);
		PortletConfig pf = portletConfigFactory.create(PortletLocalServiceUtil.getPortlet(longid),httpServletRequest.getServletContext());
		this.init();

		super.include(portletConfig, httpServletRequest, httpServletResponse);
	}

	@Override
	public void processAction(
			PortletConfig portletConfig, ActionRequest actionRequest,
			ActionResponse actionResponse)
		throws Exception {

		String favoriteColor = ParamUtil.getString(actionRequest, "favoriteColor");

		setPreference(actionRequest, "favoriteColor", favoriteColor);

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
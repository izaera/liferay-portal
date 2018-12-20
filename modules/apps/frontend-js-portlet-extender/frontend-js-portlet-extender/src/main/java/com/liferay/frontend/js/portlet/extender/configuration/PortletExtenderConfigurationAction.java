package com.liferay.frontend.js.portlet.extender.configuration;

import com.liferay.frontend.js.portlet.extender.internal.portlet.JSPortlet;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.DefaultConfigurationAction;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;

/**
 * @author Gustavo Mantuan
 */

public class PortletExtenderConfigurationAction
	extends DefaultConfigurationAction
	implements ManagedService {

	private static final String _CONFIGURATION = "configurationObject";
	private static final String _FORM_DATA = "formDate";
	private static final String _CONFIGURATION_TPL;
	private static final Log _log =
		LogFactoryUtil.getLog(PortletExtenderConfigurationAction.class);

	static {
		_CONFIGURATION_TPL = _loadTemplate("configuration.html.tpl");
	}

	private final String _name;

	public PortletExtenderConfigurationAction(String name) {
		_name = name;
	}

	private static String _loadTemplate(String name) {
		InputStream inputStream = JSPortlet.class.getResourceAsStream(
			"dependencies/" + name);

		try {
			return StringUtil.read(inputStream);
		}
		catch (Exception e) {
			_log.error("Unable to read template " + name, e);
		}

		return StringPool.BLANK;
	}

	@Override
	public void include(
		PortletConfig portletConfig, HttpServletRequest request,
		HttpServletResponse response)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		PortletDisplay portletDisplay = themeDisplay.getPortletDisplay();

		JSONObject jsonValues = JSONFactoryUtil.createJSONObject();

		try (InputStream configurationStream = getServletContext(request)
			.getResourceAsStream("META-INF/resources/configuration.json")) {

			String configurationString = StringUtil.read(configurationStream);
			JSONObject jsonObject =
				JSONFactoryUtil.createJSONObject(configurationString);

			request.setAttribute(_CONFIGURATION, jsonObject);

			PortletPreferences portletPreferences =
				PortletPreferencesFactoryUtil
					.getExistingPortletSetup(
						themeDisplay.getLayout(),
						portletDisplay.getPortletResource());
			portletPreferences.getMap().forEach((key, value) -> {
				jsonValues.put(key, value);
			});

			generateConfigurationFormFieldsByJson(portletDisplay, jsonObject,
				jsonValues, portletDisplay.getNamespace(),
				response.getWriter(),
				portletDisplay.getURLConfiguration().split("=")[0]);

		}
		catch (Exception e) {
			_log.error(
				"Unable to process configuration.json of " +
				portletDisplay.getPortletResource(),
				e);
		}
	}

	@Override
	public void processAction(
		PortletConfig portletConfig, ActionRequest actionRequest,
		ActionResponse actionResponse)
		throws Exception {

		String configuration =
			ParamUtil.getString(actionRequest, _CONFIGURATION);

		JSONObject jsonObject = JSONFactoryUtil
			.createJSONObject(
				configuration.substring(1, configuration.length()));

		Iterator<String> keys = jsonObject.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.get(key);

			if (value instanceof JSONObject) {
				String type = ((JSONObject) value).getString("type", "text");

				if ("select".equals(type) || "radio".equals(type) ||
					"checkbox".equals(type)) {
					String[] parameterValues =
						ParamUtil.getParameterValues(actionRequest, key);
					setPreference(actionRequest, key, parameterValues);
				}
				else {
					setPreference(
						actionRequest, key,
						ParamUtil.getString(actionRequest, key));
				}

			}
		}

		super.processAction(portletConfig, actionRequest, actionResponse);
	}

	@Override
	public void updated(Dictionary<String, ?> dictionary)
		throws ConfigurationException {
	}

	@Override
	protected Collection<PortletMode> getNextPossiblePortletModes(
		RenderRequest request) {
		return super.getNextPossiblePortletModes(request);
	}

	private String appendPortletName(String portletName, String whatToAppend) {
		return portletName + whatToAppend;
	}

	private void generateConfigurationFormFieldsByJson(
		PortletDisplay portletDisplay, JSONObject jsonObject,
		JSONObject jsonValues, String portletName, PrintWriter printWriter,
		String urlConfiguration) {
		printWriter.println(String.format(
			"<form class='form container container-no-gutters-sm-down container-view' "
			+ "method='post' id=\"%s\" name=\"%s\" "
			+ "data-fm-namespace=\"%s\">",
			appendPortletName(portletName, "fm"),
			appendPortletName(portletName, "fm"),
			portletName));

		printWriter.println(String.format(
			"<input class=\"field form-control\" "
			+ "id=\"%s\" name=\"%s\" type=\"hidden\" value=\"%s\"/>",
			appendPortletName(portletName, _FORM_DATA),
			appendPortletName(portletName, _FORM_DATA),
			System.currentTimeMillis()
		));

		printWriter.println(String.format(
			"<input class=\"field form-control\" "
			+ "id=\"%s\" name=\"%s\" type=\"hidden\" value='\"%s\"'/>",
			appendPortletName(portletName, _CONFIGURATION),
			appendPortletName(portletName, _CONFIGURATION),
			jsonObject.toString()
		));

		printWriter.println(String.format(
			"<input class=\"field form-control\" "
			+ "id=\"%s\" name=\"%s\" type=\"hidden\" value=\"%s\"/>",
			appendPortletName(portletName, Constants.CMD),
			appendPortletName(portletName, Constants.CMD),
			Constants.UPDATE
		));

		printWriter.println(
			StringUtil.replace(
				_CONFIGURATION_TPL,
				new String[]{
					"$PORTLET_ID", "$OBJECT_CONFIGURATION", "$OBJECT_VALUES",
					"$INSTANCE", "$URL_CONFIGURATION"},
				new String[]{
					portletName, jsonObject.toJSONString(),
					jsonValues.toJSONString(),
					portletDisplay.getPortletResource(), urlConfiguration}));

		printWriter.println("</form>");
		printWriter.flush();
	}
}

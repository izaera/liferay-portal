  <%@ page import="com.liferay.portal.kernel.util.StringPool" %>
    <%@
      page import="com.liferay.portal.kernel.util.Validator" %>

    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

    <%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

    <%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
    <%@
      taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
    <%@
      taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
    <%@
      taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
    <%@page
      import="com.liferay.frontend.js.portlet.extender.configuration.PortletExtenderConfiguration" %>

    <liferay-theme:defineObjects/>

    <portlet:defineObjects/>

      <%
PortletExtenderConfiguration configuration =
		(PortletExtenderConfiguration)
      renderRequest.getAttribute(PortletExtenderConfiguration.class.getName());

  String favoriteColor = configuration.favoriteColor();

%>
    <c:set value="<%= favoriteColor %>" var="favoriteColor"/>
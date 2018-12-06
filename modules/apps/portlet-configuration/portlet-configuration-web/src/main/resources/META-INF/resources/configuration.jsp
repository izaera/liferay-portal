  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@
      taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
    <%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
    <%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
    <%@taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>
    <%@taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>
    <%@taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
    <%@ page import="com.liferay.portal.kernel.util.Constants" %>
    <%@page import="com.liferay.portal.kernel.util.PortalUtil" %>
    <%@page import="java.util.HashMap" %>
    <%@page import="java.util.Map" %>
    <%@page import="java.util.Enumeration" %>
    <%@page import="java.util.EnumMap" %>
    <%@
      page import="com.liferay.portal.kernel.util.StringPool" %>
    <%@
      page import="com.liferay.portal.kernel.util.Validator" %>

    <liferay-theme:defineObjects/>

    <portlet:defineObjects/>

    <portlet:defineObjects/>

    <liferay-portlet:actionURL portletConfiguration="<%= true %>"
      var="configurationActionURL" />

    <liferay-portlet:renderURL portletConfiguration="<%= true %>"
      var="configurationRenderURL" />

      <%
         System.out.println(renderRequest.getParameter("mvcPath"));
         System.out.println(portletPreferences.getValue("favoriteColor", ""));

         String favoriteColor = (String) portletPreferences.getValue("favoriteColor", "");
         String favoriteColor3 = (String) portletPreferences.getValue("favoriteColor3", "");

         Enumeration x = request.getAttributeNames();

         while (x.hasMoreElements()) {
          String isbn = (String) x.nextElement();
          System.out.println(isbn);
          System.out.println(request.getAttribute(isbn));
         }

         System.out.println("aa");

         Enumeration x1 = renderRequest.getAttributeNames();

         while (x1.hasMoreElements()) {
          String isbn = (String) x1.nextElement();
          System.out.println(isbn);
          System.out.println(request.getAttribute(isbn));
         }

         System.out.println(request.getParameterMap());

         %>

    <div class="lfr-form-content" id="mainContent">

    <aui:form action="<%= configurationActionURL %>" method="post" name="fm">

      <aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>"/>

      <aui:input label="favoriteColor" name="favoriteColor" type="text" value="${favoriteColor}" />
      <aui:input label="favoriteColor2" name="favoriteColor3" type="text" value="${favoriteColor3}" />

      <aui:script>
        alert('a');
      </aui:script>


      <aui:button-row>
        <aui:button type="submit"></aui:button>
      </aui:button-row>
    </aui:form>

    </div>

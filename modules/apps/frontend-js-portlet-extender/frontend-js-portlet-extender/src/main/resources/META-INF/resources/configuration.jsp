  <%@ include file="/init.jsp" %>

    <liferay-portlet:actionURL
      portletConfiguration="<%= true %>"
      var="configurationActionURL"
    />

    <liferay-portlet:renderURL
      portletConfiguration="<%= true %>"
      var="configurationRenderURL"
    />


    <aui:form action="<%= configurationActionURL %>" method="post" name="fm">

      <aui:input name="favoriteColor" type="text"
        value="${favoriteColor}"
      />
    </aui:form>

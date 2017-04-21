package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.frontend.js.loader.modules.extender.internal.registry.PackageRegistry;
import com.liferay.frontend.js.loader.modules.extender.registry.JSBundle;
import com.liferay.frontend.js.loader.modules.extender.registry.JSModule;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackage;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;

@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.servlet.name=Serve Package Servlet",
		"osgi.http.whiteboard.servlet.pattern=/js/resolved-modules",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 1000)
	},
	service = {JSResolvedModulesServlet.class, Servlet.class}
)
public class JSResolvedModulesServlet extends HttpServlet {

	private static final long serialVersionUID = -2683080595698939805L;

	@Override
	protected void service(
			HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		response.setContentType(ContentTypes.TEXT_XML_UTF8);

		PrintWriter out = new PrintWriter(
			new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		Collection<JSBundle> jsBundles = _packageRegistry.getJSBundles();

		out.println("<resolved-modules>");

		Collection<JSModule> jsModules =
			_packageRegistry.getResolvedJSModules();

		for (JSModule jsModule : jsModules) {
			printModule(out, jsModule);
		}

		out.println("</resolved-modules>");

		out.close();
	}

	private void printModule(PrintWriter out, JSModule jsModule) {
		out.println("<module id=\"" + jsModule.getId() + "\">");

		out.println("<name>" + jsModule.getName() + "</name>");

		out.println("<url>" + jsModule.getResolvedURL() + "</url>");

		printDependencies(out, jsModule.getDependencies());

		out.println("</module>");
	}

	private void printDependencies(
		PrintWriter out, Collection<String> dependencies) {

		out.println("<dependencies>");

		for (String dependency : dependencies) {
			out.println("<dependency name=\"" + dependency + "\"/>");
		}

		out.println("</dependencies>");
	}

	@Reference(unbind = "-")
	protected void setPackageRegistry(PackageRegistry packageRegistry) {
		_packageRegistry = packageRegistry;
	}

	private transient PackageRegistry _packageRegistry;

}

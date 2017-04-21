package com.liferay.frontend.js.loader.modules.extender.internal;

import com.liferay.frontend.js.loader.modules.extender.internal.registry.PackageRegistry;
import com.liferay.frontend.js.loader.modules.extender.registry.JSBundle;
import com.liferay.frontend.js.loader.modules.extender.registry.JSModule;
import com.liferay.frontend.js.loader.modules.extender.registry.JSModuleAlias;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackage;
import com.liferay.frontend.js.loader.modules.extender.registry.JSPackageDependency;
import com.liferay.portal.kernel.util.ContentTypes;
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
		"osgi.http.whiteboard.servlet.name=JS Bundles Servlet",
		"osgi.http.whiteboard.servlet.pattern=/js/bundles",
		"service.ranking:Integer=" + (Integer.MAX_VALUE - 1000)
	},
	service = {JSBundlesServlet.class, Servlet.class}
)
public class JSBundlesServlet extends HttpServlet {

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

		out.println("<bundles>");

		for (JSBundle jsBundle : jsBundles) {
			printBundle(out, jsBundle);
		}

		out.println("</bundles>");

		out.close();
	}

	private void printBundle(PrintWriter out, JSBundle jsBundle) {
		out.println("<bundle id=\"" + jsBundle.getId() + "\">");
		out.println("<name>" + jsBundle.getName() + "</name>");
		out.println("<version>" + jsBundle.getVersion() + "</version>");

		Collection<JSPackage> jsPackages = jsBundle.getJSPackages();

		for (JSPackage jsPackage : jsPackages) {
			printPackage(out, jsPackage);
		}

		out.println("</bundle>");
	}

	private void printPackage(PrintWriter out, JSPackage jsPackage) {
		out.println("<package id=\"" + jsPackage.getId() + "\">");

		out.println("<name>" + jsPackage.getName() + "</name>");
		out.println("<version>" + jsPackage.getVersion() + "</version>");
		out.println("<main>" + jsPackage.getMain() + "</main>");

		printPackageDependencies(out, jsPackage.getJSPackageDependencies());

		printModuleAliases(out, jsPackage.getJSModuleAliases());

		printModules(out, jsPackage.getJSModules());

		Collection<JSModule> jsModules = jsPackage.getJSModules();

		out.println("</package>");
	}

	private void printPackageDependencies(
		PrintWriter out,
		Collection<JSPackageDependency> jsPackageDependencies) {

		out.println("<dependencies>");

		for (JSPackageDependency jsPackageDependency : jsPackageDependencies) {
			out.println(
				"<dependency name=\""+ jsPackageDependency.getName() +
				"\" version=\"" + jsPackageDependency.getVersionConstraints() +
				"\" />");
		}

		out.println("</dependencies>");
	}

	private void printModuleAliases(
		PrintWriter out, Collection<JSModuleAlias> jsModuleAliases) {

		out.println("<module-aliases>");

		for (JSModuleAlias jsModuleAlias : jsModuleAliases) {
			out.println(
				"<module-alias name=\""+ jsModuleAlias.getName() +
				"\" alias=\"" + jsModuleAlias.getAlias() + "\" />");
		}

		out.println("</module-aliases>");
	}

	private void printModules(PrintWriter out, Collection<JSModule> jsModules) {
		out.println("<modules>");

		for (JSModule jsModule : jsModules) {
			out.println("<module id=\"" + jsModule.getId() + "\">");

			out.println("<name>" + jsModule.getName() + "</name>");
			out.println("<url>" + jsModule.getURL() + "</url>");

			printDependencies(out, jsModule.getDependencies());

			out.println("</module>");
		}

		out.println("</modules>");
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

package org.ehoffman.embedded.jetty;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.ehoffman.webapp.invoker.Application;
import org.ehoffman.webapp.invoker.ApplicationUtil;
import org.ehoffman.webapp.invoker.lookups.ApplicationLookUpMethod;
import org.ehoffman.webapp.invoker.lookups.EclipseProjectWithMarkingProperty;
import org.testng.annotations.Test;


public class TestJettyStartUp {

	@Test()
	public void runJetty() throws Exception {
		List<Class<? extends ApplicationLookUpMethod>> methods = new ArrayList<Class<? extends ApplicationLookUpMethod>>();
		methods.add(EclipseProjectWithMarkingProperty.class);
	  Application application = ApplicationUtil.discoverApplicationByName(methods, "cr");
		Server server = ApplicationUtil.runApplicationOnOwnServer(application);
		server.join();
	}
	
}

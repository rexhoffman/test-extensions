package org.ehoffman.embedded.jetty;

import org.eclipse.jetty.server.Server;
import org.ehoffman.webapp.invoker.Application;
import org.ehoffman.webapp.invoker.ApplicationUtil;
import org.testng.annotations.Test;


public class TestJettyStartUp {

	@Test()
	public void runJetty() throws Exception {
		Application application = ApplicationUtil.discoverApplicationByName("EmbeddedWebapp");
		Server server = ApplicationUtil.runApplicationOnOwnServer(application);
		server.join();
	}
	
}

package org.ehoffman.webapp.invoker;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ApplicationUtil {	
	
	public static List<Application> discoverApplications() {
		Enumeration<URL> urls = null;
		try {
		  urls = ApplicationUtil.class.getClassLoader().getResources("webapp.properties");
		} catch (IOException io_exception){
			throw new RuntimeException("could not determine file of resource, while try to calculate project location", io_exception);
		}
		List<Application> applications = new ArrayList<Application>();
		for (URL url : Collections.list(urls)){
			try {
				System.out.println("started by "+url);
				File basetxt = new File(url.toURI());
				Properties props = new Properties();
				props.load(new FileReader(basetxt));
		    File projectBase =  basetxt.getParentFile().getParentFile().getParentFile();
		    System.out.println("running for "+projectBase);
		    applications.add(new Application(projectBase, props));
			} catch (URISyntaxException exception){
				throw new RuntimeException("could not determine file of resource, while try to calculate project location");
			} catch (IOException io_exception){
				throw new RuntimeException("could not determine file of resource, while try to calculate project location", io_exception);
			}
		}
		return applications;
	}
	
	public static Application discoverApplicationByName(String displayName){
		for (Application application : discoverApplications()){
			if (displayName != null && displayName.equals(application.getDisplayName())){
				return application;
			}
		}
		return null;
	}
	
	public static Server runApplicationOnOwnServer(Application application) throws Exception {
		Server server = new Server(0);
		WebAppContext context = new WebAppContext();
		System.out.println("WebXml "+application.getWebXml().toString());
		context.setDescriptor(application.getWebXml().toString());
		context.setResourceBase(application.getWebContentDirs().get(0).toString());
		context.setContextPath("/"+application.getContextRoot());
		context.setParentLoaderPriority(true);
		server.setHandler(context);
		server.start();
		return server;
	}

}

package org.ehoffman.webapp.invoker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.ehoffman.webapp.invoker.lookups.ApplicationLookUpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationUtil {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

  /**
   * Used to detect applications that may be run or accessed from a test, either in IDE, or via command line.
   * 
   * @param classes the list order determines order of precedence, the first @{ApplicationLookUpMethod} to find an Application with a matching name, will be used if that application is used.
   * @return A set of Applications, ready to be passed to {@link #runApplicationOnOwnServer(Application)}
   */
  public static Set<Application> discoverApplications(List<Class<? extends ApplicationLookUpMethod>> classes) {
    Set<Application> applications = new HashSet<Application>();
    for (Class<? extends ApplicationLookUpMethod> clazz : classes){
      ApplicationLookUpMethod lookUpMethod = null;
      try {
        lookUpMethod = clazz.newInstance();
      } catch (IllegalAccessException e){
        throw new RuntimeException(e);
      } catch (InstantiationException e){
        throw new RuntimeException(e);
      }
      applications.addAll(lookUpMethod.lookup()); //set addAll skips duplicates (which in the case of applications is based on the context root)
    }
    return applications;
  }


  /**
   * Used to find a specific applications that may be run or accessed from a test, either in IDE, or via command line.
   * 
   * @param classes the list order determines order of precedence, the first @{ApplicationLookUpMethod} to find an Application with a matching name, will be used if that application is used.
   * @return An application, ready to be passed to {@link #runApplicationOnOwnServer(Application)}
   */
  public static Application discoverApplicationByName(List<Class<? extends ApplicationLookUpMethod>> classes, String contextRoot){
    for (Class<? extends ApplicationLookUpMethod> clazz : classes){
      ApplicationLookUpMethod lookUpMethod = null;
      try {
        lookUpMethod = clazz.newInstance();
        Application application = lookUpMethod.lookupByName(contextRoot);
        if (application != null){
          return application;
        }
      } catch (IllegalAccessException e){
        throw new RuntimeException(e);
      } catch (InstantiationException e){
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  public static Server runApplicationOnOwnServer(Application application) throws Exception {
    Server server = new Server(0);
    WebAppContext context = new WebAppContext();
    logger.info("App is "+(application.isExploded()?"":"not ")+"exploded");
    if (application.isExploded()){
      logger.info("WebXml %s", application.getWebXml().toString());
      context.setDescriptor(application.getWebXml().toString());
      context.setResourceBase(application.getWebContentDirs().get(0).toString());
      context.setParentLoaderPriority(true);
    } else {
      context.setWar(application.getWarFile().getAbsolutePath());
      context.setParentLoaderPriority(false);
    }
    server.setHandler(context);
    context.setContextPath("/"+application.getContextRoot());
    server.start();
    return server;
  }

}

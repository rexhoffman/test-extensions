package org.ehoffman.webapp.invoker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ehoffman.webapp.invoker.lookups.ApplicationLookUpMethod;
import org.ehoffman.webapp.invoker.lookups.EclipseProjectWithMarkingProperty;
import org.ehoffman.webapp.invoker.lookups.ScanTargetDir;

public class ApplicationUtil {

  //private static final Logger logger = LoggerFactory.getLogger(ApplicationUtil.class);

  private static List<Class<? extends ApplicationLookUpMethod>> lookupMethods = new ArrayList<Class<? extends ApplicationLookUpMethod>>();
  static {
    if (System.getProperty("java.class.path").contains("org.testng.eclipse") || System.getProperty("java.class.path").contains("idea_rt")){ //running in eclipse with testng, or idea
      lookupMethods.add(EclipseProjectWithMarkingProperty.class);
    }
    lookupMethods.add(ScanTargetDir.class);
  }

  /**
   * Used to detect applications that may be run or accessed from a test, either in IDE, or via command line.
   * 
   * @param classes the list order determines order of precedence, the first @{ApplicationLookUpMethod} to find an Application with a matching name, will be used if that application is used.
   * @return A set of Applications, ready to be passed to {@link #runApplicationOnOwnServer(Application)}
   */
  public static Set<Application> discoverApplications() {
    return discoverApplications(lookupMethods);
  }

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
  public static Application discoverApplicationByName(String contextRoot){
    return discoverApplicationByName(lookupMethods, contextRoot);
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
}

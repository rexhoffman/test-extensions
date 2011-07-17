package org.ehoffman.webapp.invoker.lookups;

import java.util.List;

import org.ehoffman.webapp.invoker.Application;


/**
 * All implementations must have a public, parameterless constructor.
 * 
 * @author rexhoffman
 */
public interface ApplicationLookUpMethod {

  /**
   * 
   * @return
   */
  List<Application> lookup();
  
  /**
   * 
   * @param contextRoot
   * @return
   */
  Application lookupByName(String contextRoot);
}

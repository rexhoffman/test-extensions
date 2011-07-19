package org.ehoffman.testing.module.webapp;

import java.net.URL;

public interface Application {

  public URL getDefaultRootUrl();

  public URL getSecureRootUrl();

  public String getName();

}

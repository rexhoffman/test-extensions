package org.ehoffman.testing.module.webapp;

import java.util.Map;

import org.ehoffman.module.Module;

public abstract class WebAppModule implements Module<Application> {
  
  public abstract String getWebAppName();
  //private static Server
  
  @Override
  public String getModuleType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    return getWebAppName();
  }

  @Override
  public Class<? extends Application> getTargetClass() {
    return Application.class;
  }

  @Override
  public Map<String, Class<?>> getDependencyDefinition() {
    return null;
  }

  @Override
  public Application create(Map<String, ?> dependencies) {
    return null;
  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub
    
  }

}

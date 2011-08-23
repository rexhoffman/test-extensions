package org.ehoffman.testing.module.webapp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.ehoffman.module.Module;
import org.ehoffman.webapp.invoker.ApplicationUtil;

public abstract class WebAppModule implements Module<Application> {

  private static Map<Class<? extends WebAppModule>, JettyApplication> jettyApplicationMap = new HashMap<Class<? extends WebAppModule>, WebAppModule.JettyApplication>();

  public abstract String getWebAppName();

  @Override
  public String getModuleType() {
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

  private static class JettyApplication implements Application {
    private final org.ehoffman.webapp.invoker.Application application;

    protected JettyApplication(org.ehoffman.webapp.invoker.Application application){
      this.application = application;
      this.application.start();
    }

    @Override
    public URL getSecureRootUrl() {
      return this.application.getSecureRootUrl();
    }

    @Override
    public URL getDefaultRootUrl() {
      return this.application.getDefaultRootUrl();
    }

    @Override
    public String getName() {
      return this.application.getContextRoot();
    }

    public void shutdown(){
      this.application.shutdown();
    }

  }

  @Override
  public Application create(Map<String, ?> dependencies) {
    JettyApplication jettyApplication = new JettyApplication(ApplicationUtil.discoverApplicationByName(getWebAppName()));
    jettyApplicationMap.put(this.getClass(), jettyApplication);
    return jettyApplication;
  }

  @Override
  public void destroy() {
    JettyApplication application =jettyApplicationMap.get(this.getClass());
    if (application != null){
      application.shutdown();
    }
  }

}

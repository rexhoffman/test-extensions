package org.ehoffman.testing.module.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ehoffman.module.ModuleGroup;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.module.PooledModule;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.HttpCommandExecutor;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class WebDriverModule implements ModuleProvider<RemoteWebDriverInterface> {

  private static boolean useGrid = false;
  
  /**
   * See {@link HttpCommandExecutor#HttpCommandExecutor(URL)} it hard codes this system property.
   * 
   * Hard coding it here in case it changes in later versions.
   * 
   */
  private static final String webDriverSystemPropertyForGridUrl = "webdriver.remote.server";
  
  static {
    try {
      URL gridLocation = new URL(System.getProperty(webDriverSystemPropertyForGridUrl));
      if (gridLocation != null) useGrid = true;
      System.out.println("grid location: "+gridLocation.toString());
    } catch (MalformedURLException e) {}
  }
  
  @Override
  public String getModuleType() {
    return WebDriverModule.class.getSimpleName();
  }
  
  public List<Class<? extends ModuleProvider<?>>> rightOneInList(Class<? extends ModuleProvider<?>> grid, Class<? extends ModuleProvider<?>> local) {
    List<Class<? extends ModuleProvider<?>>> provider = new ArrayList<Class<? extends ModuleProvider<?>>>();
    provider.add(useGrid?grid:local);
    return provider;
  }
  
  public static class Firefox extends WebDriverModule implements ModuleGroup<RemoteWebDriverInterface> {
    @Override
    public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
      return rightOneInList(WebDriverGridModule.Firefox.class, WebDriverLocalModule.Firefox.class);
    }
  }
  
  public static class Chrome extends WebDriverModule implements ModuleGroup<RemoteWebDriverInterface> {
    @Override
    public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
      return rightOneInList(WebDriverGridModule.Chrome.class, WebDriverLocalModule.Chrome.class);
    }
  }

  public static class HtmlUnitFirefox extends WebDriverModule implements ModuleGroup<RemoteWebDriverInterface> {
    @Override
    public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
      //this seems kind of pointless, why not just use local always?
      return rightOneInList(WebDriverGridModule.HtmlUnitFirefox.class, WebDriverLocalModule.HtmlUnitFirefox.class);
    }
  }

  public static class HtmlUnitIE6 extends WebDriverModule implements ModuleGroup<RemoteWebDriverInterface> {
    @Override
    public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
      //this seems kind of pointless, why not just use local always?
      return rightOneInList(WebDriverGridModule.HtmlUnitIE.class, WebDriverLocalModule.HtmlUnitIE6.class);
    }
  }
  

}

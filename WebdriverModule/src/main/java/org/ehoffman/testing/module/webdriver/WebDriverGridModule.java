package org.ehoffman.testing.module.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.ehoffman.module.ModuleProvider;
import org.ehoffman.module.PrototypeModule;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebDriverGridModule implements ModuleProvider<WebDriver> {

  private static Logger logger = LoggerFactory.getLogger(WebDriverGridModule.class);
  
  /**
   * See {@link HttpCommandExecutor#HttpCommandExecutor(URL)} it hard codes this system property.
   * 
   * Hard coding it here in case it changes in later versions.
   * 
   */
  private static final String webDriverSystemPropertyForGridUrl = "webdriver.remote.server";
  
  private static URL GRID_LOCATION;
  
  static {
    try {
      GRID_LOCATION = new URL(System.getProperty(webDriverSystemPropertyForGridUrl));
      System.out.println("grid location: "+GRID_LOCATION.toString());
	} catch (MalformedURLException e) {}
  }

  protected WebDriver getRemoteWithVersion(DesiredCapabilities dc, String version){
    if (version != null && !"".equals(version)){
      dc.setVersion(version);
    }
    //System.out.println("About to create driver on " +GRID_LOCATION+ " with dc of "+dc);
    WebDriver driver = new RemoteWebDriver(GRID_LOCATION, dc);
    System.out.println("About to augment driver" +driver);
    if (driver != null) {
      driver = new Augmenter().augment( driver );
    }
    return driver;
  }
  
  public static class IE6 extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.internetExplorer(), "6");
    }
  }
  public static class IE7 extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.internetExplorer(), "7");
    }
  }
  public static class IE8 extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.internetExplorer(), "8");
    }
  }
  public static class IE9 extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.internetExplorer(), "9");
    }
  }

  public static class Chrome extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      DesiredCapabilities dc = DesiredCapabilities.chrome();
      dc.setCapability("chrome.switches", Arrays.asList("--disable-popup-blocking"));
      return getRemoteWithVersion(dc, null);
    }
  }
  
  public static class Firefox extends WebDriverGridModule implements PrototypeModule<WebDriver> {
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.firefox(), null);
    }
  }  

  public static class Firefox36 extends WebDriverGridModule implements PrototypeModule<WebDriver> {
	@Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.firefox(), "3.6");
	}
  }

  public static class Firefox6 extends WebDriverGridModule implements PrototypeModule<WebDriver> {
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.firefox(), "6");
    }
  }
  
  public static class Firefox7 extends WebDriverGridModule implements PrototypeModule<WebDriver> {
	@Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.firefox(), "7");
    }
  }

  public static class Firefox8 extends WebDriverGridModule implements PrototypeModule<WebDriver> {
    @Override
    public WebDriver getDriver() {
      return getRemoteWithVersion(DesiredCapabilities.firefox(), "8");
    }
  }

  
  public static class HtmlUnitFirefox extends WebDriverGridModule implements PrototypeModule<WebDriver> {
    @Override
    public WebDriver getDriver() {
      DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
      dc.setBrowserName("firefox");
      dc.setJavascriptEnabled(true);
      return getRemoteWithVersion(dc, null);
    }
  }

  public static class HtmlUnitIE extends WebDriverGridModule implements PrototypeModule<WebDriver> {
    @Override
    public WebDriver getDriver() {
      DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
      dc.setBrowserName("internet explorer");
      dc.setJavascriptEnabled(true);
      return getRemoteWithVersion(dc, null);
    }
  }
  
  public static class Android extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver() {
      DesiredCapabilities dc = DesiredCapabilities.android();
      dc.setPlatform(Platform.LINUX);
      return getRemoteWithVersion(dc, null);
    }
  }
  public static class IPhone extends WebDriverGridModule implements PrototypeModule<WebDriver> { 
    @Override
    public WebDriver getDriver()  {
      DesiredCapabilities dc = DesiredCapabilities.iphone();
      return getRemoteWithVersion(dc, null);
    }
  }
  
  public WebDriver getDriver() {
    throw new RuntimeException("Must use a subclass of this class");
  }
  
  public String getName() {
    return this.getClass().getSimpleName();
  }

  public Class<? extends RemoteWebDriverInterface> getTargetClass() {
    return RemoteWebDriverInterface.class;
  }

  public Map<String, Class<?>> getDependencyDefinition() {
    return null;
  }

  public WebDriver create(Map<String, ?> dependencies) {
    return getDriver();
  }
  
  public void destroy() {
  }

  public String getModuleType() {
    return WebDriverModule.class.getSimpleName();
  }

}

package org.ehoffman.testing.module.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.ehoffman.module.ModuleProvider;
import org.ehoffman.module.PooledModule;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverGridModule implements ModuleProvider<RemoteWebDriverInterface> {

  // Make this a system variable or something?
  private static URL GRID_LOCATION;
  
  static {
    try {
      GRID_LOCATION = new URL(System.getProperty("seleniumhub"));
      System.out.println("grid location: "+GRID_LOCATION.toString());
	} catch (MalformedURLException e) {}
  }

  public static class IE6 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
      dc.setVersion("6");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class IE7 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
      dc.setVersion("7");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class IE8 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
      dc.setVersion("8");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class IE9 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
      dc.setVersion("9");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class Chrome extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.chrome();
      dc.setCapability("chrome.switches", Arrays.asList("--disable-popup-blocking"));
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }

  public static class Firefox extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
	@Override
	public Object makeObject() throws Exception {
	  return new RemoteWebDriver(GRID_LOCATION, DesiredCapabilities.firefox());
    }
  }

  public static class Firefox36 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
	@Override
	public Object makeObject() throws Exception {
	  DesiredCapabilities dc = DesiredCapabilities.firefox();
	  dc.setVersion("3.6");
	  return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }

  public static class Firefox6 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.firefox();
      dc.setVersion("6");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  
  public static class Firefox7 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
	@Override
	public Object makeObject() throws Exception {
	  DesiredCapabilities dc = DesiredCapabilities.firefox();
	  dc.setVersion("7");
	  return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }

  public static class Firefox8 extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.firefox();
      dc.setVersion("8");
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }

  
  public static class HtmlUnitFirefox extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
      dc.setBrowserName("firefox");
      dc.setJavascriptEnabled(true);
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }

  public static class HtmlUnitIE extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.htmlUnit();
      dc.setBrowserName("internet explorer");
      dc.setJavascriptEnabled(true);
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class Android extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.android();
      dc.setPlatform(Platform.LINUX);
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
  }
  public static class IPhone extends WebDriverGridModule implements PooledModule<RemoteWebDriverInterface> { 
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.iphone();
      return new RemoteWebDriver(GRID_LOCATION, dc);
    }
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

  public RemoteWebDriverInterface create(Map<String, ?> dependencies) {
    //not used... we're pooling
    return null;
  }

  public void destroy() {
  }

  public String getModuleType() {
    return RemoteWebDriverInterface.class.getSimpleName();
  }

  public void destroyObject(Object obj) throws Exception {
    WebDriver driver = (WebDriver) obj;
    driver.quit();
  }

  public boolean validateObject(Object obj) {
    return true;
  }

  public void activateObject(Object obj) throws Exception {
  }

  public void passivateObject(Object obj) throws Exception {
    WebDriver driver = (WebDriver) obj;
    String currentHandler = driver.getWindowHandle();
    for (String handle : driver.getWindowHandles()){
      if (!handle.equals(currentHandler)){
        driver.switchTo().window(handle);
        driver.close();
      }
    }
    driver.switchTo().window(currentHandler);
  }
  
  public int getMaxPoolElements() {
	return 10;
}

}

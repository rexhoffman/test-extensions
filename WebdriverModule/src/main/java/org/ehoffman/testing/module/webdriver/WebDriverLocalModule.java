package org.ehoffman.testing.module.webdriver;

import java.util.Arrays;
import java.util.Map;

import org.ehoffman.module.ModuleProvider;
import org.ehoffman.module.PooledModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class WebDriverLocalModule implements ModuleProvider<RemoteWebDriverInterface> {

  public static class Chrome extends WebDriverLocalModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      DesiredCapabilities dc = DesiredCapabilities.chrome();
      dc.setCapability("chrome.switches", Arrays.asList("--disable-popup-blocking"));
      return new ChromeDriver(dc);
    }
  }

  public static class Firefox extends WebDriverLocalModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      FirefoxDriver driver = new FirefoxDriver();
      return driver;
    }
  }

  public static class HtmlUnitFirefox extends WebDriverLocalModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.FIREFOX_3_6);
      driver.setJavascriptEnabled(true);
      return driver;
    }
  }

  public static class HtmlUnitIE6 extends WebDriverLocalModule implements PooledModule<RemoteWebDriverInterface> {
    @Override
    public Object makeObject() throws Exception {
      HtmlUnitDriver driver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER_6);
      driver.setJavascriptEnabled(true);
      return driver;
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
    return WebDriverModule.class.getSimpleName();
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
    return 3;
  }
}

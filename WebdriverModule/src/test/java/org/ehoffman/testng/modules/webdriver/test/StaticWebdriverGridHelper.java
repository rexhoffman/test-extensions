package org.ehoffman.testng.modules.webdriver.test;

import static org.fest.assertions.Assertions.*;

import org.ehoffman.testing.module.webdriver.WebDriverGridModule;
import org.ehoffman.testing.module.webdriver.WebDriverHubModule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.Test;

public class StaticWebdriverGridHelper {

  @Test
  public void basicGridTest() throws Exception {
    try {
      System.setProperty("hubport", "4444");
      System.setProperty("seleniumhub", "http://localhost:4444/wd/hub");

      WebDriverHubModule.lauchGrid();
      WebDriverHubModule.lauchNode("http://localhost:4444/grid");
      WebDriverGridModule.IE6 module = new WebDriverGridModule.IE6();
      WebDriver driver = (WebDriver) module.makeObject();
      assertThat(driver).isNotNull();
      driver.close();
      WebDriverHubModule.stopRemote();
      WebDriverHubModule.stopHub();
    } catch (WebDriverException e) {
      assertThat(e.getMessage()).contains("Empty pool of VM for setup {platform=WINDOWS, ensureCleanSession=true, browserName=internet explorer, version=6}");
    }
  }

}

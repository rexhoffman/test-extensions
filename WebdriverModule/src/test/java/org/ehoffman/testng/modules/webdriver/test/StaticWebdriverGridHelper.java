package org.ehoffman.testng.modules.webdriver.test;

import static org.fest.assertions.Assertions.*;

import java.net.BindException;

import org.ehoffman.testing.module.webdriver.WebDriverGridModule;
import org.ehoffman.testing.module.webdriver.WebDriverHubModule;
import org.openqa.grid.common.exception.CapabilityNotPresentOnTheGridException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StaticWebdriverGridHelper {

  @BeforeClass
  public void startHub() {
    System.setProperty("hubport", "4444");
    System.setProperty("seleniumhub", "http://localhost:4444/wd/hub");
    try {
      WebDriverHubModule.lauchGrid();
      WebDriverHubModule.lauchNode("http://localhost:4444/grid");
    } catch (Exception e) {
      // if this fails, it better be because the hub is already running.
      assertThat(e).isExactlyInstanceOf(BindException.class).hasMessage("Address already in use");
    }
  }

  @AfterClass
  public void stopHub() throws Exception {
    WebDriverHubModule.stopRemote();
    WebDriverHubModule.stopHub();
  }
  
  @Test
  public void missingIE6GridTest() throws Exception {
    try {
      WebDriverGridModule.IE6 module = new WebDriverGridModule.IE6();
      WebDriver driver = (WebDriver) module.makeObject();
      assertThat(driver).isNotNull();
      driver.close();
      assertThat(true).as("should not be reachable").isFalse();
    } catch (Throwable t) {
      assertThat(t).isExactlyInstanceOf(org.openqa.selenium.WebDriverException.class);
    }
  }

  @Test
  public void basicGridTest() throws Exception {
    //TODO: fix this (auto installer?)
    WebDriverGridModule.Firefox6 module = new WebDriverGridModule.Firefox6();
    WebDriver driver = (WebDriver) module.makeObject();
    assertThat(driver).isNotNull();
    driver.close();
  }

}

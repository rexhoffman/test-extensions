package org.ehoffman.testng.modules.webdriver.test;

import static org.fest.assertions.Assertions.*;

import java.net.BindException;

import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testing.module.webdriver.WebDriverGridModule;
import org.ehoffman.testing.module.webdriver.StaticWebdriverGridHelper;
import org.ehoffman.testing.module.webdriver.WebDriverModule;
import org.ehoffman.testng.extensions.Fixture;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyEnforcer.class)
public class WebdriverGridTest {


  @BeforeSuite
  public void startHub() {
    System.setProperty("hubport", "4444");
    System.setProperty("webdriver.remote.server", "http://localhost:4444/wd/hub");
    try {
      StaticWebdriverGridHelper.lauchGrid();
      StaticWebdriverGridHelper.lauchNode("http://localhost:4444/grid");
    } catch (Exception e) {
      // if this fails, it better be because the hub is already running.
      e.printStackTrace();
      assertThat(e).isExactlyInstanceOf(BindException.class).hasMessage("Address already in use");
    }
  }

  @AfterSuite
  public void stopHub() throws Exception {
    StaticWebdriverGridHelper.stopRemote();
    StaticWebdriverGridHelper.stopHub();
  }

  @Test(groups = { "unit" })
  public void missingIE6GridTest() throws Exception {
    WebDriver driver = null;
    try {
      WebDriverGridModule.IE6 module = new WebDriverGridModule.IE6();
      driver = (WebDriver) module.makeObject();
      assertThat(driver.getCurrentUrl()).isNotNull();
      assertThat(true).as("should not be reachable").isFalse();
    } catch (Throwable t) {
      assertThat(t).isExactlyInstanceOf(org.openqa.selenium.WebDriverException.class);
    } finally {
      if (driver != null) driver.quit();
    }
  }

  @Test(groups = { "unit" })
  public void basicGridTest() throws Throwable {
    // TODO: fix this (auto installer?)
    WebDriver driver = null;
    try {
      WebDriverGridModule.Firefox module = new WebDriverGridModule.Firefox();
      driver = (WebDriver) module.makeObject();
      assertThat(driver).isNotNull();
    } catch (Throwable t) {
      throw t;
    } finally {
      if (driver != null) driver.quit();
    }
  }

  @Test(groups = { "unit" })
  @Fixture(factory = { WebDriverModule.Firefox.class /* , WebDriverModule.Chrome.class ... need to work around timeout exception */})
  public void testCanTakeScreenShotThroughGrid() throws Throwable {
    WebDriver driver = null;
    try {
      driver = FixtureContainer.getService(WebDriverGridModule.class);
      driver.get("http://www.google.com");
      byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    } catch (Throwable t) {
      throw t;
    }
  }

}
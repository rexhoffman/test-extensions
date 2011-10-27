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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyEnforcer.class)
public class WebdriverGridTest {
  Logger logger = LoggerFactory.getLogger(WebdriverGridTest.class);

  @BeforeSuite(groups="unit")
  public void startHub() {
    logger.info("\n\nStarting hub\n\n");
    System.setProperty("hubport", "4444");
    System.setProperty("webdriver.remote.server", "http://localhost:4444/wd/hub");
    try {
      StaticWebdriverGridHelper.lauchGrid();
      StaticWebdriverGridHelper.lauchNode("http://localhost:4444/grid");
      logger.info("\n\nStarted hub\n\n");
    } catch (Exception e) {
      // if this fails, it better be because the hub is already running.
      logger.info("\n\nFailed to start hub\n\n",e);
      assertThat(e).isExactlyInstanceOf(BindException.class).hasMessage("Address already in use");
    }
  }

  @AfterSuite(groups="unit")
  public void stopHub() throws Exception {
    try {
      logger.info("\n\nStopping hub\n\n");
      StaticWebdriverGridHelper.stopRemote();
      StaticWebdriverGridHelper.stopHub();
      logger.info("\n\nStopped hub\n\n");
    } catch (Exception e) {
      logger.info("\n\nFailed to stop hub\n\n",e);
      throw e;
    }
  }

  @Test(groups = { "unit" })
  public void missingIE6GridTest() throws Exception {
    logger.info("\n\nStarting missingIE6GridTest\n\n");
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
      logger.info("\n\nFinished missingIE6GridTest\n\n");
    }
  }

  @Test(groups = { "unit" })
  public void basicGridTest() throws Throwable {
    logger.info("\n\nStarting basicGridTest\n\n");
    // TODO: fix this (auto installer?)
    WebDriver driver = null;
    try {
      WebDriverGridModule.Firefox module = new WebDriverGridModule.Firefox();
      logger.info("\n\nbasicGridTest, getting driver instance\n\n");
      driver = (WebDriver) module.makeObject();
      logger.info("\n\nbasicGridTest, got driver instance\n\n");
      assertThat(driver).isNotNull();
    } catch (Throwable t) {
      throw t;
    } finally {
      logger.info("\n\nbasicGridTest, quit driver instance\n\n");
      if (driver != null) driver.quit();
      logger.info("\n\nFinished basicGridTest\n\n");
    }
  }

  @Test(groups = { "unit" })
  @Fixture(factory = { WebDriverModule.Firefox.class /* , WebDriverModule.Chrome.class ... need to work around timeout exception */})
  public void testCanTakeScreenShotThroughGrid() throws Throwable {
    logger.info("\n\nStarting testCanTakeScreenShotThroughGrid\n\n");
    WebDriver driver = null;
    try {
      driver = FixtureContainer.getService(WebDriverGridModule.class);
      driver.get("http://www.google.com");
      byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    } catch (Throwable t) {
      throw t;
    } finally {
      logger.info("\n\nFinished testCanTakeScreenShotThroughGrid\n\n");
    }
  }

}
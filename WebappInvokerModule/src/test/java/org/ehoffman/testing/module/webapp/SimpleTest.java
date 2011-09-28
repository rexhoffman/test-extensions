package org.ehoffman.testing.module.webapp;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;

import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testing.module.webdriver.WebDriverModule;
import org.ehoffman.testng.extensions.Fixture;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyEnforcer.class)
public class SimpleTest {

  @Test(groups="functional")
  @Fixture(factory={EmbeddedWebappModule.class, WebDriverModule.Firefox.class, WebDriverModule.HtmlUnitIE6.class})
  public void simpleTestOfApplication(){
    WebDriver driver = FixtureContainer.getService(WebDriverModule.class);
    Application application = FixtureContainer.getService(EmbeddedWebappModule.class);
    driver.navigate().to(application.getDefaultRootUrl());
    assertThat(driver.findElement(By.id("message"))).isDisplayed().textContains("the time is now : ");
  }
}

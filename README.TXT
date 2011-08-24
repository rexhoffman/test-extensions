Pretty straight forward maven projects, build with pom.xml and a maven 3 install:

End results: you'll be able to easily write tests that start up web applications in jetty (either by detecting a marking webapp.properties file in an open webapp project and running it exploded, or by scanning the target/ dir for .war files.

The webapp is left open for all tests that happen to use it during the execution, whether driven by a right-click "run as" testng test in eclipse, or via maven surefire runner.

There is equivalent support for Webdriver, where browser instances are pooled and tests are run multithreaded.

So here is an example test:



package org.ehoffman.testing.module.webapp;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;

import org.ehoffman.testing.module.webdriver.WebDriverModule;
import org.ehoffman.testng.extensions.Fixture;
import org.ehoffman.testng.extensions.modules.FixtureContainer;
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


So this test is actually run twice, once in Firefox, and once in HtmlUnit (because those modules both have a "getModuleType" method that returns the string "WebDriver") after starting up the app handled by EmbeddedWebappModule.class.

Because these tests can be easily run and debugged in eclipse, method byte code replacement (stuff that's been around since jdk 1.4) can be used while debugging to work on methods either in the application or test (after popping it of the execution stack)

You will also need a toolchains.xml file in your ~/.m2 repo, you can find the info here: http://maven.apache.org/guides/mini/guide-using-toolchains.html

My toolchains.xml on mac looks like:

<?xml version="1.0" encoding="UTF8"?>
<toolchains>
  <toolchain>
     <type>jdk</type>
     <provides>
         <version>1.5</version>
         <vendor>sun</vendor>
         <id>default</id>
     </provides>
     <configuration>
        <jdkHome>/System/Library/Frameworks/JavaVM.framework/Versions/1.5/</jdkHome>
     </configuration>
  </toolchain>
  <toolchain>
     <type>jdk</type>
     <provides>
         <version>1.6</version>
         <vendor>sun</vendor>
         <id>ide</id>
     </provides>
     <configuration>
        <jdkHome>/System/Library/Frameworks/JavaVM.framework/Versions/1.6</jdkHome>
     </configuration>
  </toolchain>
</toolchains>

To have the java 5 actually reference a real java 1.5 install follow these instructions: http://www.s-seven.net/java_15_lion


One caveat, if you try to run 'mvn site:site' on it's own it will fail, you must run "mvn install site:site" as some of the later builds rely on copying the EmbeddedWebapp War in to their target directory before running the webbapp in Jetty.
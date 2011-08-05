package org.ehoffman.testng.tests;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ehoffman.testng.extensions.AnnotationEnforcer;
import org.ehoffman.testng.extensions.Broken;
import org.ehoffman.testng.extensions.Fixture;
import org.ehoffman.testng.extensions.modules.FixtureContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({ MyEnforcer.class })
public class FrameworkTest {
  private static Set<String> results = Collections.synchronizedSet(new HashSet<String>());
  private static Set<String> expectedForUnitTests = new HashSet<String>(Arrays.asList("sharedTest","unit1"));
  private static Set<String> expectedForIntegrationTests = new HashSet<String>(Arrays.asList("sharedTest", "sharedTest2", "remote1"));

  private static final Logger logger = LoggerFactory.getLogger(FrameworkTest.class);

  @Test(groups = { "unit","remote-integration" })
  @Fixture(factory = {CountModule.class, SimpleModule.class}, destructive = false)
  public void sharedTest() throws Exception {
    logger.info(this.getClass().getClassLoader().getClass().getName());
    logger.info(""+System.getProperty("java.class.path").contains("org.testng.eclipse"));
    Object o = FixtureContainer.getService(SimpleModule.class);
    logger.info(System.getProperties().toString());
    logger.info("Class is "+o.getClass().getSimpleName() + " and modules are: " + FixtureContainer.getModuleClassesSimpleName());
    Integer fixture = ((IntegerHolder)o).getInteger();
    assertThat(fixture).isEqualTo(42);
    results.add("sharedTest");
  }

  @Test(groups = { "remote-integration" })
  public void sharedTest2() {
    results.add("sharedTest2");
  }

  @Test(groups = "remote-integration")
  public void remote1() {
    logger.info(System.getenv().toString());
    // assertThat(AnnotationEnforcer.isIntegrationTestPhase()).as("This test should not be run during the unit phase").isTrue();
    results.add("remote1");
  }

  @Test(groups = "unit")
  public void unit1() {
    results.add("unit1");
    // assertThat(AnnotationEnforcer.isIntegrationTestPhase()).as("This test should not be run during the integration phase").isFalse();
  }

  @Test(groups = { "remote-integration", "unit" })
  @Broken(developer = "rex hoffman", issueInTracker = "???")
  public void shared3Broken() {
    results.add("shared3Broken");
    assertThat(false).as("This test should not be run when known breaks is set to false").isTrue();
  }

  @AfterSuite()
  public void verifyTestMethods() {
    if (!AnnotationEnforcer.isIntegrationTestPhase()) {
      logger.info("unit: " + results);
      assertThat(results).containsOnly(expectedForUnitTests.toArray()).as("Should contain only the expected tests");
    } else {
      logger.info("integration: " + results);
      assertThat(results).containsOnly(expectedForIntegrationTests.toArray()).as("Should contain only the expected tests");
    }
  }

}
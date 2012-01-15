package org.ehoffman.testing.tests;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testng.extensions.Broken;
import org.ehoffman.testng.extensions.Fixture;
import org.ehoffman.testng.extensions.JUnitReportReporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({MyEnforcer.class})
public class FrameworkTest {
  private static Set<String> results = Collections.synchronizedSet(new HashSet<String>());
  private static String SHARED_TEST = "sharedTest";
  private static String REMOTE_TEST_2 = "remote2";
  private static String UNIT_TEST = "unit1";
  private static String REMOTE_TEST = "remote1";
  private static String SHARED_BROKEN_TEST = "broken";
  
  private static Set<String> expectedForUnitTests = new HashSet<String>(Arrays.asList(SHARED_TEST,UNIT_TEST));
  private static Set<String> expectedForIntegrationTests = new HashSet<String>(Arrays.asList(SHARED_TEST, REMOTE_TEST_2, REMOTE_TEST));
  private static Set<String> all = new HashSet<String>();
  static{
    all.addAll(expectedForIntegrationTests);
    all.addAll(expectedForUnitTests);
  }
  
  private static final Logger logger = LoggerFactory.getLogger(FrameworkTest.class);

  @Test(groups = { "unit","remote-integration" })
  @Fixture(factory = {CountModule.class, SimpleModule.class}, destructive = false)
  public void sharedTest() throws Exception {
    Object o = FixtureContainer.getService(SimpleModule.class);
    Integer fixture = ((IntegerHolder)o).getInteger();
    assertThat(fixture).isEqualTo(42);
    results.add(SHARED_TEST);
  }

  @Test(groups = { "remote-integration" })
  public void remote2() {
    results.add(REMOTE_TEST_2);
  }

  @Test(groups = {"remote-integration"})
  public void remote1() {
    logger.info("remote1");
    // assertThat(AnnotationEnforcer.isIntegrationTestPhase()).as("This test should not be run during the unit phase").isTrue();
    results.add(REMOTE_TEST);
  }

  @Test(groups = "unit")
  public void unit1() {
    results.add("unit1");
    // assertThat(AnnotationEnforcer.isIntegrationTestPhase()).as("This test should not be run during the integration phase").isFalse();
  }

  @Test(groups = { "remote-integration", "unit"})
  @Broken(developer = "rex hoffman", issueInTracker = "???")
  public void shared3Broken() {
    results.add(SHARED_BROKEN_TEST);
    assertThat(false).as("This test should not be run when known breaks is set to false").isTrue();
  }

  @AfterSuite()
  public void verifyTestMethods() {
    if (MyEnforcer.isIdeMode()){
      logger.info("all: " + results);
      assertThat(results).containsOnly(all.toArray()).as("Should contain only the expected tests");
    } else if (MyEnforcer.isIntegrationPhase()) {
      logger.info("integration: " + results);
      assertThat(results).containsOnly(expectedForIntegrationTests.toArray()).as("Should contain only the expected tests");
    } else {
      logger.info("unit: " + results);
      assertThat(results).containsOnly(expectedForUnitTests.toArray()).as("Should contain only the expected tests");
    }
  }

}
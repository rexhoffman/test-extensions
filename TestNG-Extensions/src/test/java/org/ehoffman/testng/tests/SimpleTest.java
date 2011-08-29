package org.ehoffman.testng.tests;

import static org.fest.assertions.Assertions.assertThat;

import org.ehoffman.testing.module.FixtureContainer;
import org.ehoffman.testing.testng.FixtureRunnerMethodInterceptor;
import org.ehoffman.testng.extensions.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(FixtureRunnerMethodInterceptor.class)
public class SimpleTest {
  
  private static final Logger logger = LoggerFactory.getLogger(FrameworkTest.class);

  @Test(groups = { "unit","remote-integration" })
  @Fixture(factory = {CountModule.class, SimpleModule.class}, destructive = false)
  public void sharedTest() throws Exception {
    logger.info("sharedTest "+FixtureContainer.getModuleClassesSimpleName());
    Object o = FixtureContainer.getService(SimpleModule.class);
    Integer fixture = ((IntegerHolder)o).getInteger();
    assertThat(fixture).isEqualTo(42);
  }

}

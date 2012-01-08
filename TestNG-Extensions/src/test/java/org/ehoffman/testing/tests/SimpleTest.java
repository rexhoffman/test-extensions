package org.ehoffman.testing.tests;

import static org.fest.assertions.Assertions.assertThat;

import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testng.extensions.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({MyEnforcer.class})
public class SimpleTest {
  
  private static final Logger logger = LoggerFactory.getLogger(SimpleTest.class);
    
  @Test(groups = { "unit","remote-integration" })
  @Fixture(factory = {CountModule.class, SimpleModule.class})
  public void sharedTest() throws Exception {
    logger.info("sharedTest "+FixtureContainer.getModuleClassesSimpleName());
    Object o = FixtureContainer.getService(SimpleModule.class);
    System.out.println(FixtureContainer.getModuleClasses());
    Integer fixture = ((IntegerHolder)o).getInteger();
    assertThat(fixture).isEqualTo(42);
  }

}

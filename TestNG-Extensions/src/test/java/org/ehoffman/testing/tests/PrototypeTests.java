package org.ehoffman.testing.tests;

import static org.fest.assertions.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.ehoffman.module.PrototypeModule;
import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testng.extensions.Fixture;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyEnforcer.class)
public class PrototypeTests {

  public static class PrototypeSimpleModule extends SimpleModule implements PrototypeModule<IntegerHolder>{
  }

  private AtomicReference<IntegerHolder> integerHolder = new AtomicReference<IntegerHolder>();
  
  private void validateThisTestsPrototypeIsNotTheSameAsTheOthers(IntegerHolder testsValue){
    IntegerHolder otherTestsInstance = integerHolder.getAndSet(testsValue);
    if (otherTestsInstance != null){
      assertThat(testsValue).as("Prototype modules appear to not be working").isNotSameAs(otherTestsInstance);
    }
  }
  
  @Test(groups="unit")
  @Fixture(factory=PrototypeSimpleModule.class)
  public void setSimpleModuleTest1(){
    IntegerHolder service = FixtureContainer.getService(PrototypeSimpleModule.class);
    IntegerHolder service2 = FixtureContainer.getService(PrototypeSimpleModule.class);
    assertThat(service2).isSameAs(service);
    validateThisTestsPrototypeIsNotTheSameAsTheOthers(service);
  }

  @Test(groups="unit")
  @Fixture(factory=PrototypeSimpleModule.class)
  public void setSimpleModuleTest2(){
    IntegerHolder service = FixtureContainer.getService(PrototypeSimpleModule.class);
    IntegerHolder service2 = FixtureContainer.getService(PrototypeSimpleModule.class);
    assertThat(service2).isSameAs(service);
    validateThisTestsPrototypeIsNotTheSameAsTheOthers(service);
  }
  
  @Test(groups="unit")
  @Fixture(factory=PrototypeSimpleModule.class)
  public void setSimpleModuleTest2(){
    IntegerHolder service = FixtureContainer.getService(PrototypeSimpleModule.class);
    IntegerHolder service2 = FixtureContainer.getService(PrototypeSimpleModule.class);
    assertThat(service2).isSameAs(service);
    validateThisTestsPrototypeIsNotTheSameAsTheOthers(service);
  }
  
}

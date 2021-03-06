package org.ehoffman.testing.tests;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.ehoffman.module.Module;
import org.ehoffman.testing.fixture.FixtureContainer;
import org.ehoffman.testng.extensions.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MyEnforcer.class)
public class TestNiceMessageOnDependencyNotSet {
  private static Logger logger = LoggerFactory.getLogger(TestNiceMessageOnDependencyNotSet.class);

  public static class TestModule implements Module<IntegerHolder>{

    @Override
    public String getModuleType() {
      return this.getClass().getSimpleName();
    }

    @Override
    public Class<? extends IntegerHolder> getTargetClass() {
      return IntegerHolder.class;
    }

    @Override
    public Map<String, Class<?>> getDependencyDefinition() {
      Map<String, Class<?>> dependencyDefinitions = new HashMap<String, Class<?>>();
      dependencyDefinitions.put("DNE",IntegerHolder.class);
      return dependencyDefinitions;
    }

    @Override
    public IntegerHolder create(Map<String, ?> dependencies) {
      IntegerHolder holder = (IntegerHolder)dependencies.get("DNE");
      IntegerHolder output = new IntegerHolder();
      output.setInteger(holder.getInteger() * 2);
      return output;
    }

    @Override
    public void destroy() {
    }
  }

  @Test(groups="unit")
  @Fixture(factory = {TestNiceMessageOnDependencyNotSet.TestModule.class})
  public void niceDependencyNotDefinedErrorMessage(){
    try {
      IntegerHolder holder = FixtureContainer.getService(TestNiceMessageOnDependencyNotSet.TestModule.class);
    } catch (RuntimeException e){
      assertThat(e.getMessage()).contains("Module TestModule has missing dependencies.  They are {DNE=class org.ehoffman.testing.tests.IntegerHolder}");
    }
  }
}

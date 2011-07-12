package org.ehoffman.testng.tests;

import java.util.HashMap;
import java.util.Map;

import org.ehoffman.module.Module;
import org.ehoffman.testng.extensions.Fixture;
import org.ehoffman.testng.extensions.modules.FixtureContainer;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.fest.assertions.Assertions.*;

@Listeners(MyEnforcer.class)
public class TestNiceMessageOnDependencyNotSet {

  public static class TestModule implements Module<IntegerHolder>{

    public String getModuleType() {
      return this.getClass().getSimpleName();
    }

    public String getName() {
      return this.getClass().getSimpleName();
    }

    public Class<? extends IntegerHolder> getTargetClass() {
      return IntegerHolder.class;
    }

    public Map<String, Class<?>> getDependencyDefinition() {
      Map<String, Class<?>> dependencyDefinitions = new HashMap<String, Class<?>>();
      dependencyDefinitions.put("DNE",IntegerHolder.class);
      return dependencyDefinitions;
    }

    public IntegerHolder create(Map<String, ?> dependencies) {
      IntegerHolder holder = (IntegerHolder)dependencies.get("DNE");
      IntegerHolder output = new IntegerHolder();
      output.setInteger(holder.getInteger() * 2);
      return output;
    }

    public void destroy() {
    }
  }

  @Test(groups="unit")
  @Fixture(factory = {TestNiceMessageOnDependencyNotSet.TestModule.class})
  public void niceDependencyNotDefinedErrorMessage(){
    IntegerHolder holder = FixtureContainer.getService(TestNiceMessageOnDependencyNotSet.TestModule.class);
    try {
      System.out.println(holder.getInteger());
    } catch (RuntimeException e){
      assertThat(e.getMessage()).contains("Module TestModule has missing dependencies.  They are {DNE=class org.ehoffman.testng.tests.IntegerHolder}");
    }
  }
}

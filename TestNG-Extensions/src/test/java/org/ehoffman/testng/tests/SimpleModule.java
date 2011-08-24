package org.ehoffman.testng.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleProvider;

public class SimpleModule implements Module<IntegerHolder> {

  public String getModuleType(){
    return SimpleModule.class.getSimpleName();    
  }
  
  public SimpleModule() {
  }

  public boolean requiresRemote() {
    return false;
  }

  public boolean threadSafe() {
    return true;
  }

  public String getName() {
    return "Simple";
  }

  public void destroy() {
  }

  public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
    List<Class<? extends ModuleProvider<?>>> list = new ArrayList<Class<? extends ModuleProvider<?>>>();
    list.add(SimpleModule.class);
    return list;
  }

  public Map<String, Class<?>> requiredServices() {
    return null;
  }

  public Class<IntegerHolder> getTargetClass() { 
    return IntegerHolder.class;
  }

  public Map<String, Class<?>> getDependencyDefinition() {
    return null;
  }

  public IntegerHolder create(Map<String, ?> dependencies) {
    IntegerHolder integerHolder = new IntegerHolder();
    integerHolder.setInteger(42);
    return integerHolder;
  }
}
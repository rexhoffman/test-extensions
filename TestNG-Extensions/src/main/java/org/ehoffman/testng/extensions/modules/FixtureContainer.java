package org.ehoffman.testng.extensions.modules;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.testng.extensions.services.HotSwappableProxy;
import org.ehoffman.testng.extensions.services.HotswappableThreadLocalProxyFactory;


public class FixtureContainer {

  private static ThreadLocal<TestResult> testResult = new ThreadLocal<TestResult>();  
  private static Map<String, HotSwappableProxy> fixtureServices = new HashMap<String, HotSwappableProxy>();
  private static ThreadLocal<Set<Class<? extends Module<?>>>> moduleClasses = new ThreadLocal<Set<Class<? extends Module<?>>>>();
  
  public static void createServicesIfNeeded(Set<Class<? extends Module<?>>> modules){
    synchronized (fixtureServices) {
      for (Class<? extends Module<?>> moduleClass : modules){
        Module<?> module = Modules.getInstance(moduleClass);
        String serviceName = module.getModuleType();
        HotSwappableProxy proxy = fixtureServices.get(serviceName);
        if (proxy == null){
          proxy = (HotSwappableProxy)HotswappableThreadLocalProxyFactory.createHotSwapableThreadLocalTarget(module);
          fixtureServices.put(serviceName, proxy);
        }
      }
    }
  }
  
  public static Map<String, HotSwappableProxy> getServices(){
    return fixtureServices;
  }
  
  /**
   * @param name
   * @param value
   */
  public static void addAttribute(String name, String value){
    testResult.get().setAttribute(name, value);
  }

  /**
   * @param name of the file to be created
   * @param value the bytes of the file to link to, or display if an image
   */
  public static void addAttribute(String name, byte[] value){
    testResult.get().setAttribute(name, value);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<? extends ModuleProvider<T>> clazz) {
    try {
      ModuleProvider<T> provider = clazz.newInstance();
      return (T) fixtureServices.get(provider.getModuleType());
    } catch (IllegalAccessException e){
      throw new RuntimeException("Module Provider could not be constructed", e);
    } catch (InstantiationException e){
      throw new RuntimeException("Module Provider could not be constructed", e);
    }
  }
  
  public static Set<Class<? extends Module<?>>> getModuleClasses(){
	  return Collections.unmodifiableSet(moduleClasses.get());
  }
  
  public static Set<String> getModuleClassesSimpleName(){
    Set<String> output = new HashSet<String>(moduleClasses.get().size());
    for (Class<? extends Module<?>> clazz : moduleClasses.get()){
      output.add(clazz.getSimpleName());
    }
    return Collections.unmodifiableSet(output);
  }
  
  static void wipeFixture() {
    for (Entry<String, HotSwappableProxy> serviceEntry : fixtureServices.entrySet()){
      serviceEntry.getValue().setProxyTargetModule(null);
    }
    testResult.remove();
  }
  
  static void setModuleClassesAndTestResult(Set<Class<? extends Module<?>>> moduleClasses, TestResult result){
	  FixtureContainer.moduleClasses.set(moduleClasses);
	  for (Class<? extends Module<?>> moduleClass : moduleClasses){
	    Module<?> module = Modules.getInstance(moduleClass);
	    String serviceName = module.getModuleType();
	    fixtureServices.get(serviceName).setProxyTargetModule(module);
	  }
    testResult.set(result);
  }
}

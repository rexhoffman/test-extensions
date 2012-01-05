package org.ehoffman.testing.fixture;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.testing.fixture.services.HotSwappableProxy;
import org.ehoffman.testing.fixture.services.HotswapableThreadLocalInvocationHandler;
import org.ehoffman.testing.fixture.services.HotswappableThreadLocalProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixtureContainer {
  private static final Logger                                               logger                    = LoggerFactory.getLogger(FixtureContainer.class);
  private static final ConcurrentMap<Class<? extends Module<?>>, Module<?>> reusableModulesInstances  = new ConcurrentHashMap<Class<? extends Module<?>>, Module<?>>();
  private static final ConcurrentMap<String, HotSwappableProxy>             fixtureServices           = new ConcurrentHashMap<String, HotSwappableProxy>();
  private static ThreadLocal<Set<Class<? extends Module<?>>>>               moduleClasses             = new ThreadLocal<Set<Class<? extends Module<?>>>>();

  private static void throwRuntimeExceptionIfModuleClassesAreNotSet(){
    if (moduleClasses.get() == null){
      throw new RuntimeException("You must use extend the ExtensibleTestNGListener class and make sure it is applied as a TestNG listener to this test.  setInterceptors(");
    }
  }
  
  static Iterator<Set<Class<? extends Module<?>>>> getDotProductModuleCombinations(Collection<Class<? extends ModuleProvider<?>>> moduleClasses, boolean destructive) {
    Collection<Set<Class<? extends Module<?>>>> listOfOptions = mergeListsOfSameModuleType(ModuleUtil.getAllPossibleModules(moduleClasses));
    return new DotProductIterator<Class<? extends Module<?>>>(listOfOptions);
  }

  private static <T extends Module<?>> T getNewInstance(Class<T> moduleClass) {
    T module = null;
    try {
      module = moduleClass.newInstance();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return module;
  }

  private static Module<?> getModuleFromClass(Class<? extends Module<?>> moduleClass) {
    Module<?> module = null;
    module = reusableModulesInstances.get(moduleClass);
    if (module == null) {
        module = getNewInstance(moduleClass);
        reusableModulesInstances.putIfAbsent(moduleClass, module);
    }
    if (module != reusableModulesInstances.get(moduleClass)) { //another thread has already added the module
        module.destroy();
        module = reusableModulesInstances.get(moduleClass);
    }
    return module;
  }

  static void destroyAll(){
    for (Module<?> module : reusableModulesInstances.values()){
      module.destroy();
    }
  }
  
  
  static Collection<Set<Class<? extends Module<?>>>> mergeListsOfSameModuleType(Collection<Class<? extends Module<?>>> input) {
    Map<String, Set<Class<? extends Module<?>>>> output = new HashMap<String, Set<Class<? extends Module<?>>>>();
    if (input != null) {
      for (Class<? extends Module<?>> clazz : input) {
        Module<?> module = getModuleFromClass(clazz);
        String type = module.getModuleType();
        if (output.get(type) == null) {
          Set<Class<? extends Module<?>>> newList = new HashSet<Class<? extends Module<?>>>();
          newList.add(clazz);
          output.put(type, newList);
        } else {
          output.get(type).add(clazz);
        }
      }
    }
    return output.values();
  }

  /**
   * Given a the class of a {@link Module}, ensure that a {@link HotSwappableProxy} for the Service needed, for this {@link Module} class is put in the {@link FixtureContainer#fixtureServices}
   * When the {@link HotSwappableProxy} is accessed in a test it will then invoke the {@link Module#create(Map)} method after instantiating a object from the {@link Module} class.
   * 
   * @param moduleClass
   */
  protected static void createServiceIfNeeded(Class<? extends Module<?>> moduleClass) {
    Module<?> module = getModuleFromClass(moduleClass);
    String serviceName = module.getModuleType();
    HotSwappableProxy proxy = fixtureServices.get(serviceName);
    if (proxy == null) {
      proxy = (HotSwappableProxy) HotswappableThreadLocalProxyFactory.createHotSwapableThreadLocalTarget(module);
      fixtureServices.putIfAbsent(serviceName, proxy);
    }
  }

  /**
   * Given a module class, ensure that the Service needed for this class is put in the {@link FixtureContainer#fixtureServices}
   * 
   * @param moduleClass
   */
  protected static void createServicesIfNeeded(Set<Class<? extends Module<?>>> moduleClasses) {
    for (Class<? extends Module<?>> moduleClass : moduleClasses) {
      createServiceIfNeeded(moduleClass);
    }
  }

  public static Map<String, HotSwappableProxy> getServices() {
    return fixtureServices;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<? extends ModuleProvider<T>> clazz) {
    throwRuntimeExceptionIfModuleClassesAreNotSet();
    try {
      ModuleProvider<T> provider = clazz.newInstance();
      HotSwappableProxy proxy = (HotSwappableProxy) fixtureServices.get(provider.getModuleType());
      T nakedService = (T) proxy.getUnwrappedService();
      return nakedService;
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Module Provider could not be constructed", e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Module Provider could not be constructed", e);
    }
  }

  /**
   * Should the test need to access the list of {@link Module} classes that are made available to it (your test shouldn't)
   * they are available as an unmodifiable set return from this method.
   */
  public static Set<Class<? extends Module<?>>> getModuleClasses() {
    return Collections.unmodifiableSet(moduleClasses.get());
  }

  /**
   * Primarily for logging purposes, this method will return a {@link Set} of {@link String} that are the {@link Class#getSimpleName()}
   * of the {@link Module} classes provided to a test through the {@link FixtureContainer}.
   */
  public static Set<String> getModuleClassesSimpleName() {
    if (moduleClasses.get() == null){
      throw new RuntimeException("The FixtureRunnerMethodInterceptor is not configure as a TestNG Listener method.");
    }
    Set<String> output = new HashSet<String>(moduleClasses.get().size());
    for (Class<? extends Module<?>> clazz : moduleClasses.get()) {
      output.add(clazz.getSimpleName());
    }
    return Collections.unmodifiableSet(output);
  }

  /**
   * Called after a test run to clear the {@link ThreadLocal} {@link FixtureContainer#moduleClasses}, to ensure
   * that module information is not accidentally accessible to a test through the static methods on this class
   * to the next test to execute on this thread.
   */
  static void wipeFixture() {
    for (Entry<String, HotSwappableProxy> serviceEntry : fixtureServices.entrySet()) {
      serviceEntry.getValue().setProxyTargetModule(null);
    }
    moduleClasses.set(null);
  }

  /**
   * Sets the set of module classes used for one testMethod invocation into a
   * thread local map as well as the individual {@link HotSwappableProxy}
   * instances by calling the
   * {@link HotSwappableProxy#setProxyTargetModule(Module)} method.
   * 
   * For implementations {@link HotSwappableProxy#setProxyTargetModule(Module)}
   * see
   * {@link HotswapableThreadLocalInvocationHandler#invocation(java.lang.reflect.Method, Object[])}
   * 
   * @param moduleClasses
   */
  static void setModuleClasses(Set<Class<? extends Module<?>>> moduleClasses) {
    createServicesIfNeeded(moduleClasses);
    FixtureContainer.moduleClasses.set(moduleClasses);
    for (Class<? extends Module<?>> moduleClass : moduleClasses) {
      Module<?> module = getModuleFromClass(moduleClass);
      String serviceName = module.getModuleType();
      fixtureServices.get(serviceName).setProxyTargetModule(module);
    }
  }
}

package org.ehoffman.testing.module;

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
import org.ehoffman.testing.module.Modules;
import org.ehoffman.testng.extensions.services.HotSwappableProxy;
import org.ehoffman.testng.extensions.services.HotswapableThreadLocalInvocationHandler;
import org.ehoffman.testng.extensions.services.HotswappableThreadLocalProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixtureContainer {
  private static final Logger                                               logger                    = LoggerFactory.getLogger(Modules.class);
  private static final ConcurrentMap<Class<? extends Module<?>>, Module<?>> reusableThreadSafeModules = new ConcurrentHashMap<Class<? extends Module<?>>, Module<?>>();
  private static final ConcurrentMap<String, HotSwappableProxy>             fixtureServices           = new ConcurrentHashMap<String, HotSwappableProxy>();
  private static ThreadLocal<Set<Class<? extends Module<?>>>>               moduleClasses             = new ThreadLocal<Set<Class<? extends Module<?>>>>();

  public static Iterator<Set<Class<? extends Module<?>>>> getDotProductModuleCombinations(Collection<Class<? extends ModuleProvider<?>>> moduleClasses, boolean destructive) {
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

  private static Module<?> getModuleFromClass(Class<? extends Module<?>> moduleClass, boolean destructive) {
    Module<?> module = null;
    if (!destructive) {
      module = reusableThreadSafeModules.get(moduleClass);
      if (module == null) {
        module = getNewInstance(moduleClass);
        reusableThreadSafeModules.putIfAbsent(moduleClass, module);
      }
      if (module != reusableThreadSafeModules.get(moduleClass)) {
        module.destroy();
        module = reusableThreadSafeModules.get(moduleClass);
      }
    } else {
      module = getNewInstance(moduleClass);
    }
    return module;
  }

  public static void destroyAll(){
    for (Module<?> module : reusableThreadSafeModules.values()){
      module.destroy();
    }
  }
  
  public static Collection<Set<Class<? extends Module<?>>>> mergeListsOfSameModuleType(Collection<Class<? extends Module<?>>> input) {
    Map<String, Set<Class<? extends Module<?>>>> output = new HashMap<String, Set<Class<? extends Module<?>>>>();
    if (input != null) {
      for (Class<? extends Module<?>> clazz : input) {
        Module<?> module = getModuleFromClass(clazz, false);
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

  public static void createServiceIfNeeded(Class<? extends Module<?>> moduleClass) {
    Module<?> module = getModuleFromClass(moduleClass, false);
    String serviceName = module.getModuleType();
    HotSwappableProxy proxy = fixtureServices.get(serviceName);
    if (proxy == null) {
      proxy = (HotSwappableProxy) HotswappableThreadLocalProxyFactory.createHotSwapableThreadLocalTarget(module);
      fixtureServices.putIfAbsent(serviceName, proxy);
    }
  }

  public static void createServicesIfNeeded(Set<Class<? extends Module<?>>> moduleClasses) {
    for (Class<? extends Module<?>> moduleClass : moduleClasses) {
      createServiceIfNeeded(moduleClass);
    }
  }

  public static Map<String, HotSwappableProxy> getServices() {
    return fixtureServices;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<? extends ModuleProvider<T>> clazz) {
    try {
      ModuleProvider<T> provider = clazz.newInstance();
      return (T) fixtureServices.get(provider.getModuleType());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Module Provider could not be constructed", e);
    } catch (InstantiationException e) {
      throw new RuntimeException("Module Provider could not be constructed", e);
    }
  }

  public static Set<Class<? extends Module<?>>> getModuleClasses() {
    return Collections.unmodifiableSet(moduleClasses.get());
  }

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

  public static void wipeFixture() {
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
  public static void setModuleClasses(Set<Class<? extends Module<?>>> moduleClasses, boolean destructive) {
    createServicesIfNeeded(moduleClasses);
    FixtureContainer.moduleClasses.set(moduleClasses);
    for (Class<? extends Module<?>> moduleClass : moduleClasses) {
      Module<?> module = getModuleFromClass(moduleClass, destructive);
      String serviceName = module.getModuleType();
      fixtureServices.get(serviceName).setProxyTargetModule(module);
    }
  }
}

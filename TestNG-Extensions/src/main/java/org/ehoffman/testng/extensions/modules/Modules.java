package org.ehoffman.testng.extensions.modules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleGroup;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.testng.extensions.services.HotSwappableProxy;

public class Modules {

  private static final ConcurrentMap<Class<? extends Module<?>>, Module<?>> reusableThreadSafeModules = new ConcurrentHashMap<Class<? extends Module<?>>, Module<?>>();

  static <T extends Module<?>> T getInstance(Class<T> moduleClass) {
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
      synchronized (reusableThreadSafeModules) {
        module = reusableThreadSafeModules.get(moduleClass);
        if (module == null) {
          module = getInstance(moduleClass);
          reusableThreadSafeModules.put(moduleClass, module);
        }
      }
    } else {
      module = getInstance(moduleClass);
    }
    return module;
  }
  
  private static Collection<Set<Class<? extends Module<?>>>> mergeListsOfSameModuleType(Collection<Class<? extends Module<?>>> input){
    Map<String, Set<Class<? extends Module<?>>>> output = new HashMap<String, Set<Class<? extends Module<?>>>>();
    if (input != null){
      for (Class<? extends Module<?>> clazz : input){
        Module<?> module = getModuleFromClass(clazz, false);
        String type = module.getModuleType();
        if (output.get(type) == null){
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
   * @param provider a class should implement ModuleGroup or Module, not just ModuleProvider, which acts a common marking super interface.
   * A module represents a single, atomic piece of a test fixture.
   * A module group contains a list of class to ModuleProviders, which may in turn be module groups.
   * 
   * This method walks that chain an returns the list of all Modules referenced by a ModuleGroup
   * 
   * @return a List of a Module Class, that are referenced by the ModuleGroup, or the input if it is an implementation of Module and not ModuleProvider
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  @SuppressWarnings("unchecked")
  private static Set<Class<? extends Module<?>>> getAllPossibleModules(Class<? extends ModuleProvider<?>> provider) {
    Set<Class<? extends Module<?>>> modules = new HashSet<Class<? extends Module<?>>>();
    if (ModuleGroup.class.isAssignableFrom(provider)){
      try {
        List<Class<? extends ModuleProvider<?>>> classes = ((ModuleGroup<?>)provider.newInstance()).getModuleClasses();
        if (classes != null){
          for (Class<? extends ModuleProvider<?>> _provider : classes){
             modules.addAll(getAllPossibleModules(_provider));
          }
        }
      } catch (IllegalAccessException e){
        throw new RuntimeException("a class that implements ModuleGroup must have a public, no argument constructor"+provider, e);
      } catch (InstantiationException e){
        throw new RuntimeException("a class that implements ModuleGroup could not be instanciated "+provider, e);
      }
    } else if (Module.class.isAssignableFrom(provider)){
      modules.add((Class<? extends Module<?>>) provider);
    } else {
      throw new RuntimeException("only an instance of module or modulegroup may be used to configure a foctory "+provider);
    }
    return modules;
  }

  private static Set<Class<? extends Module<?>>> getAllPossibleModules(Collection<Class<? extends ModuleProvider<?>>> providers) {
    Set<Class<? extends Module<?>>> modules = new HashSet<Class<? extends Module<?>>>();
    for (Class<? extends ModuleProvider<?>> provider : providers){
      modules.addAll(getAllPossibleModules(provider));
    }
    System.out.println("All modules: "+modules);
    return modules;
  }
  
  public static Iterator<Set<Class<? extends Module<?>>>> getDotProductModuleCombinations(Collection<Class<? extends ModuleProvider<?>>> moduleClasses, boolean destructive) {
    Collection<Set<Class<? extends Module<?>>>> listOfOptions = mergeListsOfSameModuleType(getAllPossibleModules(moduleClasses));
    return new DotProductIterator<Class<? extends Module<?>>>(listOfOptions);
  }

  public static Set<Module<?>> getModules(Set<Class<? extends Module<?>>> factoryClasses, boolean destructive) {
    Set<Module<?>> modules = new HashSet<Module<?>>();
    for (Class<? extends Module<?>> fclass : factoryClasses) {
      modules.add(getModuleFromClass(fclass, destructive));
    }
    return modules;
  }

  public static void unsetServiceTargetModule(Collection<HotSwappableProxy> services) {
    unsetAllServices(services);
  }

  private static void unsetAllServices(Collection<HotSwappableProxy> proxies) {
    if (proxies != null){
      for (HotSwappableProxy proxy : proxies) {
        proxy.setProxyTargetModule(null);
      }
    }
  }
  
  public static void destroyAll(){
    for (Module<?> module : reusableThreadSafeModules.values()){
      HotSwappableProxy proxy = FixtureContainer.getServices().get(module.getModuleType());
      proxy.setProxyTargetModule(null);
      module.destroy();
    }
  }
  
  public static void destroyAll(Set<? extends Module<?>> modules){
    if (modules != null){
      for (Module<?> module : modules){
        module.destroy();
      }
    }
  }

}

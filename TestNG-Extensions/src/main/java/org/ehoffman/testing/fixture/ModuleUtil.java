package org.ehoffman.testing.fixture;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleGroup;
import org.ehoffman.module.ModuleProvider;

/**
 * Provides utility methods to take a list of {@link Module} and {@link ModuleGroup}, and expand them out by drilling in to all modules and groups provided by the {@link ModuleGroup#getModuleClasses()} method.
 * The end result returned is a {@link Set} of {@link Module}.
 * 
 * @author rexhoffman
 */
public class ModuleUtil {
    
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
  public static Set<Class<? extends Module<?>>> getAllPossibleModules(Class<? extends ModuleProvider<?>> provider) {
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
      @SuppressWarnings("unchecked")
      Class<? extends Module<?>> clazz = (Class<? extends Module<?>>) provider;
      modules.add(clazz);
    } else {
      throw new RuntimeException("only an instance of module or modulegroup may be used to configure a foctory "+provider);
    }
    return modules;
  }

  public static Set<Class<? extends Module<?>>> getAllPossibleModules(Collection<Class<? extends ModuleProvider<?>>> providers) {
    Set<Class<? extends Module<?>>> modules = new HashSet<Class<? extends Module<?>>>();
    for (Class<? extends ModuleProvider<?>> provider : providers){
      modules.addAll(getAllPossibleModules(provider));
    }
    return modules;
  }
}

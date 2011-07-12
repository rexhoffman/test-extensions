package org.ehoffman.module;

import java.util.List;

public interface ModuleGroup<T> extends ModuleProvider<T> {
  
  /**
   * 
   * @return
   */
  public List<Class<? extends ModuleProvider<?>>> getModuleClasses();
}

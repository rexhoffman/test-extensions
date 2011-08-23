package org.ehoffman.module;

import java.util.Map;


/**
 * Indicates an interchangeable piece of a test Fixture.  The Modules exposes service for consumption of a test method.
 * It also specifies services it needs in order to provide it's services.  Implementors of this class are expected to be thread safe.
 * Which requires that the class be stateless, or that it contains a poolable set of services, and selects one for each test that needs to run.
 * 
 * Testing will be run in a heavily multithreaded manner.  Care must be taken in constructing implementations of this class.
 * @author rexhoffman
 */
public interface Module<T> extends ModuleProvider<T> {
	
  /**
   * This is a key that is used to determine which modules are interchangeable in testing 
   * @return
   */
  public String getModuleType();
  
  /**
   * This is the class that the TestNG-Extensions library, 
   * @return
   */
  Class<? extends T> getTargetClass();

  /**
   * 
   * @return
   */
  Map<String, Class<?>> getDependencyDefinition();
  
  /**
   * 
   * @param dependencies
   * @return
   */
  T create(Map<String, ?> dependencies);

  /**
   * Provides and needed clean up of this module.  This method will be called once on completion of the test suite.
   */
  void destroy();
}
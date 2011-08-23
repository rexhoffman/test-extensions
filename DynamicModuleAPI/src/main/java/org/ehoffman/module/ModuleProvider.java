package org.ehoffman.module;

/**
 * 
 * This is simply a marking interface.
 * 
 * The annotation @Fixture takes a list of classes that all must implement this interface.  Any implementor of this interface is expected to inherit it from
 * {@link Module} or {@link ModuleGroup} which it would implement.
 * 
 * @author rexhoffman
 *
 * @param <T>
 */
public interface ModuleProvider<T> {
	
	  /**
	   * This is a key that is used to determine which modules are interchangeable in testing, A module provider can exist 
	   * @return
	   */
	  public String getModuleType();
	  
}

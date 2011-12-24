package org.ehoffman.testing.fixture.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.pool.ObjectPool;
import org.ehoffman.module.Module;
import org.ehoffman.module.PooledModule;
import org.ehoffman.module.PrototypeModule;
import org.ehoffman.testing.fixture.FixtureContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The black magic the resides inside of Jdk Proxies: {@link java.lang.reflect.Proxy}, and Cglib Proxies: {@link net.sf.cglib.proxy.Proxy} 
 * that allows for the dynamic (Runtime) construction of classes to provide powerful wrappers around the Services provided
 * by {@link Modules}
 * 
 * @author Rex Hoffman
 */
public class HotswapableThreadLocalInvocationHandler implements InvocationHandler, MethodInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(HotswapableThreadLocalInvocationHandler.class);
  
  /**
   * Method name expected to reference {@link HotSwappableProxy#setProxyTargetModule(Module)}
   */
  private static final String setProxyTargetModuleMethodName = getMethodNameOnClassName("setProxyTargetModule", HotSwappableProxy.class, Module.class);

  /**
   * Method name expected to reference {@link HotSwappableProxy#getUnwrappedService()}
   */
  private static final String getUnwrappedServiceMethodName = getMethodNameOnClassName("getUnwrappedService", HotSwappableProxy.class);
  
  /**
   * Verifies the method with the expected inputs exists on the class...  this is done as a sanity check on classloading.
   * 
   * @param methodName
   * @param clazz
   * @param args
   * @return
   */
  private static String getMethodNameOnClassName(String methodName, Class<?> clazz, Class<?>... args){
    try {
      return clazz.getMethod(methodName, args).getName();
    } catch (Throwable t){
      throw new RuntimeException(methodName+"( "+ args +" ) on class "+clazz.getName()+" must exist",t);
    }
  }
  
  public HotswapableThreadLocalInvocationHandler(Module<?> module){
    this.module.set(module);
  }
  
  private ThreadLocal<Module<?>> module = new ThreadLocal<Module<?>>();
  private ThreadLocal<Object> holderOfInstance = new ThreadLocal<Object>();
  private ThreadLocal<ObjectPool> holderOfPool = new ThreadLocal<ObjectPool>();
  
  /**
   * Given a map of required dependencies, mapped from name to expected class/interface implemented by the dependency,
   * and a map of name to the available dependency object instance, determine if the object is not null and is an instance of 
   * the required class/interface.  Returns a map of missing dependency class.
   * 
   * @param dependencyDefinitions map of required dependency classes
   * @param availableDependencies map of available dependency instances
   * @return a map of name to class, of dependencies that are required but not available
   */
  private Map<String, Class<?>>  calcMissingDependencies(Map<String, Class<?>> dependencyDefinitions, Map<String, ?> availableDependencies){
    Map<String, Class<?>> missing = new HashMap<String, Class<?>>();
    if (dependencyDefinitions != null){
      for (Entry<String, Class<?>> entry : dependencyDefinitions.entrySet()){
        if (availableDependencies.get(entry.getKey()) == null ||
            !entry.getValue().isAssignableFrom(availableDependencies.get(entry.getKey()).getClass())){
          missing.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return missing;
  }
  
  private String getName(){
	  return module.get().getClass().getSimpleName();
  }
  
  
  private void createIfHolderInstanceNotSet() throws Exception {
    if (holderOfInstance.get() == null) {
      if (module.get() != null){
        Map<String, HotSwappableProxy> dependencies = new HashMap<String, HotSwappableProxy>();
        if (module.get().getDependencyDefinition() != null){
          for (Map.Entry<String, Class<?>> entry : module.get().getDependencyDefinition().entrySet()){
            dependencies.put(entry.getKey(), FixtureContainer.getServices().get(entry.getKey()));
          }
        }

        //Checks for missing dependencies and halts processing if they are found
        Map<String, Class<?>> missingDependencies = calcMissingDependencies(module.get().getDependencyDefinition(), dependencies);
        if (missingDependencies.size() != 0){
          throw new RuntimeException("Module "+getName()+" has missing dependencies.  They are "+missingDependencies);
        }
        
        logger.debug("About to build services for "+getName()+" with dependencies "+dependencies);
        if (PooledModule.class.isAssignableFrom(module.get().getClass())){
          holderOfPool.set((ObjectPool)FactoryUtil.buildObject(module.get(), dependencies));
          holderOfInstance.set(holderOfPool.get().borrowObject());
        } else  if (PrototypeModule.class.isAssignableFrom(module.get().getClass())) {
          
        } else {
          holderOfInstance.set(FactoryUtil.buildObject(module.get(), dependencies));
        }
      } else {
        throw new RuntimeException("Hotswappable Threadlocal Proxy was not set");
      }
    }
  }
  

  /**
   * When class is used as the {@link InvocationHandler} for a {@link java.lang.reflect.Proxy} this method will
   * delegate all the method invocation to the {@link HotswapableThreadLocalInvocationHandler#invocation(Method, Object[])} method.
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return invocation(method, args);
  }

  /**
   * When class is used as the {@link MethodInterceptor} for a {@link net.sf.cglib.proxy.Proxy} this method will
   * delegate all the method invocation to the {@link HotswapableThreadLocalInvocationHandler#invocation(Method, Object[])} method.
   */
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    return invocation(method, args);
  }
  
  
  /**
   * This is the the execution entry point for all calls to a proxied object.  
   * 
   * @param method
   * @param args
   * @return
   * @throws Exception
   */
  public Object invocation(Method method, Object[] args) throws Exception {
    if (method.getName().endsWith(setProxyTargetModuleMethodName) && args.length == 1) {
      if (module.get() != null && PooledModule.class.isAssignableFrom(module.get().getClass()) && holderOfPool.get() != null && holderOfInstance.get() != null){
        logger.info("Returning "+holderOfInstance.get()+" to Pool "+holderOfPool.get());
        holderOfPool.get().returnObject(holderOfInstance.get());
        holderOfPool.set(null);
      }
      holderOfInstance.set(null);
      module.set((Module<?>)args[0]);
      return null;
    } else if (method.getName().endsWith(getUnwrappedServiceMethodName) && (args == null || args.length == 0)) {
      createIfHolderInstanceNotSet();
      return holderOfInstance.get();
    } else {
      createIfHolderInstanceNotSet();
      return method.invoke(holderOfInstance.get(), args);
    }
  }
}

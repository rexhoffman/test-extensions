package org.ehoffman.testing.fixture.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.ehoffman.module.Module;
import org.ehoffman.module.PooledModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryUtil {
 
  private static Logger logger = LoggerFactory.getLogger(FactoryUtil.class);
  
  private static Map<Class<? extends Module<?>>, Map<Map<String,Object>, Object>> factoryClassToMapOfDependenciesToInstance = Collections.synchronizedMap(new HashMap<Class<? extends Module<?>>, Map<Map<String,Object>,Object>>());

  public static Map<String, Object> unwrapDependencies(Map<String, HotSwappableProxy> dependencies){
    Map<String, Object> output = new HashMap<String, Object>();
    for (Map.Entry<String, HotSwappableProxy> entry : dependencies.entrySet()){
      output.put(entry.getKey(), entry.getValue().getUnwrappedService());
    }
    return output;
  }
  
  private static ObjectPool createObjectPool(PooledModule<?> factory, Map<String, HotSwappableProxy> dependencies){
    Config config = new Config();
    config.maxActive = factory.getMaxPoolElements();
    config.maxIdle = factory.getMaxPoolElements();
    config.minIdle = 0;
    config.testOnBorrow = false;
    config.testOnReturn = false;
    config.testWhileIdle = false;
    config.whenExhaustedAction =  GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
    config.maxWait = -1;
    return new GenericObjectPool(factory, config);
  }
  
  @SuppressWarnings("unchecked")
  public static Object buildObject(Module<?> factory, Map<String, HotSwappableProxy> dependencies){
    Map<Map<String,Object>, Object> preCreatedInstancesFromFactory = null;
    Map<String, Object> unwrappedDependecies = unwrapDependencies(dependencies);
    synchronized (factoryClassToMapOfDependenciesToInstance) {
      preCreatedInstancesFromFactory = factoryClassToMapOfDependenciesToInstance.get(factory.getClass());
      if (preCreatedInstancesFromFactory == null){
        preCreatedInstancesFromFactory = Collections.synchronizedMap(new HashMap<Map<String,Object>, Object>());
        factoryClassToMapOfDependenciesToInstance.put((Class<? extends Module<?>>) factory.getClass(), preCreatedInstancesFromFactory);
      }
    }
    Object output = null;
    synchronized (preCreatedInstancesFromFactory) {
      output = preCreatedInstancesFromFactory.get(unwrappedDependecies);    
      if (output == null){
        if (PooledModule.class.isAssignableFrom(factory.getClass())){
          output = createObjectPool((PooledModule<?>)factory, dependencies);
        } else {
          output = factory.create(dependencies);
        }
        preCreatedInstancesFromFactory.put(unwrappedDependecies, output);
      }
    }
    return output;
  }
  
  
  public static void destroy() {
    for (Entry<Class<? extends Module<?>>, Map<Map<String,Object>, Object>> entry : factoryClassToMapOfDependenciesToInstance.entrySet()){
      if (PooledModule.class.isAssignableFrom(entry.getKey())){
        for (Object o : entry.getValue().values()){
          try {
            ((ObjectPool)o).close();
          } catch (Exception e){
            logger.error("Could not close pool for "+entry.getKey().getSimpleName(), e);
            throw new RuntimeException(e);
          }
        }
      }
    }
  }
}
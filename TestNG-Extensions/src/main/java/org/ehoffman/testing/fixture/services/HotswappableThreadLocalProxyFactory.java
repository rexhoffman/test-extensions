package org.ehoffman.testing.fixture.services;

import java.lang.reflect.Proxy;

import net.sf.cglib.proxy.Enhancer;

import org.ehoffman.module.Module;

public class HotswappableThreadLocalProxyFactory {

  @SuppressWarnings("unchecked")
  public static <T> T createHotSwapableThreadLocalTarget(Module<T> module) {
    Class<? extends T> clazz = module.getTargetClass();
    if (clazz.isInterface()){
      return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz,
          HotSwappableProxy.class }, new HotswapableThreadLocalInvocationHandler(module));
    } else {
       Enhancer enhancer = new Enhancer();
       enhancer.setSuperclass(clazz);
       enhancer.setInterfaces(new Class[]{HotSwappableProxy.class});
       enhancer.setCallback(new HotswapableThreadLocalInvocationHandler(module));
       return (T) enhancer.create();
    }
  }

}

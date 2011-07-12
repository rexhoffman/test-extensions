package org.ehoffman.testng.tests;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleGroup;
import org.ehoffman.module.ModuleProvider;

public class CountModule implements ModuleGroup<CountModule.StringHolder> {

  public String getModuleType() {
    return CountModule.class.getSimpleName();
  }
  
  @SuppressWarnings("unchecked")
  public List<Class<? extends ModuleProvider<?>>> getModuleClasses() {
    List<Class<? extends ModuleProvider<?>>> out = new ArrayList<Class<? extends ModuleProvider<?>>>();
    for (int i = 0; i < 10; i++){
      Enhancer enhancer = new Enhancer();
      enhancer.setSuperclass(NullModule.class);
      enhancer.setClassLoader(this.getClass().getClassLoader());
      enhancer.setCallbackTypes(new Class[]{Callback1.class});
      enhancer.setSerialVersionUID(new Long(i));
      enhancer.setInterfaces(new Class[]{Module.class});
      out.add(enhancer.createClass());
    }
    return out;
  }
  

  
  public static class NullModule implements Module<StringHolder> {

    public String getModuleType() {
      return CountModule.class.getSimpleName();
    }
    
    public String getName() {
      return StringHolder.class.getSimpleName();
    }

    public Class<StringHolder> getTargetClass() {
      return StringHolder.class;
    }

    public Map<String, Class<?>> getDependencyDefinition() {
      return null;
    }

    public StringHolder create(Map<String, ?> dependencies) {
      return null;
    }

    public void destroy() {
    }

  }
      
  public static class Callback1 implements MethodInterceptor {    
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      if (method.getName().equals("create")){
         StringHolder holder = new StringHolder();
         holder.setValue(this.getClass().getSimpleName());
         return holder;
      }
      if (method.getName().equals("getModuleType")){
        return "couter";
      }
      return null;
    }
  }
  
  public static class StringHolder{
    private String value;
    public String getValue() {
      return value;
    }
    public void setValue(String value) {
      this.value = value;
    }
  }

  
}
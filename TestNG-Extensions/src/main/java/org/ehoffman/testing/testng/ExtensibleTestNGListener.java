package org.ehoffman.testing.testng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtensibleTestNGListener implements IMethodInterceptor, IInvokedMethodListener, ITestListener {
  
  private static Logger logger = LoggerFactory.getLogger(ExtensibleTestNGListener.class);
  
  private static Map<Class<? extends ExtensibleTestNGListener>, List<? extends Interceptor>> clazzToInterceptors = new HashMap<Class<? extends ExtensibleTestNGListener>, List<? extends Interceptor>>();
  
  public static void setInterceptors(Class<? extends ExtensibleTestNGListener> clazz, List<? extends Interceptor> interceptors){
    clazzToInterceptors.put(clazz, interceptors);
  }
  
  private List<? extends Interceptor> getInterceptors(){
    return clazzToInterceptors.get(this.getClass());
  }
  
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context){
    List<IMethodInstance> output = methods;
    for (Interceptor interceptor : getInterceptors()){
      output = interceptor.intercept(output);
    }
    return output;
  }


  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    for (Interceptor interceptor : getInterceptors()){
      interceptor.beforeInvocation(testResult);
    }

  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    for (Interceptor interceptor : getInterceptors()){
      interceptor.afterInvocation(testResult);
    }
  }

 

  @Override
  public void onFinish(ITestContext context) {
    StringBuilder errors = new StringBuilder();
    boolean error = false;
    for (Interceptor interceptor : getInterceptors()){
      for (String message : interceptor.getConfigErrorMessages()){
        errors.append(message).append("\n");
        error = true;
      }
    }
    for (Interceptor interceptor : getInterceptors()){
      interceptor.shutdown();
    }
    if (error){
      logger.error(errors.toString());
      throw new RuntimeException("Tests are not properly configured "+errors);
    }
  }


  @Override
  public void onTestStart(ITestResult result) {
  }


  @Override
  public void onTestSuccess(ITestResult result) {
  }


  @Override
  public void onTestFailure(ITestResult result) {
  }


  @Override
  public void onTestSkipped(ITestResult result) {
  }


  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
  }


  @Override
  public void onStart(ITestContext context) {
  }
  
}
package org.ehoffman.testing.testng;

import java.util.ArrayList;
import java.util.List;

import org.ehoffman.logback.capture.LogbackCapture;
import org.testng.IMethodInstance;
import org.testng.ITestResult;

public class LogBackInterceptor implements Interceptor {

  private static String log_attribute_key = "Log";
  
  public static String getLogAttributeKey(){
    return log_attribute_key;
  }
  
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods) {
    return methods;
  }

  @Override
  public List<String> getConfigErrorMessages() {
    return new ArrayList<String>();
  }

  @Override
  public void beforeInvocation(ITestResult testResult) {
    System.out.println("Start thread "+Thread.currentThread().getId() +" stack "+ testResult.getName());
    LogbackCapture.start();
  }

  @Override
  public void afterInvocation(ITestResult testResult) {
    System.out.println("Stop thread "+Thread.currentThread().getId() +" stack "+ testResult.getName());
    testResult.setAttribute(LogBackInterceptor.getLogAttributeKey(),LogbackCapture.stop());
  }

  @Override
  public void shutdown() {
  }
 
}

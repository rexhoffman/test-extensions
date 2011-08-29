package org.ehoffman.testing.testng;

import java.util.List;

import org.testng.IMethodInstance;
import org.testng.ITestResult;

public interface Interceptor {

  public List<IMethodInstance> intercept(List<IMethodInstance> methods);
  
  public List<String> getConfigErrorMessages();  
  
  public void beforeInvocation(ITestResult testResult);
  
  public void afterInvocation(ITestResult testResult);
  
  public void shutdown();
}

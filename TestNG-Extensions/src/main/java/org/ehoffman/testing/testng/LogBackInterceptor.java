package org.ehoffman.testing.testng;

import java.util.List;

import org.testng.IMethodInstance;
import org.testng.ITestResult;

public class LogBackInterceptor implements Interceptor {

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getConfigErrorMessages() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void beforeInvocation(ITestResult testResult) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void afterInvocation(ITestResult testResult) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub
    
  }
 
}

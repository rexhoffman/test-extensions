package org.ehoffman.testing.testng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ehoffman.testing.fixture.FixtureInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * When extend this class, as you must to use the Test-Extensions framework, you should call
 * {@link ExtensibleTestNGListener#setInterceptors(Class, List)} method in a static block in your implementation class.  This will configure the set of
 * Listeners you want applied to your tests, examples include {@link LogBackInterceptor}, {@link GroupsInterceptor}, {@link BrokenInterceptor} and the {@link FixtureInterceptor}.<br/>
 * <br/>
 * The only intercepter absolutely needed to use fixtures is the {@link FixtureInterceptor}<br/>
 * <br/>
 * Here is an example implementation<br> <code>
 * public class MyEnforcer extends ExtensibleTestNGListener {<br/>
   &nbsp;&nbsp;private static boolean ideMode = Boolean.valueOf(System.getProperty("java.class.path").contains("org.testng.eclipse"));<br/>
   &nbsp;&nbsp;private static boolean integrationPhase = Boolean.valueOf(System.getProperty("integration_phase"));<br/>
   &nbsp;&nbsp;private static boolean runBrokenTests = false;<br/>
   &nbsp;&nbsp;<br/>
   &nbsp;&nbsp;static {<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;ideMode = false;<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;ExtensibleTestNGListener.setInterceptors(MyEnforcer.class,<br/> 
   &nbsp;&nbsp;&nbsp;&nbsp;Arrays.asList(new LogBackInterceptor(),<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;new BrokenInterceptor(runBrokenTests, Broken.class, ideMode),<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;new GroupsInterceptor(new String[] { "unit", "functional" }, new String[] { "remote-integration" }, integrationPhase, ideMode),<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;new FixtureInterceptor()));<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
   &nbsp;&nbsp;<br/>
   &nbsp;&nbsp;public static boolean isIntegrationPhase() {<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;return integrationPhase;<br/>
   &nbsp;&nbsp;}<br/>
   &nbsp;&nbsp;<br/>
   &nbsp;&nbsp;public static boolean isIdeMode() {<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;return ideMode;<br/>
   &nbsp;&nbsp;}<br/>
   &nbsp;&nbsp;<br/>
   &nbsp;&nbsp;public static boolean isRunBrokenTests() {<br/>
   &nbsp;&nbsp;&nbsp;&nbsp;return runBrokenTests;<br/>
   &nbsp;&nbsp;}<br/>
   }<br/>
   <br/>
   </code>
 * 
 * 
 * @author rex hoffman
 */
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
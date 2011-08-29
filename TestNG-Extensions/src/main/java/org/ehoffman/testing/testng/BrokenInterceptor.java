package org.ehoffman.testing.testng;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.ehoffman.testng.extensions.Broken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.ITestResult;

public class BrokenInterceptor implements Interceptor {

  private static Logger logger = LoggerFactory.getLogger(BrokenInterceptor.class);
  
  private boolean ideMode;
  
  protected void verifyBrokenAnnotation(Annotation brokenAnnotation) {
  }
  
  /**
   * Will contain the class representing a annotation used to mark known breaks,
   * it will attempt to look up a class with a name contained in the
   * "known_break_property" system property.
   * 
   * It may also be null.
   * 
   * A String that does not evaluate to a class with result in a runtime
   * exception.
   */
  private final Class<? extends Annotation> knownBreakClass;
  
  /**
   * Contains the value of the System property "run_known_breaks" converted to a
   * boolean, the default is true.
   */
  protected final boolean run_known_breaks;
  
  public BrokenInterceptor(Boolean runKnownBreaks, Class<? extends Annotation> brokenAnnotation, boolean ideMode) {
    run_known_breaks = runKnownBreaks;
    knownBreakClass = brokenAnnotation;
    ideMode = Boolean.valueOf(System.getProperty("java.class.path").contains("org.testng.eclipse"));
    logger.info("");
  }
  
  private void postProcessBrokenTests(ITestResult testResult) {
    Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
    if (method != null){
      Broken broken = method.getAnnotation(Broken.class);
      if (broken != null) {
        testResult.setAttribute("Known Break", true);
        testResult.setAttribute("True Status", "SUCCESS");
        if (!testResult.isSuccess() && testResult.getStatus()!=ITestResult.SKIP) {
          testResult.setAttribute("True Status", "FAILURE");
          testResult.setStatus(ITestResult.SKIP);
          testResult.setThrowable(null);
        }
      }
    }
  }

  private boolean shouldExecuteMethod(Method method) {
    verifyBrokenAnnotation(method.getAnnotation(knownBreakClass));;
    return (ideMode || run_known_breaks || method.getAnnotation(knownBreakClass) == null);
  }
  
  @Override
  public List<String> getConfigErrorMessages() {
    return new ArrayList<String>();
  }

  @Override
  public void beforeInvocation(ITestResult testResult) {
  }

  @Override
  public void afterInvocation(ITestResult testResult) {
    postProcessBrokenTests(testResult);
  }

  @Override
  public void shutdown() {
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods) {
    List<IMethodInstance> output = new ArrayList<IMethodInstance>();
    for (IMethodInstance instance : methods){
      if (shouldExecuteMethod(instance.getMethod().getConstructorOrMethod().getMethod())){
        output.add(instance);
      }
    }
    return output;
  }

}
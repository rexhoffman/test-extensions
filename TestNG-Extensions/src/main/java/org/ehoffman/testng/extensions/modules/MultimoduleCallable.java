package org.ehoffman.testng.extensions.modules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;

import org.ehoffman.logback.capture.LogbackCapture;
import org.ehoffman.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

public class MultimoduleCallable implements Callable<TestResult> {
  private static Logger logger = LoggerFactory.getLogger(MultimoduleCallable.class);
  private Set<Class<? extends Module<?>>> modulesList;
  private Method                        method;
  private Object                        target;
  private Object[]                      arguments;
  private boolean                       destructive;
  private ITestResult                   parentResult;

  public MultimoduleCallable(Set<Class<? extends Module<?>>> modulesList, Method method, Object target, Object[] arguments,
      boolean destructive, ITestResult parentResult) {
    this.modulesList = modulesList;
    this.method = method;
    this.target = target;
    this.arguments = arguments;
    this.destructive = destructive;
    this.parentResult = parentResult;
  }

  public TestResult call() {
    TestResult result = new TestResult(modulesList, parentResult);
    Set<Module<?>> modules = null;
    try {
      modules = Modules.getModules(modulesList, destructive);
      FixtureContainer.setModuleClassesAndTestResult(modulesList, result);
      result.start();
      LogbackCapture.start();
      method.invoke(target, arguments);      
    } catch (Throwable t) {
      if (t.getClass().isAssignableFrom(InvocationTargetException.class) && t.getCause() != null){
        t = t.getCause();
      }
      result.setStatus(ITestResult.FAILURE);
      result.setThrowable(t);
    } finally {
      result.setAttribute("Log",LogbackCapture.stop());
      result.stop();
      logger.info("unsetting modules");
      Modules.unsetServiceTargetModule(FixtureContainer.getServices().values());
      if (destructive){
        Modules.destroyAll(modules);
      }
      logger.info("unset all modules");
    }
    return result;
  }
}

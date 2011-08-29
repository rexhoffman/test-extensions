package org.ehoffman.testing.testng;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.testing.module.FixtureContainer;
import org.ehoffman.testng.extensions.Fixture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAttributes;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.MethodInstance;

public class FixtureRunnerMethodInterceptor implements IMethodInterceptor, IInvokedMethodListener {
  
  private static Logger logger = LoggerFactory.getLogger(FixtureRunnerMethodInterceptor.class);
  
  private static Map<ITestNGMethod, Set<Class<? extends Module<?>>>> testNGMethodToSetOfModuleClassesForSingleInvocation = Collections.synchronizedMap(new IdentityHashMap<ITestNGMethod, Set<Class<? extends Module<?>>>>());
  
  
  private Iterator<Set<Class<? extends Module<?>>>> fixtureIterator(Fixture fixture) {
    Class<? extends ModuleProvider<?>>[] moduleArray = fixture.factory();
    return FixtureContainer.getDotProductModuleCombinations(Arrays.asList(moduleArray), fixture.destructive());
  }

  /**
   * This enables the fixture annotation.  It will create read the annotations, sort the assigned modules by there getModuleType method in to buckets.
   * It will then run the test from with every combination of modules selecting one from each bucket.
   * 
   * Because the ITestMethod instance does not implement the {@link IAttributes} interface we will use a local static map keyed of of the IMethodInstance object,
   * containing the set of module classes it should use for that invocation of the method.  There will be a corresponding entry in the map for each IMethodInstance returned in the 
   * list that makes use of @Fixture annotation on it's implementing method.
   */
  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
    List<IMethodInstance> output = new ArrayList<IMethodInstance>();
    for (IMethodInstance instance : methods){
      output.addAll(calculateRuns(instance, context));
    }
    return output;
  }
  
  /**
   * 
   * @param instance the inputed IMethodInstance that has not yet been evaluated
   * @param context
   * @return a set containing either the IMethodInstance that was passed in, or set or ITestMethods with a corresponding key in the {@link FixtureRunnerMethodInterceptor#testNGMethodToSetOfModuleClassesForSingleInvocation}
   * map that has a value of the Set or modules that particular instance of IMethodInstance should be run with.  The services the {@link Module}s provide with be available via the {@link FixtureContainer} to the test.
   */
  private List<IMethodInstance> calculateRuns(IMethodInstance instance, ITestContext context){
    List<IMethodInstance> output = new ArrayList<IMethodInstance>();
    Method method = instance.getMethod().getConstructorOrMethod().getMethod();
    if (method != null && method.getAnnotation(Fixture.class) != null) {
      Fixture fixture = method.getAnnotation(Fixture.class);
      Iterator<Set<Class<? extends Module<?>>>> fixtureIterator = fixtureIterator(fixture);
      while (fixtureIterator.hasNext()) {
        ITestNGMethod testNGMethod = instance.getMethod().clone();
        testNGMethod.getInstance();
        IMethodInstance newMultiInstance = new MethodInstance(testNGMethod);
        testNGMethodToSetOfModuleClassesForSingleInvocation.put(testNGMethod, fixtureIterator.next());
        logger.info("added "+testNGMethod.toString()+" with modules of "+testNGMethodToSetOfModuleClassesForSingleInvocation.get(testNGMethod));
        output.add(newMultiInstance);
      }
    } else {
      output.add(instance);
    }
    logger.info("added all "+testNGMethodToSetOfModuleClassesForSingleInvocation);
    return output;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    logger.info("in before method");
    ITestNGMethod testNGmethod = method.getTestMethod();
    Set<Class<? extends Module<?>>> moduleClasses = testNGMethodToSetOfModuleClassesForSingleInvocation.get(testNGmethod);
    if (moduleClasses != null){
      FixtureContainer.setModuleClasses(moduleClasses, testNGmethod.getConstructorOrMethod().getMethod().getAnnotation(Fixture.class).destructive());
    } else {
      FixtureContainer.setModuleClasses(Collections.EMPTY_SET, false);
      FixtureContainer.wipeFixture();
    }
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    if (testNGMethodToSetOfModuleClassesForSingleInvocation.get(method.getTestMethod()) != null) {
      List<String> names = new ArrayList<String>(FixtureContainer.getModuleClassesSimpleName());
      Collections.sort(names);
      testResult.setAttribute("module providers", names);
    }
    logger.info("in after method");
    FixtureContainer.wipeFixture();
  }
}

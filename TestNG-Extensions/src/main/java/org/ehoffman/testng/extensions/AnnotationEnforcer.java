package org.ehoffman.testng.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ehoffman.logback.capture.LogbackCapture;
import org.ehoffman.module.Module;
import org.ehoffman.module.ModuleProvider;
import org.ehoffman.testng.extensions.modules.FixtureContainer;
import org.ehoffman.testng.extensions.modules.Modules;
import org.ehoffman.testng.extensions.modules.MultiResultException;
import org.ehoffman.testng.extensions.modules.MultimoduleCallable;
import org.ehoffman.testng.extensions.modules.TestResult;
import org.ehoffman.testng.extensions.services.FactoryUtil;
import org.testng.IAnnotationTransformer;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

public class AnnotationEnforcer implements IHookable, ITestListener, IInvokedMethodListener, IAnnotationTransformer, IReporter {

  private static final Logger logger = Logger.getLogger(AnnotationEnforcer.class);

  protected void verifyBrokenAnnotation(Annotation brokenAnnotation) {
  }

  /**
   * Contains the value of the System property "integration_phase", converted to
   * boolean, default is false
   */
  protected static boolean integration_phase;

  /**
   * Contains the value of the System property "run_known_breaks" converted to a
   * boolean, the default is true.
   */
  protected static boolean run_known_breaks;
  /**
   * Will be populated with the contents of the system property
   * "unit_test_groups", split on the "," character.
   */
  protected static String[] unit_test_groups;

  /**
   * Will be populated with the contents of the system property
   * "integration_test_groups", split on the "," character.
   */
  protected static String[] integration_test_groups;

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
  private static Class<? extends Annotation> knownBreak;

  private static boolean ideMode = false;
  
  /**
   * contains the union of {@link AnnotationTransformer#unit_test_groups} and
   * {@link AnnotationTransformer#integration_test_groups}, with an additional
   * element of {@link AnnotationTransformer#validation_test_group}
   */
  protected static List<String> validGroups;

  protected static void createValidGroups() {
    validGroups = new ArrayList<String>();
    validGroups.addAll(Arrays.asList(unit_test_groups));
    validGroups.addAll(Arrays.asList(integration_test_groups));
    System.out.println("Valid groups: " + validGroups);
  }

  protected static void configureAnnotationEnforcer(Boolean runKnownBreaks, Class<? extends Annotation> brokenAnnotation, String[] unitTestGroups,
      String[] integrationTestGroups, Boolean integrationsPhase) {
    if (runKnownBreaks != null) {
      AnnotationEnforcer.run_known_breaks = runKnownBreaks;
    } else {
      AnnotationEnforcer.run_known_breaks = (System.getProperty("run_known_breaks") != null);
    }
    if (brokenAnnotation != null) {
      AnnotationEnforcer.knownBreak = brokenAnnotation;
    } else {
      AnnotationEnforcer.knownBreak = Broken.class;
    }
    if (unitTestGroups != null) {
      AnnotationEnforcer.unit_test_groups = unitTestGroups;
    } else {
      AnnotationEnforcer.unit_test_groups = ("" + System.getProperty("unit_test_groups")).split(",");
    }
    if (integrationTestGroups != null) {
      AnnotationEnforcer.integration_test_groups = integrationTestGroups;
    } else {
      AnnotationEnforcer.integration_test_groups = ("" + System.getProperty("integration_test_groups")).split(",");
    }
    if (integrationsPhase != null) {
      AnnotationEnforcer.integration_phase = integrationsPhase;
    } else {
      AnnotationEnforcer.integration_phase = Boolean.valueOf(System.getProperty("integration_phase"));
    }
    ideMode =  Boolean.valueOf(System.getProperty("java.class.path").contains("org.testng.eclipse"));
    createValidGroups();
  }

  /**
   * Before TestNG runs all the tests in this phase, it will call $
   * {@link AnnotationTransformer#transform(ITestAnnotation, Class, Constructor, Method)}
   * on all AnnotationElements (Methods, Classes, Constructors) marked with a
   * {@link Test} annotation. If any of those annotations contain groups that
   * are not in the list of {@link AnnotationEnforcer#validGroups} a string
   * marking the error will be added to this List.
   */
  private static List<String> badTestGroupTests = new ArrayList<String>();

  /**
   * @return an unmodifiable list that contains a String for each @Test
   *         annotation that has a group value that is not contained in $
   *         {@link AnnotationEnforcer#validGroups} calculated at
   *         construction of this object by TestNG.
   */
  public static List<String> badTestGroupsTests() {
    return Collections.unmodifiableList(badTestGroupTests);
  }

  /**
   * @return a boolean that lets the tests know if they are being run in the
   *         integration test or unit test phase.
   */
  public static boolean isIntegrationTestPhase() {
    return integration_phase;
  }

  /**
   * Returns the list of groups, contained in "groups" that are not contained in
   * "listOfGoodGroups"
   * 
   * @param groups
   *          an array containing a the groups that a test belogs to.
   * @param listOfGroups
   *          a list of groups that should be a super set of the string contain
   *          in groups
   * @return groups, minus the intersection of list of groups.
   */
  private List<String> checkGroups(String[] groups, List<String> listOfGoodGroups) {
    List<String> badGroups = new ArrayList<String>();
    for (String s : groups) {
      if (!listOfGoodGroups.contains(s)) {
        logger.info("Noting bad group " + s);
        badGroups.add(s);
      }
    }
    return badGroups;
  }

  /**
   * Given an @Test annotation, validates that all the groups on the annotation
   * are contained in the list of good groups calculated during the construction
   * of this object.
   * 
   * Any bad groups are stored in a array in a static variable brokenMethods
   * internally. They are accessible with
   * {@link AnnotationTransformer#getBrokenTestList()}.
   * 
   * @param annotation
   *          the @Test annotations meta-data
   * @param source
   *          the string
   */
  private void checkMethodGroupsAreAllCorrect(String[] groups, String source) {
    List<String> names = checkGroups(groups, validGroups);
    if (!names.isEmpty()) {
      badTestGroupTests.add(source + " has an Test annotation with bad groups of " + names.toString());
    }
  }

  /**
   * Determines if any group in groups is also contained in groupsToRun
   * 
   * @param groups
   *          list of groups the test belongs to.
   * @param groupsToRun
   *          list of groups to run during this test phase.
   * @return true iff one of more groups in groups is also in groupsToRun
   */
  private boolean inGroups(String[] groups, List<String> groupsToRun) {
    for (String group : groups) {
      for (String groupToRun : groupsToRun) {
        if (group.trim().equalsIgnoreCase(groupToRun.trim()))
          return true;
      }
    }
    return false;
  }

  /**
   * Determines if a test should be run during this test phase and disables it
   * if it should not be.
   * 
   * @param annotation
   *          that may be disabled
   * @param element
   *          the element on which this annotation resides.
   */
  private boolean shouldSkip(Test annotation, AnnotatedElement element) {
    if (ideMode) return false;
    if (knownBreak != null && element.getAnnotation(knownBreak) != null) {
      verifyBrokenAnnotation(element.getAnnotation(knownBreak));
      if (!run_known_breaks) {
        return true;
      }
    }
    if (integration_phase && !inGroups(annotation.groups(), Arrays.asList(integration_test_groups))) {
      return true;
    }
    if (!integration_phase && !inGroups(annotation.groups(), Arrays.asList(unit_test_groups))) {
      return true;
    }
    return false;
  }

  private Iterator<Set<Class<? extends Module<?>>>> fixtureIterator(Fixture fixture) {
    Class<? extends ModuleProvider<?>>[] moduleArray = fixture.factory();
    return Modules.getDotProductModuleCombinations(Arrays.asList(moduleArray), fixture.destructive());
  }

  private void addErrorToResults(List<TestResult> results, Throwable throwable, ITestResult parentResult) {
    TestResult brokenTest = new TestResult(new HashSet<Class<? extends Module<?>>>(), parentResult);
    brokenTest.setThrowable(throwable);
    results.add(brokenTest);
  }

  private static final String INDIVIDUAL_RESULTS = "individual_Results";
  
  private void processResults(ITestResult result, List<TestResult> individualResults) {
    List<TestResult> failures = new ArrayList<TestResult>();
    for (TestResult iresult : individualResults) {
      if (iresult.getStatus() == ITestResult.FAILURE || iresult.getThrowable() != null) {
        result.setStatus(ITestResult.FAILURE);
        failures.add(iresult);
      }
    }
    if (failures.size() > 0) {
      MultiResultException exception = new MultiResultException(failures);
      result.setThrowable(exception);
    }
    result.setAttribute(INDIVIDUAL_RESULTS, individualResults);
  }

  private Integer getMaxThreadCount(){
    String count_string = System.getProperty("max_threadcount");
    try {
      int val = Integer.parseInt(count_string);
      return val;
    } catch (NumberFormatException e){
      return null;
    }
  }
  
  private static final String logAttributeName = "Log";
  
  private void runMultpleTimesWithFixture(final IHookCallBack icb, ITestResult testResult, Fixture fixture) {
    ExecutorService exService;
    if (getMaxThreadCount() == null){
      exService = Executors.newCachedThreadPool();
    } else if (getMaxThreadCount() == 1){
      exService = Executors.newSingleThreadExecutor();
    } else {
      exService = Executors.newFixedThreadPool(getMaxThreadCount());
    }
    CompletionService<TestResult> service = new ExecutorCompletionService<TestResult>(exService);
    Iterator<Set<Class<? extends Module<?>>>> fixtureIterator = fixtureIterator(fixture);
    boolean destructive = fixture.destructive();
    List<Future<TestResult>> resultFutures = new ArrayList<Future<TestResult>>();
    boolean firstRun = true;
    while (fixtureIterator.hasNext()) {
      Set<Class<? extends Module<?>>> modules = fixtureIterator.next();
      if (firstRun) {
        FixtureContainer.createServicesIfNeeded(modules);
        firstRun = false;
      }
      @SuppressWarnings("deprecation")
      MultimoduleCallable callable = new MultimoduleCallable(modules, testResult.getMethod().getMethod(), testResult.getInstance(),
          testResult.getParameters(), destructive, testResult);
      resultFutures.add(service.submit(callable));
    }
    List<TestResult> results = new ArrayList<TestResult>();
    for (Future<TestResult> resultFuture : resultFutures) {
      try {
        results.add(resultFuture.get());
      } catch (ExecutionException e){
        e.printStackTrace();
        Throwable t = e;
        if (t.getCause() != null){
          t = t.getCause();
        }
        addErrorToResults(results, t, testResult);
      } catch (Throwable t) {
        t.printStackTrace();
        addErrorToResults(results, t, testResult);
      }
    }
    processResults(testResult, results);
  }

  private void postProcessBrokenTests(Method method, ITestResult testResult) {
    Broken broken = method.getAnnotation(Broken.class);
    if (broken != null) {
      testResult.setAttribute("Known Break", true);
      testResult.setAttribute("True Status", "SUCCESS");
      if (!testResult.isSuccess()) {
        logger.error("\n@Broken method: " + method.toString() + " will be marked as a skip.  Fix this test\n");
        testResult.setAttribute("True Status", "FAILURE");
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(null);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void run(final IHookCallBack icb, ITestResult testResult) {
    AnnotatedElement element = testResult.getMethod().getMethod();
    Test annotation = (Test) element.getAnnotation(Test.class);
    if (!ideMode) checkMethodGroupsAreAllCorrect(annotation.groups(), element.toString());
    if (!shouldSkip(annotation, element)) {
      Fixture fixture = testResult.getMethod().getMethod().getAnnotation(Fixture.class);
      if (fixture != null) {
        runMultpleTimesWithFixture(icb, testResult, fixture);
      } else {
        LogbackCapture.start();
        try {
          icb.runTestMethod(testResult);
        } finally {
          testResult.setAttribute(logAttributeName, LogbackCapture.stop());
        }
      }
    } else {
      testResult.setStatus(ITestResult.SKIP);
    }
    postProcessBrokenTests(testResult.getMethod().getMethod(), testResult);
  }

  /*********************************************************************************/
  /* ITestListenerMethods */
  /*********************************************************************************/

  private void failIfIncorrectGroups() {
    if (badTestGroupsTests() != null && !badTestGroupsTests().isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("Running verifyCorrectSetup, of TestNGConfigTest, failed annotation config for these methods: \n");
      for (String error : badTestGroupsTests()) {
        builder.append(error);
        builder.append("\n");
      }
      String error = builder.toString();
      logger.error(error);
      throw new RuntimeException("Incorrect group values:");
    }
  }

  private void explodeIResultMap(IResultMap results){
    Map<ITestNGMethod, List<ITestResult>> map = new HashMap<ITestNGMethod, List<ITestResult>>();
    for (ITestNGMethod method : results.getAllMethods()){
      //ITestResult
      Set<ITestResult> resultSet = results.getResults(method);
      for (ITestResult result : resultSet){
        @SuppressWarnings("unchecked")
        List<TestResult> individualResults = (List<TestResult>)result.getAttribute(INDIVIDUAL_RESULTS);
        if (map.get(method) == null){
          map.put(method, new ArrayList<ITestResult>());
        }
        if (individualResults != null){
          map.get(method).addAll(individualResults);
        } else {
          map.get(method).add(result);
        }
      }
    }
    for (ITestNGMethod method : map.keySet()){
      results.removeResult(method);
    }
    for (ITestNGMethod method : map.keySet()){
      ITestNGMethod method2 = method.clone();
      for (ITestResult result : map.get(method)){
        results.addResult(result, method2);
      }
    }
  }
  
  
  public void onFinish(ITestContext context) {
    explodeIResultMap(context.getFailedTests());
    explodeIResultMap(context.getFailedButWithinSuccessPercentageTests());
    explodeIResultMap(context.getPassedTests());
    System.out.println("New Passed Tests: "+context.getPassedTests());
    explodeIResultMap(context.getSkippedTests());
    Modules.destroyAll();
    FactoryUtil.destroy();
    failIfIncorrectGroups();
  }


  public void onStart(ITestContext arg0) {
  }

  public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
  }

  public void onTestFailure(ITestResult arg0) {
  }

  public void onTestSkipped(ITestResult result) {
  }

  public void onTestStart(ITestResult arg0) {
  }

  public void onTestSuccess(ITestResult result) {
  }

  /*********************************************************************************/
  /* IInvokedMethodListener */
  /*********************************************************************************/

  @SuppressWarnings("deprecation")
  private void postProcessBrokenTests(IInvokedMethod method, ITestResult testResult) {
    Broken broken = method.getTestMethod().getMethod().getAnnotation(Broken.class);
    if (broken != null) {
      testResult.setAttribute("Known Break", true);
      testResult.setAttribute("True Status", "SUCCESS");
      if (!testResult.isSuccess()) {
        logger.error("\n@Broken method: " + method.getTestMethod().getMethod().toString() + " will be marked as a skip.  Fix this test\n");
        testResult.setAttribute("True Status", "FAILURE");
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(null);
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    postProcessBrokenTests(method, testResult);
    Test test = method.getTestMethod().getMethod().getAnnotation(Test.class);
    if (test != null && shouldSkip(test, method.getTestMethod().getMethod())) {
      testResult.setStatus(ITestResult.SKIP);
    }
  }

  public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {
  }

  /*********************************************************************************/
  /* IAnnotationTransformer */
  /*********************************************************************************/

  /**
   * Sets the annotation's enabled variable to false, disabling the test during
   * this test run.
   * 
   * @param annotation
   *          the annotation to disable
   * @param element
   *          the element on which this annotation resides.
   */
  private void disable(ITestAnnotation annotation, AnnotatedElement element) {
    logger.info("disabling: " + element.toString());
    annotation.setEnabled(false);
  }

  /**
   * Determines if a test should be run during this test phase and disables it
   * if it should not be.
   * 
   * @param annotation
   *          that may be disabled
   * @param element
   *          the element on which this annotation resides.
   */
  private void disableTestIfNotMeantToRun(ITestAnnotation annotation, AnnotatedElement element) {
    if (knownBreak != null && element.getAnnotation(knownBreak) != null) {
      if (!run_known_breaks) {
        disable(annotation, element);
      } else {
        annotation.setSkipFailedInvocations(true);
      }
    }
    if (integration_phase && !inGroups(annotation.getGroups(), Arrays.asList(integration_test_groups))) {
      disable(annotation, element);
    }
    if (!integration_phase && !inGroups(annotation.getGroups(), Arrays.asList(unit_test_groups))) {
      disable(annotation, element);
    }
  }

  /**
   * @see IAnnotationTransformer#transform(ITestAnnotation, Class, Constructor,
   *      Method)
   */
  public void transform(ITestAnnotation annotation, @SuppressWarnings("rawtypes") Class testClass, @SuppressWarnings("rawtypes") Constructor constructor, Method testMethod) {
    AnnotatedElement element = testClass;
    if (element == null) {
      element = constructor;
    }
    if (element == null) {
      element = testMethod;
    }
    logger.trace("transforming " + element.toString());
    checkMethodGroupsAreAllCorrect(annotation.getGroups(), element.toString());
    disableTestIfNotMeantToRun(annotation, element);
  }

  
  /**
   * IReport Implementation.  Rock it.
   */
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    new JUnitReportReporter().generateReport(xmlSuites, suites, outputDirectory);    
  }
}

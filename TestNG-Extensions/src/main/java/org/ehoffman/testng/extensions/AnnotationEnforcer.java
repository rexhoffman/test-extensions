package org.ehoffman.testng.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ehoffman.testing.module.FixtureContainer;
import org.ehoffman.testng.extensions.services.FactoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class AnnotationEnforcer implements ITestListener, IInvokedMethodListener, IAnnotationTransformer, IReporter {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationEnforcer.class);

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
    logger.info("Valid groups: " + validGroups);
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
        if (group.trim().equalsIgnoreCase(groupToRun.trim())) {
          return true;
        }
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
    if (ideMode) {
      return false;
    }
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


  @Override
  public void onFinish(ITestContext context) {
    logger.trace("New Passed Tests: "+context.getPassedTests());
    FixtureContainer.destroyAll();
    FactoryUtil.destroy();
    failIfIncorrectGroups();
  }


  @Override
  public void onStart(ITestContext arg0) {
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
  }

  @Override
  public void onTestFailure(ITestResult arg0) {
  }

  @Override
  public void onTestSkipped(ITestResult result) {
  }

  @Override
  public void onTestStart(ITestResult arg0) {
  }

  @Override
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
      if (!testResult.isSuccess() && testResult.getStatus()!=ITestResult.SKIP) {
        testResult.setAttribute("True Status", "FAILURE");
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(null);
      }
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    postProcessBrokenTests(method, testResult);
    Test test = method.getTestMethod().getMethod().getAnnotation(Test.class);
    if (test != null && shouldSkip(test, method.getTestMethod().getMethod())) {
      testResult.setStatus(ITestResult.SKIP);
    }
  }

  @Override
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
  @Override
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
  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    new JUnitReportReporter().generateReport(xmlSuites, suites, outputDirectory);
  }
}

package org.ehoffman.testing.testng;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.Test;

public class GroupsInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(GroupsInterceptor.class);

  /**
   * Contains the value of the System property "integration_phase", converted to
   * boolean, default is false
   */
  protected boolean integration_phase;

  /**
   * Will be populated with the contents of the system property
   * "unit_test_groups", split on the "," character.
   */
  protected String[] unit_test_groups;

  /**
   * Will be populated with the contents of the system property
   * "integration_test_groups", split on the "," character.
   */
  protected String[] integration_test_groups;


  private boolean ideMode = false;

  public GroupsInterceptor(String[] unit_test_groups, String[] integration_test_groups, boolean integrationPhase, boolean ideMode){
    this.unit_test_groups = unit_test_groups;
    this.integration_test_groups = integration_test_groups;
    this.integration_phase = integrationPhase;
    this.ideMode = ideMode;
    createValidGroups();
  }
  
  
  /**
   * contains the union of {@link AnnotationTransformer#unit_test_groups} and
   * {@link AnnotationTransformer#integration_test_groups}, with an additional
   * element of {@link AnnotationTransformer#validation_test_group}
   */
  protected List<String> validGroups;

  protected void createValidGroups() {
    validGroups = new ArrayList<String>();
    validGroups.addAll(Arrays.asList(unit_test_groups));
    validGroups.addAll(Arrays.asList(integration_test_groups));
    logger.info("Valid groups: " + validGroups);
  }

  /**
   * Before TestNG runs all the tests in this phase, it will call $
   * {@link AnnotationTransformer#transform(ITestAnnotation, Class, Constructor, Method)}
   * on all AnnotationElements (Methods, Classes, Constructors) marked with a
   * {@link Test} annotation. If any of those annotations contain groups that
   * are not in the list of {@link GroupsInterceptor#validGroups} a string
   * marking the error will be added to this List.
   */
  private List<String> badTestGroupTests = new ArrayList<String>();

  /**
   * @return an unmodifiable list that contains a String for each @Test
   *         annotation that has a group value that is not contained in $
   *         {@link GroupsInterceptor#validGroups} calculated at
   *         construction of this object by TestNG.
   */
  public List<String> badTestGroupsTests() {
    return Collections.unmodifiableList(badTestGroupTests);
  }

  /**
   * @return a boolean that lets the tests know if they are being run in the
   *         integration test or unit test phase.
   */
  public boolean isIntegrationTestPhase() {
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

  private boolean shouldRun(String[] groups) {
    if (integration_phase && !inGroups(groups, Arrays.asList(integration_test_groups)) && !ideMode) {
      return false;
    }
    if (!integration_phase && !inGroups(groups, Arrays.asList(unit_test_groups)) && !ideMode) {
      return false;
    }
    return true;
  }

  @Override
  public List<IMethodInstance> intercept(List<IMethodInstance> methods) {
    List<IMethodInstance> output = new ArrayList<IMethodInstance>();
    for (IMethodInstance method : methods){
      checkMethodGroupsAreAllCorrect(method.getMethod().getGroups(), method.getMethod().getConstructorOrMethod().getName());
      if (shouldRun(method.getMethod().getGroups())){
        output.add(method);
      }
    }
    return output;
  }

  @Override
  public List<String> getConfigErrorMessages() {
    return badTestGroupsTests();
  }

  @Override
  public void beforeInvocation(ITestResult testResult) {
  }

  @Override
  public void afterInvocation(ITestResult testResult) {
  }

  @Override
  public void shutdown() {
  }
}

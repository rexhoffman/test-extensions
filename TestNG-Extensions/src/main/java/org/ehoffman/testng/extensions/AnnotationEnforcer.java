package org.ehoffman.testng.extensions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ehoffman.testing.fixture.FixtureInterceptor;
import org.ehoffman.testing.testng.BrokenInterceptor;
import org.ehoffman.testing.testng.ExtensibleTestNGListener;
import org.ehoffman.testing.testng.GroupsInterceptor;
import org.ehoffman.testing.testng.LogBackInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated please extend {@link ExtensibleTestNGListener} and call the {@link ExtensibleTestNGListener#setInterceptors(List)} in a static block.
 * 
 * @author rexhoffman
 */
public class AnnotationEnforcer extends ExtensibleTestNGListener {

  private static final Logger logger = LoggerFactory.getLogger(AnnotationEnforcer.class);
  private static boolean ideMode = false;
  private static boolean run_known_breaks = false; 
  private static Class<? extends Annotation> knownBreakAnnotation = null;
  
  public static boolean isIntegrationPhase(){
    return integrationPhase;
  }
  
  public static boolean isIdeMode(){
    return ideMode;
  }
  
  public static boolean isRunBrokenTests(){
    return run_known_breaks;
  }
  

  /**
   * Contains the value of the System property "integration_phase", converted to
   * boolean, default is false
   */
  protected static boolean integrationPhase;

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

  protected static void configureAnnotationEnforcer(Boolean runKnownBreaks, Class<? extends Annotation> brokenAnnotation, String[] unitTestGroups, String[] integrationTestGroups, Boolean integrationsPhase, Class<? extends AnnotationEnforcer> clazz) {
    if (runKnownBreaks != null) {
      AnnotationEnforcer.run_known_breaks = runKnownBreaks;
    } else {
      AnnotationEnforcer.run_known_breaks = (System.getProperty("run_known_breaks") != null);
    }
    if (brokenAnnotation != null) {
      AnnotationEnforcer.knownBreakAnnotation = brokenAnnotation;
    } else {
      AnnotationEnforcer.knownBreakAnnotation = Broken.class;
    }
    if (unitTestGroups != null) {
      AnnotationEnforcer.unit_test_groups = unitTestGroups;
    } else {
      String value = System.getProperty("unit_test_groups");
      if (value != null && !"".equals(value)){
        AnnotationEnforcer.unit_test_groups = value.split(",");
      } else {
        AnnotationEnforcer.unit_test_groups = new String[] {"unit","functional"};
      }
    }
    if (integrationTestGroups != null) {
      AnnotationEnforcer.integration_test_groups = integrationTestGroups;
    } else {
      String value = System.getProperty("integration_test_groups");
      if (value != null && !"".equals(value)){
        AnnotationEnforcer.unit_test_groups = value.split(",");
      } else {
        AnnotationEnforcer.unit_test_groups = new String[] {"remote-integration"};
      }
    }
    if (integrationsPhase != null) {
      AnnotationEnforcer.integrationPhase = integrationsPhase;
    } else {
      AnnotationEnforcer.integrationPhase = Boolean.valueOf(System.getProperty("integration_phase"));
    }
    if (System.getProperty("java.class.path").contains("org.testng.eclipse")){
      ideMode = true;
    }
    ExtensibleTestNGListener.setInterceptors(clazz, Arrays.asList(
        new LogBackInterceptor(),
        new BrokenInterceptor(run_known_breaks, knownBreakAnnotation, ideMode),
        new GroupsInterceptor(unit_test_groups, integration_test_groups, integrationPhase, ideMode),
        new FixtureInterceptor()));
    createValidGroups();
  }
}

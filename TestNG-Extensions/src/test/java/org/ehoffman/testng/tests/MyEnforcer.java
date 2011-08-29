package org.ehoffman.testng.tests;

import java.util.Arrays;

import org.ehoffman.testing.module.ExtensibleTestNGListener;
import org.ehoffman.testing.testng.BrokenInterceptor;
import org.ehoffman.testing.testng.FixtureInterceptor;
import org.ehoffman.testing.testng.GroupsInterceptor;
import org.ehoffman.testng.extensions.Broken;

public class MyEnforcer extends ExtensibleTestNGListener {
  private static boolean ideMode = Boolean.valueOf(System.getProperty("java.class.path").contains("org.testng.eclipse"));
  private static boolean integrationPhase = Boolean.valueOf(System.getProperty("integration_phase"));
  private static boolean runBrokenTests = false; 
  
  static {
    ideMode = true;
    ExtensibleTestNGListener.setInterceptors(Arrays.asList(
    new BrokenInterceptor(runBrokenTests, Broken.class, ideMode),
    new GroupsInterceptor(new String[] {"unit","functional"}, new String[] {"remote-integration"}, integrationPhase, ideMode),
    new FixtureInterceptor()));
  }
  
  public static boolean isIntegrationPhase(){
    return integrationPhase;
  }
  
  public static boolean isIdeMode(){
    return ideMode;
  }
  
  public static boolean isRunBrokenTests(){
    return runBrokenTests;
  }
}

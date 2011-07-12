package org.ehoffman.testng.tests;

import org.ehoffman.testng.extensions.AnnotationEnforcer;

public class MyEnforcer extends AnnotationEnforcer {
  static {
    configureAnnotationEnforcer(false, null, new String[]{"unit","local-integration"}, new String[]{"remote-integration"}, null);
  }

}

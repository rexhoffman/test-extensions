package org.ehoffman.testing.tests.deprecated;

import org.ehoffman.testng.extensions.AnnotationEnforcer;
import org.ehoffman.testng.extensions.Broken;

public class MyAnnotationEnforcer extends AnnotationEnforcer {
  static {
    configureAnnotationEnforcer(false, Broken.class, new String[]{"unit","local-integration"}, new String[]{"remote-integration"}, true, MyAnnotationEnforcer.class);
  }
}

package org.ehoffman.testing.module.webapp;

import org.ehoffman.testng.extensions.AnnotationEnforcer;

public class MyEnforcer extends AnnotationEnforcer {
  static {
    configureAnnotationEnforcer(false, null, new String[]{"unit","functional"}, new String[]{"remote"}, null, MyEnforcer.class);
  }
}

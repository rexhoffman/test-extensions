package org.ehoffman.testng.extensions.modules;

import java.util.List;


public class MultiResultException extends Exception {

  private static final long serialVersionUID = 1L;
  List<TestResult> causes;

  public MultiResultException(List<TestResult> causes) {
    this.causes = causes;
  }

  public String getMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append("This test failed for multiple reasons, as it was run multiple times, each error will be enumerated now:\n\n");
    for (TestResult result : causes) {
      builder.append("    Error while running with: " + result.getModuleClasses() + " modules:\n    Error message is: " + result.getThrowable().getMessage() + "\n");
      Throwable t = result.getThrowable();
      while (t != null){
        for (StackTraceElement element : t.getStackTrace()) {
          builder.append("      " + element.toString() + "\n");
        }
        t = t.getCause();
        if (t != null){
          builder.append("   caused by: "+t.getMessage()+"\n");
        }
      }
      builder.append("\n");  
    }
    return builder.toString();
  }

}
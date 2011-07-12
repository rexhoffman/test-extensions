package org.ehoffman.testng.extensions;

@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value = { java.lang.annotation.ElementType.METHOD })
public @interface Broken {
  public final static String IGNORE_KNOWN_BREAKS = "run_known_breaks";

  public String developer();

  public String issueInTracker();
}

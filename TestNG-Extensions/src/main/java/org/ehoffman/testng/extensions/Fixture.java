package org.ehoffman.testng.extensions;

import org.ehoffman.module.ModuleProvider;

@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(value = { java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE,java.lang.annotation.ElementType.CONSTRUCTOR })
public @interface Fixture {
  public Class<? extends ModuleProvider<?>>[] factory();

  public boolean destructive() default false;
}

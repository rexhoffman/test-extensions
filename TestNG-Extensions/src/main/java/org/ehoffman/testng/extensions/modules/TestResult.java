package org.ehoffman.testng.extensions.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ehoffman.module.Module;
import org.testng.ITestResult;

public class TestResult extends org.testng.internal.TestResult implements ITestResult{
  private static final long serialVersionUID = 1L;

  private final Set<Class<? extends Module<?>>> moduleClasses;
  private final CharSequence simpleNames;
  
  private long m_startMillis;
  private long m_endMillis;
  
  public TestResult(Set<Class<? extends Module<?>>> moduleClasses , ITestResult parentResult){
    super(parentResult.getTestClass(), parentResult.getInstance(), parentResult.getMethod(), null, System.currentTimeMillis(), 0L);
    this.moduleClasses = Collections.unmodifiableSet(moduleClasses);
    this.simpleNames = nicelyOutputedModules();
    this.setAttribute("Modules", simpleNames);
  }

  private CharSequence nicelyOutputedModules(){
    List<String> simpleNamesList = new ArrayList<String>();
    for (Class<? extends Module<?>> module : moduleClasses){
      simpleNamesList.add(module.getSimpleName());
    }
    Collections.sort(simpleNamesList);
    StringBuilder builder = new StringBuilder();
    builder.append(simpleNamesList);
    return builder;
  }
  
  
  public String getName(){
    return super.getName()+" "+simpleNames;
  }
  
  public Set<Class<? extends Module<?>>> getModuleClasses(){
    return moduleClasses;
  }
    
  public void stop(){
    m_endMillis = System.currentTimeMillis();
  }

  public void start(){
    m_startMillis = System.currentTimeMillis();
  }
  
  /**
   * @return Returns the endMillis.
   */
  @Override
  public long getEndMillis() {
    return m_endMillis;
  }

  /**
   * @return Returns the startMillis.
   */
  @Override
  public long getStartMillis() {
    return m_startMillis;
  }
  
}

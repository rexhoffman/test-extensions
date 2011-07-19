package org.ehoffman.embedded.jetty;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.ehoffman.webapp.invoker.Application;
import org.ehoffman.webapp.invoker.ApplicationUtil;
import org.ehoffman.webapp.invoker.lookups.ApplicationLookUpMethod;
import org.ehoffman.webapp.invoker.lookups.EclipseProjectWithMarkingProperty;
import org.ehoffman.webapp.invoker.lookups.ScanTargetDir;
import org.testng.annotations.Test;


public class TestJettyStartUp {

  private CharSequence read(URL url){
    try {
      Reader reader = new InputStreamReader(url.openStream());
      char[] buffer = new char[1024];
      StringBuilder builder = new StringBuilder();
      int charsRead = reader.read(buffer);
      while (charsRead != -1){
        builder.append(buffer, 0, charsRead);
        charsRead = reader.read();
      }
      return builder;
    } catch (IOException ioe){
      throw new RuntimeException("Couldn't read landing page:"+url);
    }
  }

  @Test()
  public void runJetty() throws Exception {
    if (System.getProperty("java.class.path").contains("org.testng.eclipse")){ //running in eclipse with testng
      runJettyWithLookup(EclipseProjectWithMarkingProperty.class);
    }
    runJettyWithLookup(ScanTargetDir.class);
  }

  public void runJettyWithLookup(Class<? extends ApplicationLookUpMethod> lookupClass) throws Exception {
    List<Class<? extends ApplicationLookUpMethod>> methods = new ArrayList<Class<? extends ApplicationLookUpMethod>>();
    methods.add(lookupClass);
    Application application = ApplicationUtil.discoverApplicationByName(methods, "EmbeddedWebapp");
    application.start();
    URL url = application.getDefaultRootUrl();
    assertThat(read(url).toString()).contains("Rex says that the time is now : ");
    application.shutdown();
    assertThat(application.getDefaultRootUrl()).isNull();
    try {
      assertThat(read(url).toString()).doesNotContain("Rex says that the time is now : ");
    } catch (RuntimeException e){
      assertThat(e).hasMessage("Couldn't read landing page:"+url.toString());
    }
  }

}

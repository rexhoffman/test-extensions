package org.ehoffman.webapp.invoker.lookups;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.ehoffman.webapp.invoker.Application;
import org.ehoffman.webapp.invoker.ApplicationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EclipseProjectWithMarkingProperty implements ApplicationLookUpMethod {

  private static final Logger logger = LoggerFactory.getLogger(EclipseProjectWithMarkingProperty.class);

  protected static final String[] defaultLocations = new String[] { "/src/main/webapp/", "/WebContent/" };


  private File[] findDefaultContentLocations(File projectDir){
    List<File> output = new ArrayList<File>();
    for (String location : defaultLocations){
      File file = new File(projectDir, location);
      if (file != null && file.exists() && file.isDirectory()){
        output.add(file);
      }
    }
    return output.toArray(new File[output.size()]);
  }


  @Override
  public List<Application> lookup() {
    Enumeration<URL> urls = null;
    try {
      urls = ApplicationUtil.class.getClassLoader().getResources("webapp.properties");
    } catch (IOException io_exception){
      throw new RuntimeException("could not determine file of resource, while try to calculate project location", io_exception);
    }
    List<Application> applications = new ArrayList<Application>();
    for (URL url : Collections.list(urls)){
      try {
        logger.info("started by "+url);
        File basetxt = new File(url.toURI());
        Properties props = new Properties();
        props.load(new FileReader(basetxt));
        File projectBase =  basetxt.getParentFile().getParentFile().getParentFile();
        logger.info("running for "+projectBase);
        logger.info("Context root "+props.getProperty("contextRoot"));
        applications.add(Application.buildExploded(findDefaultContentLocations(projectBase)).setContextRoot(props.getProperty("contextRoot")).build());
      } catch (URISyntaxException exception){
        throw new RuntimeException("could not determine file of resource, while try to calculate project location");
      } catch (IOException io_exception){
        throw new RuntimeException("could not determine file of resource, while try to calculate project location", io_exception);
      }
    }
    return applications;
  }

  @Override
  public Application lookupByName(String contextRoot) {
    for (Application application : lookup()){
      if (contextRoot != null && contextRoot.equals(application.getContextRoot())){
        return application;
      }
    }
    return null;
  }

}

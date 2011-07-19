package org.ehoffman.webapp.invoker.lookups;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ehoffman.webapp.invoker.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanTargetDir implements ApplicationLookUpMethod {

  private static final Logger logger = LoggerFactory.getLogger(ScanTargetDir.class);

  private File calculateTargetDirectory() {
    return new File("target/");
  }

  @Override
  public List<Application> lookup() {
    File file = calculateTargetDirectory();
    String[] warsNames = file.list();
    List<Application> applications = new ArrayList<Application>();
    for (String fileName : warsNames) {
      if (fileName.toLowerCase().endsWith(".war")) {
        applications.add(new Application(new File(file, fileName)));
      }
    }
    return applications;
  }

  @Override
  public Application lookupByName(String contextRoot) {
    File file = calculateTargetDirectory();
    logger.info("Warfile root is: " + file.getAbsolutePath());
    String[] warsNames = file.list();
    for (String name : warsNames) {
      logger.info("looking at: " + name);
      if (name.toLowerCase().contains(contextRoot.toLowerCase()) && name.toLowerCase().endsWith(".war")) {
        return new Application(new File(file, name));
      }
    }
    return null;
  }

}

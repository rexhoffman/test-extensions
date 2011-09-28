package org.ehoffman.testing.module.webdriver;

import org.openqa.grid.common.GridDocHelper;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.exception.GridConfigurationException;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.web.Hub;
import org.testng.annotations.Test;

public class WebDriverHubModule {

  private Hub h;
  private SelfRegisteringRemote remote;
  
  private void stopHub() throws Exception {
    h.stop();
  }
  
  private void stopRemote(){
    remote.stopRemoteServer();
  }
  
  private void lauchGrid() throws Exception{
    try {
      GridHubConfiguration c = GridHubConfiguration.build(new String[] {"-port","0"});
      h = new Hub(c);
      h.start();
      System.out.println("Port is: "+h.getUrl());
    } catch (GridConfigurationException e) {
      e.printStackTrace();
      GridDocHelper.printHelp(e.getMessage());
    }
  }
  
  private void lauchNode(String huburl) throws Exception {
    try {      
      RegistrationRequest c = RegistrationRequest.build(new String[] {"-role","webdriver","-hub",huburl,"-port","0"});
      remote = new SelfRegisteringRemote(c);
      remote.startRemoteServer();
      remote.startRegistrationProcess();
    } catch (GridConfigurationException e) {
      e.printStackTrace();
      GridDocHelper.printHelp(e.getMessage());
    }
  }
  
  @Test
  public void startStopTest() throws Exception {
    lauchGrid();
    lauchNode("http://localhost:4444/grid/register");
    stopRemote();
    stopHub();
  }
  
  
  
  
}

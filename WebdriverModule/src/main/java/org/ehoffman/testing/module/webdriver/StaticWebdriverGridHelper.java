package org.ehoffman.testing.module.webdriver;

import java.util.concurrent.locks.Lock;

import org.openqa.grid.common.GridDocHelper;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.common.exception.GridConfigurationException;
import org.openqa.grid.internal.utils.GridHubConfiguration;
import org.openqa.grid.internal.utils.SelfRegisteringRemote;
import org.openqa.grid.web.Hub;
import org.testng.annotations.Test;

public class StaticWebdriverGridHelper {

  private static Hub h;
  private static SelfRegisteringRemote remote;
  
  public static void stopHub() throws Exception {
    h.stop();
  }
  
  public static void stopRemote(){
    remote.stopRemoteServer();
  }
  
  public static void lauchGrid() throws Exception{
    try {
      String port = System.getProperty("hubport");
      GridHubConfiguration c = GridHubConfiguration.build(new String[] {"-port",port});
      h = new Hub(c);
      h.start();
      System.out.println("Port is: "+h.getUrl());
    } catch (GridConfigurationException e) {
      e.printStackTrace();
      GridDocHelper.printHelp(e.getMessage());
    }
  }
  
  public static void lauchNode(String huburl) throws Exception {
    try {      
      RegistrationRequest c = RegistrationRequest.build(new String[] {"-role","webdriver","-hub",huburl,"-browser", "browserName=firefox","-port","5555"});      
      remote = new SelfRegisteringRemote(c);
      remote.startRemoteServer();
      remote.startRegistrationProcess();
      
      boolean ready = false;
      while (!ready){
        Thread.sleep(1000L);
        Lock luck = h.getRegistry().getLock();
        luck.lock();
        ready = !h.getRegistry().getAllProxies().isEmpty();
        luck.unlock();
      }
    } catch (GridConfigurationException e) {
      e.printStackTrace();
      GridDocHelper.printHelp(e.getMessage());
    }
  }
  
  @Test
  public void startStopTest() throws Exception {
    System.setProperty("hubport","4444");
    lauchGrid();
    lauchNode("http://localhost:4444/grid/register");
    stopRemote();
    stopHub();
  }
}
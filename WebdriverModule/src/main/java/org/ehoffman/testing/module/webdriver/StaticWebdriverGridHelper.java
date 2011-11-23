package org.ehoffman.testing.module.webdriver;

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
  
  public static boolean isMac(){
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf( "mac" ) >= 0); 
  }
  
  public static boolean isUnix(){
    String os = System.getProperty("os.name").toLowerCase();
    //linux or unix
    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
  }
  
  public static boolean isWinXp(){
    String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf( "win" ) >= 0 && os.indexOf( "xp" ) >= 0); 
  }
  
  public static void setDefaultChromeSystemPropertyBasedOnSystemType(){
    String prop = "webdriver.chrome.driver";
    String linuxLoc = "/usr/bin/google-chrome";
    String macLoc = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
    String xpLoc = "%HOMEPATH%\\Local Settings\\Application Data\\Google\\Chrome\\Application\\chrome.exe";
    String vistaLoc = "C:\\Users\\%USERNAME%\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";
    if (isMac()) System.setProperty(prop, macLoc); 
    else if (isUnix()) System.setProperty(prop, linuxLoc); 
    else if (isWinXp()) System.setProperty(prop, xpLoc); 
    else System.setProperty(prop, vistaLoc);
  }
  
  
  public static void lauchGrid() throws Exception{
    try {
      String port = System.getProperty("hubport");
      setDefaultChromeSystemPropertyBasedOnSystemType();
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
      RegistrationRequest c = RegistrationRequest.build(new String[] {"-role","webdriver","-hub",huburl,"-browser", "browserName=firefox,maxInstances=3",/*"-browser", "browserName=chrome,maxInstances=3",*/"-port","5555"});      
      remote = new SelfRegisteringRemote(c);
      remote.startRemoteServer();
      remote.startRegistrationProcess();
      boolean ready = false;
      while (!ready){
        Thread.sleep(1000L);
        ready = !h.getRegistry().getAllProxies().isEmpty();
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
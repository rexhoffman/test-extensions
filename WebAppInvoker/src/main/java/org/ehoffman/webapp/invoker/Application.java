package org.ehoffman.webapp.invoker;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  protected static final String webXmlSubLocation = "/WEB-INF/web.xml";
  protected File webXml = null;
  protected File warFile = null;
  protected List<File> webContent;
  protected boolean built = false;
  protected String contextRoot;
  protected Server server;

  public static WarBuilder buildWar(File warFile){
    return new WarBuilder(warFile);
  }

  public static ExplodedBuilder buildExploded(File ... contentDirectories){
    return new ExplodedBuilder(contentDirectories);
  }

  public static class WarBuilder{
    private final File warFile;
    private String contextRoot;

    public WarBuilder(File warFile){
      if (warFile.isFile() && (warFile.getAbsolutePath().endsWith(".war"))){
        this.warFile = warFile;
      } else {
        throw new RuntimeException("File not found or does not end in .war, file: "+warFile.getAbsolutePath());
      }
      String fileName = warFile.getName();
      contextRoot = fileName.substring(0, fileName.indexOf(".war"));
    }

    public WarBuilder setContextRoot(String contextRoot){
      this.contextRoot = contextRoot;
      return this;
    }

    public Application build(){
      Application application = new Application();
      application.contextRoot = this.contextRoot;
      application.warFile = this.warFile;
      return application;
    }
  }

  public static class ExplodedBuilder{
    private File webXml;
    private final List<File> contentDirs = new ArrayList<File>();
    private String contextRoot;

    /**
     * Checks to see if the directory root, has a web.xml in ./WEB-INF/web.xml and sets it in this builder if it does.
     * @param root
     */
    private void checkForWebXml(File root) {
      File webappRoot = new File(root.getAbsolutePath());
      if (webappRoot != null) {
        File webXml = new File(root.getAbsolutePath() + webXmlSubLocation);
        logger.info("Checking to see if a web.xml exists here: "+webXml.toString());
        if (webXml != null && webXml.isFile() && this.webXml == null) {
          logger.info("Setting web.xml to "+webXml.toString());
          this.webXml = webXml;
        }
      }
    }

    public ExplodedBuilder(File ... contentDirectories ){
      addContentDirectory(contentDirectories);
      contextRoot = "";
    }

    public ExplodedBuilder addContentDirectory(File ... contentDirectories){
      for (File contentDirectory : contentDirectories){
        logger.info("Attempting to add content directory "+contentDirectory.toString());
        if (contentDirectory.isDirectory()){
          contentDirs.add(contentDirectory);
          checkForWebXml(contentDirectory);
        }
      }
      return this;
    }


    public ExplodedBuilder addWebXml(File webXml){
      if (webXml.canRead() && webXml.isFile()){
        this.webXml = webXml;
      }
      return this;
    }

    public ExplodedBuilder setContextRoot(String contextRoot){
      this.contextRoot = contextRoot;
      return this;
    }

    public Application build(){
      Application application =  new Application();
      application.contextRoot = this.contextRoot;
      application.webXml = this.webXml;
      application.webContent = this.contentDirs;
      return application;
    }
  }

  private Application() {
  }

  /**
   * Status method, only callable if {@link #build()} has been called.
   * 
   * @return
   */
  public List<File> getWebContentDirs() {
    return webContent;
  }

  /**
   * Status method, only callable if {@link #build()} has been called.
   * 
   * @return
   */
  public File getWebXml() {
    return webXml;
  }

  /**
   * Status method, only callable if {@link #build()} has been called.
   * 
   * @return
   */
  public String getContextRoot() {
    if (contextRoot == null){
      contextRoot = "";
    }
    return contextRoot;
  }

  /**
   * Status method, only callable if {@link #build()} has been called.
   * 
   * @return
   */
  public File getWarFile(){
    return warFile;
  }

  /**
   * Status method, only callable if {@link #build()} has been called.
   * 
   * @return
   */
  public boolean isExploded(){
    return (warFile == null);
  }

  public void start(){
    try {
      if (this.server == null){
        this.server = runApplicationOnOwnServer();
      }
    } catch (Exception e){
      throw new RuntimeException("Error starting server",e);
    }
  }

  public void shutdown(){
    try {
      this.server.stop();
    } catch (Exception e){
      throw new RuntimeException("Error shutting down server",e);
    }
  }

  private String getUrlPartContextRoot(){
    String out = getContextRoot();
    if (out.length() != 0 && !out.startsWith("/")){
      out = "/"+out;
    }
    if (out.endsWith("/")){
      out = out.substring(0, out.length()-1);
    }
    return out;
  }

  public URL getDefaultRootUrl(){
    if (server == null){
      start();
    }
    Connector[] connectors = server.getConnectors();
    for (Connector connector : connectors){
      if (connector.getLocalPort() > 0){
        try {
          return new URL("http://"+"localhost"+":"+connector.getLocalPort()+this.getUrlPartContextRoot()+"/");
        } catch (MalformedURLException mal){
          throw new RuntimeException("jetty connector not valid", mal);
        }
      }
    }
    return null;
  }

  public URL getSecureRootUrl(){
    if (server == null){
      start();
    }
    Connector[] connectors = server.getConnectors();
    for (Connector connector : connectors){
      if (connector.getConfidentialPort() > 0){
        try {
          return new URL("https://"+"localhost"+":"+connector.getConfidentialPort()+"/"+this.getUrlPartContextRoot()+"/");
        } catch (MalformedURLException mal){
          throw new RuntimeException("jetty connector not valid", mal);
        }
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contextRoot == null) ? 0 : contextRoot.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Application other = (Application) obj;
    if (contextRoot == null) {
      if (other.contextRoot != null) {
        return false;
      }
    } else if (!contextRoot.equals(other.contextRoot)) {
      return false;
    }
    return true;
  }

  private Server runApplicationOnOwnServer() throws Exception {
    Server server = new Server(0);
    WebAppContext context = new WebAppContext();
    logger.info("App is "+(this.isExploded()?"":"not ")+"exploded");
    if (this.isExploded()){
      logger.info("WebXml %s", this.getWebXml().toString());
      context.setDescriptor(this.getWebXml().toString());
      if (this.getWebContentDirs() != null && this.getWebContentDirs().size() > 0){
        context.setResourceBase(this.getWebContentDirs().get(0).toString());
      }
      context.setParentLoaderPriority(true);
    } else {
      context.setWar(this.getWarFile().getAbsolutePath());
      context.setParentLoaderPriority(false);
    }
    server.setHandler(context);
    if (this.getContextRoot() != null && this.getContextRoot().length() > 0){
      if (this.getContextRoot().startsWith("/")){
        context.setContextPath(this.getContextRoot());
      } else {
        context.setContextPath("/"+this.getContextRoot());
      }
    }
    server.start();
    return server;
  }

  
  
}

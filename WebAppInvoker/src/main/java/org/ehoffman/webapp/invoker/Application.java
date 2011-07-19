package org.ehoffman.webapp.invoker;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

public class Application {
  private static final String[] defaultLocations = new String[] { "/src/main/webapp/", "/WebContent/" };
  private static final String webXmlSubLocation = "WEB-INF/web.xml";
  private File webXml = null;
  private File warFile = null;
  private final Properties props;
  private List<File> webContent;

  //derived
  private String contextRoot;
  private Server server;


  public Application(File rootLocation) {
    this(rootLocation, new Properties(), defaultLocations);
  }

  public Application(File rootLocation, Properties props) {
    this(rootLocation, props, defaultLocations);
  }

  public Application(File rootLocation, String... webResourceLocations) {
    this(rootLocation, new Properties(), webResourceLocations);
  }

  public Application(File rootLocation, Properties props, String... webResourceLocations) {
    if (rootLocation.isDirectory()){
      this.webContent = calcWebContent(rootLocation, defaultLocations);
    }
    if (rootLocation.isFile()){
      if (rootLocation.getAbsolutePath().endsWith(".war")){
        this.warFile = rootLocation;
      }
    }
    this.props = props;
    this.contextRoot = ((String) this.props.get("contextRoot"));
    if (contextRoot == null && warFile != null){
      String fileName = warFile.getName().toLowerCase();
      contextRoot = fileName.substring(0, fileName.indexOf(".war"));
    }
  }

  private void addDir(List<File> output, File root, String subpath) {
    File webappRoot = new File(root.getAbsolutePath() + subpath);
    if (webappRoot != null) {
      output.add(webappRoot);
      File webXml = new File(root.getAbsolutePath() + subpath + webXmlSubLocation);
      if (webXml != null && webXml.isFile() && this.webXml == null) {
        this.webXml = webXml;
      }
    }
  }

  public List<File> getWebContentDirs() {
    return webContent;
  }

  public File getWebXml() {
    return webXml;
  }

  public String getContextRoot() {
    return contextRoot;
  }

  public File getWarFile(){
    return warFile;
  }

  private List<File> calcWebContent(File rootLocation, String... webResourceLocations) {
    File root = rootLocation;
    List<File> output = new ArrayList<File>();
    for (String location : webResourceLocations) {
      addDir(output, root, location);
    }
    try {
      //InputStream stream = new FileInputStream(getWebXml());
      //this.displayName = XpathValueExtractor.getDisplayName(stream);
    } catch (Exception e){
      throw new RuntimeException("Please ensure you set a display name in your web.xml", e);
    }
    return output;
  }

  public boolean isExploded(){
    return (warFile == null);
  }

  void setServer(Server server){
    this.server = server;
  }

  public void start(){
    try {
      this.server = ApplicationUtil.runApplicationOnOwnServer(this);
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

  public URL getDefaultRootUrl(){
    if (server == null){
      start();
    }
    Connector[] connectors = server.getConnectors();
    for (Connector connector : connectors){
      if (connector.getLocalPort() > 0){
        try {
          return new URL("http://"+"localhost"+":"+connector.getLocalPort()+"/"+this.getContextRoot()+"/");
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
          return new URL("https://"+"localhost"+":"+connector.getConfidentialPort()+"/"+this.getContextRoot()+"/");
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

}

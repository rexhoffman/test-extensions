package org.ehoffman.webapp.invoker;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Application {
	private static final String[] defaultLocations = new String[] { "/src/main/webapp/", "/WebContent/" };
	private static final String webXmlSubLocation = "WEB-INF/web.xml";
	private File webXml;
	private Properties props;
	private List<File> webContent;
  private String displayName;
	
	public Application(File rootLocation) {
		this.webContent = calcWebContent(rootLocation, defaultLocations);
		this.props = new Properties();
	}

	public Application(File rootLocation, Properties props) {
		this.webContent = calcWebContent(rootLocation, defaultLocations);
		this.props = props;
	}

	public Application(File rootLocation, String... webResourceLocations) {
		this.webContent = calcWebContent(rootLocation, webResourceLocations);
		this.props = new Properties();
	}

	public Application(File rootLocation, Properties props, String... webResourceLocations) {
		this.webContent = calcWebContent(rootLocation, webResourceLocations);
		this.props = props;
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
		return (String) props.get("contextRoot");
	}
	
	public String getDisplayName(){
		return displayName;
	}

	private List<File> calcWebContent(File rootLocation, String... webResourceLocations) {
		File root = rootLocation;
		List<File> output = new ArrayList<File>();
		for (String location : webResourceLocations) {
			addDir(output, root, location);
		}
		try {
		  InputStream stream = new FileInputStream(getWebXml());
  		this.displayName = XpathValueExtractor.getDisplayName(stream);		
		} catch (Exception e){
			throw new RuntimeException("Please ensure you set a display name in your web.xml", e);
		}
		return output;
	}
}

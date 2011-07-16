package org.ehoffman.webapp.invoker;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XpathValueExtractor {
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  private static XPathFactory xFactory = XPathFactory.newInstance();
  private static XPath xpath = xFactory.newXPath();
  private static XPathExpression expr;
  static {
  	//factory.setNamespaceAware(true);
  	try {
		  expr = xpath.compile("/web-app/display-name");
  	} catch (XPathExpressionException e) {
  		Throwable t = e;
  		while (t != null){
  		  t.printStackTrace();
  		  t = t.getCause();
  		}
		  throw new RuntimeException(e);
		}
  }
  
	public static String getDisplayName(InputStream istream) {
		try {
			//factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(istream);
			
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			String out = null;
			for (int i = 0; i < nodes.getLength(); i++) {
				System.out.println(nodes.item(i).getNodeValue());
				if (out == null)
					out = nodes.item(i).getTextContent();
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

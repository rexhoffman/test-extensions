package org.ehoffman.testing.fest.webdriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class FakeWebElement implements WebElement {

	private Map<String, String> properties;
	private Map<String, String> cssProperties;
	private String tag;
	private String text;
	
	
	public FakeWebElement(String tag, String text, Map<String, String> properties, Map<String, String>  cssProperties){
	  this.properties = new HashMap<String, String>(properties);
	  this.cssProperties = new HashMap<String, String>(cssProperties);
	  this.tag = tag;
	  this.text = text;
	}
	
	@Override
	public void click() {
		//not interested in for validation
	}

	@Override
	public void submit() {
		//not interested in for validation
	}

	@Override
	public void sendKeys(CharSequence... keysToSend) {
		//not interested in for validation
	}

	@Override
	public void clear() {
		//not interested in for validation
	}

	@Override
	public String getTagName() {
		return tag;
	}

	@Override
	public String getAttribute(String name) {
		return properties.get(name);
	}

	@Override
	public boolean isSelected() {
		return new Boolean(getAttribute("selected")).booleanValue();
	}

	@Override
	public boolean isEnabled() {
		return (getAttribute("disabled") == null) || ("true".compareToIgnoreCase(getAttribute("disabled")) != 0);
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public List<WebElement> findElements(By by) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebElement findElement(By by) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDisplayed() {
		return (new Boolean(getCssValue("displayed")).booleanValue());
	}

	@Override
	public Point getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getSize() {
	    return new Dimension(new Integer(getCssValue("width")), new Integer(getCssValue("height")));
	}

	@Override
	public String getCssValue(String cssPropertyName) {
		return cssProperties.get(cssPropertyName);
	}

}

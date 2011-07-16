package org.ehoffman.testing.fest.webdriver;

import org.openqa.selenium.WebElement;

public class WebElementAssert {
	
	public static final WebElementExtension assertThat(WebElement element){
  	return new WebElementExtension(element);
  }
	
	public static final PageObjectExtension assertThatPageObject(Object pageObject){
    return new PageObjectExtension(pageObject);
  }
}

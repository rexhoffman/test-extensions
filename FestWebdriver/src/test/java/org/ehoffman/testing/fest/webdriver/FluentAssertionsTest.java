package org.ehoffman.testing.fest.webdriver;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;
import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThatPageObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.ehoffman.testing.fest.webdriver.WebElementExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.Test;


/**
 * Tests the WebElementExtension class
 * 
 * @author rexhoffman
 */
public class FluentAssertionsTest     {
    
    static AtomicReference<File> aFile = new AtomicReference<File>();
  
	@SuppressWarnings("serial")
    @Test
	public void testWithFakeWebElements() throws Throwable{
        WebElement input1 = new FakeWebElement("input", "", new HashMap<String,String>(){{put("name","input1"); put("type","text"); put("value","default value"); put("disabled", "false");}}, new HashMap<String,String>(){{put("color","#00c000"); put("displayed","true");put("height","20");put("width","60");put("visibility","visible");}});
        WebElement input2hidden = new FakeWebElement("input", "", new HashMap<String,String>(){{put("name","input2"); put("type","text"); put("value","default value1");  put("disabled", "FALSE");}}, new HashMap<String,String>(){{put("displayed","false");put("height","20");put("width","60");put("visibility","hidden");}});
        WebElement input3disabled = new FakeWebElement("input", "", new HashMap<String,String>(){{put("name","input3"); put("type","text"); put("value","default value2"); put("disabled", "true");}}, new HashMap<String,String>(){{put("displayed","true");put("height","20");put("width","60");put("visibility","visible");}});
        WebElement linky = new FakeWebElement("a", "Click Me!!!", new HashMap<String,String>(){{put("id","linky"); put("text","Click Me!!!"); put("disabled", "false");}}, new HashMap<String,String>(){{put("displayed","true");put("visibility","visible");}});
        WebElement selected = new FakeWebElement("option","Three",new HashMap<String,String>(){{put("value","three"); put("selected","true");}}, new HashMap<String,String>(){{put("displayed","true");put("visibility","visible");}});
        WebElement not_selected = new FakeWebElement("option","Two",new HashMap<String,String>(){{put("value","two"); put("selected","false");}}, new HashMap<String,String>(){{put("displayed","true");put("visibility","visible");}});
        IndexPage page = new IndexPage(new HtmlUnitDriver(){public String getTitle(){return "Awesome Page";}});
        page.setInput1(input1);
        page.setInput2hidden(input2hidden);
        page.setInput3disabled(input3disabled);
        page.setLinky(linky);
        page.setSelected(selected);
        page.setNot_selected(not_selected);
        verifyIndexPage(page);
	}

	private static File copyFileToTemp(String name) throws IOException {
		File file = File.createTempFile("some_index_file_", "_file.html");
		if (aFile.compareAndSet(null,file)){
	      file.createNewFile();
	      InputStream stream = FluentAssertionsTest.class.getClassLoader().getResourceAsStream(name);
		  IOUtils.copy(stream, new FileOutputStream(file));
		}
		return aFile.get();
	}
	
	@Test
	public void FirefoxDriverTest() throws Throwable {
	  File indexFile = copyFileToTemp("index.html");
      WebDriver driver = new FirefoxDriver();
      try {
        driver.navigate().to(indexFile.toURI().toURL());
        IndexPage page = PageFactory.initElements(driver, IndexPage.class);
        verifyIndexPage(page);
      } finally {
        driver.close();
      }      
	}
	
	@Test
    public void testWithLocalPageHtmlUnit() throws Throwable {
      File indexFile = copyFileToTemp("index.html");
      HtmlUnitDriver driver = new HtmlUnitDriver();
      driver.setJavascriptEnabled(true);
      try {
        driver.navigate().to(indexFile.toURI().toURL());
        IndexPage page = PageFactory.initElements(driver, IndexPage.class);
        verifyIndexPage(page);
      } finally {
        driver.close();
      }      
    }
	
	private void verifyIndexPage(IndexPage page) {
      assertThatPageObject(page).allWebElementsNotNull();
      assertsAroundWebElementInput1(page.getInput1());
      assertsAroundWebElementLinky(page.getLinky());
      assertsAroundWebInput2Hidden(page.getInput2hidden());
      assertsAroundWebInput3Disabled(page.getInput3disabled());
      assertsAroundSelectableOptions(page.getSelected(), page.getNot_selected());
	}
	
	private int randomNonZeroInt(){
	  Random r = new Random();
	  return r.nextInt(1000)+1*(r.nextBoolean()?1:-1);
	}
	
	
	private void assertsAroundWebElementInput1(WebElement element)  {
	    //positive
		assertThat(element).isNotNull().hasTagName("input").isDisplayed().isEnabled().hasName("input1").isNotHidden().hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60).hasSize(new Dimension(60,20)).hasValue("default value").doesNotHaveCssPropertyWithValue("background-image", "image.jpg");
		
		//negative tests
		//Test WebElementDescription is used and that null checks are inherited.
		try {
		  assertThat(element).isNull();
		} catch (Throwable t){
		  assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
		  assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
		  assertThat(t.getMessage()).contains("should be null");
		}
		
		try {
          assertThat(element).hasTagName("a");
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have expected tag name");
        }
		
        try {
          assertThat(element).isNotDisplayed();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should not be displayed");
        }

        try {
          assertThat(element).isNotEnabled();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should not be enabled");
        }
        
        try {
          assertThat(element).hasName("input");
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"name\" with a value of \"input\"");
        }
        
        try {
          assertThat(element).isHidden();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have css property \"visibility\" with a value of either hidden or collapsed");
        }
        
        try {
          assertThat(element).hasAttributeWithValue("type", "purplepeopleeater");
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"type\" with a value of \"purplepeopleeater\"");
        }
        
        try {
          assertThat(element).hasHeight(21);
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have a height of 21 but was 20");
        }
        
        try {
          assertThat(element).hasWidth(61);
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have a width of 61 but was 60");
        }
                
        try {
          assertThat(element).hasValue("some value");
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"value\" with a value of \"some value\"");
        }
        
        try {
          assertThat(element).doesNotHaveCssPropertyWithValue("color", "#00c000");//green
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have not have a css property with the name of color and a value of #00c000");
        }
        
        for (int i = 1; i < 20; i++){
          Dimension d = new Dimension(60+randomNonZeroInt(),20+randomNonZeroInt());
          try {
            assertThat(element).hasSize(d);
          } catch (Throwable t){
            assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
            assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
            assertThat(t.getMessage()).contains("have dimensions of " + d + "but has dimensions of " + element.getSize());
          }
  	    }
 
        try {
          assertThat((WebElement)null).hasWidth(21);
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(null).value());
          assertThat(t.getMessage()).contains("expecting actual value not to be null");
        }
 
	}
	
	private void assertsAroundWebElementLinky(WebElement element) {
      assertThat(element).isNotNull().hasTagName("a").isDisplayed().doesNotHaveAttribute("name").isNotHidden().textContains("Click Me!!!");
      try {
        assertThat(element).hasAttribute("name");
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
        assertThat(t.getMessage()).contains("expecting actual value not to be null");
      }
	}
	
	private void assertsAroundWebInput2Hidden(WebElement element) {
      assertThat(element).isNotNull().hasTagName("input").isHidden().isNotDisplayed().hasName("input2").hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60).hasAttributeWithValue("disabled", Boolean.FALSE);
    }    
	
	private void assertsAroundWebInput3Disabled(WebElement element) {
      assertThat(element).isNotNull().hasTagName("input").isNotHidden().isDisplayed().hasName("input3").hasAttributeWithValue("type", "text").hasAttributeWithValue("disabled", Boolean.TRUE).isNotEnabled();
      try {
        assertThat(element).isEnabled();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
        assertThat(t.getMessage()).contains("should be enabled");
      }
    }
	
	private void assertsAroundSelectableOptions(WebElement selected, WebElement notselected) {
      assertThat(selected).isSelected();
      try {
        assertThat(selected).isNotSelected();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(selected).value());
        assertThat(t.getMessage()).contains("should not be selected");
      }
      assertThat(notselected).isNotSelected();
      try {
        assertThat(notselected).isSelected();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(notselected).value());
        assertThat(t.getMessage()).contains("should be selected");
      }
    }
	
}

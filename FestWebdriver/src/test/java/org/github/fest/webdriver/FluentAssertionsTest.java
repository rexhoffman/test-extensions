package org.github.fest.webdriver;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.ehoffman.testing.fest.webdriver.WebElementExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.testng.annotations.Test;


public class FluentAssertionsTest     {
    
    static AtomicReference<File> aFile = new AtomicReference<File>();
  
	@Test
	public void testWithFakeWebElements() throws Throwable{
		FakeWebElement element = new FakeWebElement("input", "", new HashMap<String,String>(){{put("name","input1"); put("type","text");}}, new HashMap<String,String>(){{put("displayed","true");put("height","20");put("width","60");put("visibility","visible");}});
		assertsAroundWebElementInput1(element);
		element = new FakeWebElement("a", "Click Me!!!", new HashMap<String,String>(){{put("id","linky"); put("text","Click Me!!!");}}, new HashMap<String,String>(){{put("displayed","true");put("visibility","visible");}});
		assertsAroundWebElementLinky(element);
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
        assertsAroundWebElementInput1(driver.findElement(By.id("input1")));
        assertsAroundWebElementLinky(driver.findElement(By.id("linky")));
        assertsAroundWebInput2Hidden(driver.findElement(By.id("input2hidden")));
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
        assertsAroundWebElementInput1(driver.findElement(By.id("input1")));
        assertsAroundWebElementLinky(driver.findElement(By.id("linky")));
        assertsAroundWebInput2Hidden(driver.findElement(By.id("input2hidden")));
      } finally {
        driver.close();
      }      
    }
	
	private void assertsAroundWebElementInput1(WebElement element) throws Exception {
	    //positive
		assertThat(element).isNotNull().hasTagName("input").isDisplayed().hasName("input1").isNotHidden().hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60);
		
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
	}
	
	private void assertsAroundWebElementLinky(WebElement element) throws Exception {
      assertThat(element).isNotNull().hasTagName("a").isDisplayed().doesNotHaveAttribute("name").isNotHidden().textContains("Click Me!!!");
      try {
        assertThat(element).hasAttribute("name");
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
        assertThat(t.getMessage()).contains("expecting actual value not to be null");
      }
	}
	
	private void assertsAroundWebInput2Hidden(WebElement element) throws Exception {
      assertThat(element).isNotNull().hasTagName("input").isNotDisplayed().hasName("input2").isHidden().hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60);
    }    
}

package org.ehoffman.testing.fest.webdriver;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

    /**
     * Copies a resource from the classpath to the systems temp dir so that selenium can access it.  Does this in a thread safe way so that multiple tests can call this method at the same time.
     * If the file already exists this is not done.
     * 
     * @return a the File after 
     * @throws IOException
     */
    private static File getTestFile() throws IOException {
      File file = File.createTempFile("some_index_file_", "_file.html");
      if (aFile.compareAndSet(null,file)){
        file.createNewFile();
        InputStream stream = FluentAssertionsTest.class.getClassLoader().getResourceAsStream("index.html");
        IOUtils.copy(stream, new FileOutputStream(file));
      }
      return aFile.get();
    }  

    /**
     * @return an IndexPage page object fully populated with {@link FakeWebElemenets}
     */
    @SuppressWarnings("serial")
    private IndexPage getNewIndexPage(){
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
      return page;
    }
    
    /** 
     * Test bean to compare against page objects
     */
    public static class O1 {
      public String getSelected(){return "three";}
      public String getInput2hidden(){return "default value1";}
      public String getInput3disabled(){return "default value2";}
      public String getNot_selected(){return "two";}
      public String getLinky(){return null;}
      public String getInput1(){return "default value";}
    }
    
    /** 
     * Test bean to compare against page objects
     */
    public static class O2 {
      public String getSelected(){return "three";}
      public String getInput2hidden(){return "default value1";}
      public String getInput3disabled(){return "default value2";}
      public String getNot_selected(){return "two";}
      public String getLinky(){return null;}
      //public String getInput1(){return "default value";}
    };
    
    /** 
     * Test bean to compare against page objects
     */
    public static class O3 {
      public String getSelected(){return "three";}
      public String getInput2hidden(){return "default value1";}
      public String getInput3disabled(){return "wrong value2";}
      public String getNot_selected(){return "two";}
      public String getLinky(){return null;}
      public String getInput1(){return "default value";}
    }
    
    
    @Test(description="Checks all the assertion methods on the PageObjectExtension class")
    public void pageObjectAssertionsTest(){
      IndexPage page = getNewIndexPage();
      WebElement input2hidden = page.getInput2hidden();
      try {
        assertThatPageObject(page).allWebElementsDisplayed();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(input2hidden).value());
        assertThat(t.getMessage()).contains("should be displayed");
      }
      page.setInput2hidden(page.getLinky());
      assertThatPageObject(page).allWebElementsDisplayed();
      
      page = getNewIndexPage();
      WebElement input3disabled = page.getInput3disabled();
      try {
        assertThatPageObject(page).allWebElementsEnabled();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(input3disabled).value());
        assertThat(t.getMessage()).contains("should be enabled");
      }
      page.setInput3disabled(page.getLinky());
      assertThatPageObject(page).allWebElementsEnabled();

      page.setInput3disabled(null);
      try {
        assertThatPageObject(page).allWebElementsEnabled();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(null).value());
        assertThat(t.getMessage()).contains("expecting actual value not to be null");
      }
      
      
      page = getNewIndexPage();
      Map<String, String> map = new HashMap<String, String>();
      map.put("selected","three");
      map.put("input2hidden","default value1");
      map.put("input3disabled","default value2");
      map.put("not_selected","two");
      map.put("linky",null);
      map.put("input1","default value");
      assertThatPageObject(page).isEqualViaReflectiveMapComparison(map);
      
      Object o = new O1();
      assertThatPageObject(page).isEqualViaReflectiveBeanComparison(o);
      

      Object incomplete = new O2();
      try {
        assertThatPageObject(page).isEqualViaReflectiveBeanComparison(incomplete);
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains("expected:<['selected', 'input2hidden', 'input3disabled', 'not_selected', 'linky']> but was:<['selected', 'input2hidden', 'input3disabled', 'not_selected', 'linky', 'input1']>");
      }
      
      Object wrongValue = new O3();
      try {
        assertThatPageObject(page).isEqualViaReflectiveBeanComparison(wrongValue);
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(page.getInput3disabled()).value());
        assertThat(t.getMessage()).contains("should have attribute with the name of \"value\" with a value of \"wrong value2\"");
      }
      
    }
    
    @Test(description="Verifies all the WebElement assertions work with known faked WebElements")
	public void testWithFakeWebElements() throws Throwable{
      verifyIndexPage(getNewIndexPage());
      @SuppressWarnings("unused")
      WebElementAssert meaningless = new WebElementAssert();//need for line coverage :)
	}

    @Test(description="Verifies all the WebElement assertions work with Firefox WebElements")
	public void FirefoxDriverTest() throws Throwable {
	  File indexFile = getTestFile();
      WebDriver driver = new FirefoxDriver();
      try {
        driver.navigate().to(indexFile.toURI().toURL());
        IndexPage page = PageFactory.initElements(driver, IndexPage.class);
        verifyIndexPage(page);
      } finally {
        driver.close();
      }      
	}
	
    @Test(description="Verifies all the WebElement assertions work with HtmlUnit WebElements")
    public void testWithLocalPageHtmlUnit() throws Throwable {
      File indexFile = getTestFile();
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
	
    /**
     * Runs various methods to determine if assertion methods fail and succeed in expected cases.
     * 
     * @param page the page object containing all the webelements to inspect.
     */
	private void verifyIndexPage(IndexPage page) {
      assertThatPageObject(page).allWebElementsNotNull();
      assertsAroundWebElementInput1(page.getInput1());
      assertsAroundWebElementLinky(page.getLinky());
      assertsAroundWebInput2Hidden(page.getInput2hidden());
      assertsAroundWebInput3Disabled(page.getInput3disabled());
      assertsAroundSelectableOptions(page.getSelected(), page.getNot_selected());
	}
	
	/**
	 * Used to ensure we're testing for over and under conditions when running numerical asserts
	 * @return
	 */
	private int randomNonZeroInt(){
	  Random r = new Random();
	  return (r.nextInt(999)+1)*(r.nextBoolean()?1:-1);
	}
	
	/**
	 * Tests a webelement this is:
	 *     <li>displayed</li>
	 *     <li>visible</li>
	 *     <li>input of type text</li>
	 *     <li>has width of 60px and height of 20px</li>
	 *     <li>has text color of green</li>
	 *     <br/>
	 *     also does a null check on the asserts at the end of the method function
	 * @param element
	 */
	private void assertsAroundWebElementInput1(WebElement element)  {
	    //positive
		assertThat(element).isNotNull().hasTagName("input").isDisplayed().isEnabled().hasName("input1").isNotHidden().hasAttribute("type").hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60).hasSize(new Dimension(60,20)).hasValue("default value").doesNotHaveCssPropertyWithValue("background-image", "image.jpg");
		
		//negative tests
		//Test WebElementDescription is used and that null checks are inherited.
		try {
		  assertThat(element).isNull();
	      assertThat("Should not be reachable").isNull();
		} catch (Throwable t){
		  assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
		  assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
		  assertThat(t.getMessage()).contains("should be null");
		}
		
		try {
          assertThat(element).hasTagName("a");
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have expected tag name");
        }
		
        try {
          assertThat(element).isNotDisplayed();
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should not be displayed");
        }

        try {
          assertThat(element).isNotEnabled();
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should not be enabled");
        }
        
        try {
          assertThat(element).hasName("input");
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"name\" with a value of \"input\"");
        }
        
        try {
          assertThat(element).isHidden();
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have css property \"visibility\" with a value of either hidden or collapsed");
        }
        
        try {
          assertThat(element).hasAttributeWithValue("type", "purplepeopleeater");
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"type\" with a value of \"purplepeopleeater\"");
        }
        
        try {
          assertThat(element).hasHeight(21);
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have a height of 21 but was 20");
        }

        try {
          assertThat(element).hasWidth(61);
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have a width of 61 but was 60");
        }
                
        try {
          assertThat(element).hasValue("some value");
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have attribute with the name of \"value\" with a value of \"some value\"");
        }
        
        try {
          assertThat(element).doesNotHaveCssPropertyWithValue("color", "#00c000");//green
          System.out.println(element.getCssValue("color"));
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
          assertThat(t.getMessage()).contains("should have not have a css property with the name of color and a value of #00c000");
        }
        
        for (int i = 1; i < 20; i++){
          Dimension d = new Dimension(60+randomNonZeroInt(),20+randomNonZeroInt());
          try {
            assertThat(element).hasSize(d);
            assertThat("Should not be reachable").isNull();
          } catch (Throwable t){
            assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
            assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
            assertThat(t.getMessage()).contains("have dimensions of " + d + "but has dimensions of " + element.getSize());
          }
  	    }
 
        try {
          assertThat((WebElement)null).hasWidth(21);
          assertThat("Should not be reachable").isNull();
        } catch (Throwable t){
          assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
          assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(null).value());
          assertThat(t.getMessage()).contains("expecting actual value not to be null");
        }
 
	}
	
	/**
	 * Tests a webelement this is:
     *     <li>displayed</li>
     *     <li>visible</li>
     *     <li>an anchor tag</li>
     *     <li>has inner text that contains "Click Me!!!"</li>
     *     <li>has text color of green</li>
     *     <br/> 
     * @param element
	 */
	private void assertsAroundWebElementLinky(WebElement element) {
      assertThat(element).isNotNull().hasTagName("a").isDisplayed().doesNotHaveAttribute("name").isNotHidden().textContains("Click Me!!!");
      try {
        assertThat(element).hasAttribute("name");
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
        assertThat(t.getMessage()).contains("should have attribute with the name of name");
      }
	}
	
    /**
     * Tests a webelement this is:
     *     <li>hidden</li>
     *     <li>not displayed</li>
     *     <li>input of type text</li>
     *     <li>has text color of green</li>
     *     <br/> 
     * @param element
     */
	private void assertsAroundWebInput2Hidden(WebElement element) {
      assertThat(element).isNotNull().hasTagName("input").isHidden().isNotDisplayed().hasName("input2").hasAttributeWithValue("type", "text").hasHeight(20).hasWidth(60).hasAttributeWithValue("disabled", Boolean.FALSE);
    }    
	
    /**
     * Tests a webelement this is:
     *     <li>disabled</li>
     *     <li>input of type text</li>
     *     <li>has text color of green</li>
     *     <br/> 
     * @param element
     */
	private void assertsAroundWebInput3Disabled(WebElement element) {
      assertThat(element).isNotNull().hasTagName("input").isNotHidden().isDisplayed().hasName("input3").hasAttributeWithValue("type", "text").hasAttributeWithValue("disabled", Boolean.TRUE).isNotEnabled();
      try {
        assertThat(element).isEnabled();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(element).value());
        assertThat(t.getMessage()).contains("should be enabled");
      }
    }
	
	/**
	 * Tests two select options, one is selected, the other is not.
	 * 
	 * @param selected
	 * @param notselected
	 */
	private void assertsAroundSelectableOptions(WebElement selected, WebElement notselected) {
      assertThat(selected).isSelected();
      try {
        assertThat(selected).isNotSelected();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(selected).value());
        assertThat(t.getMessage()).contains("should not be selected");
      }
      assertThat(notselected).isNotSelected();
      try {
        assertThat(notselected).isSelected();
        assertThat("Should not be reachable").isNull();
      } catch (Throwable t){
        assertThat(t).hasNoCause().isExactlyInstanceOf(AssertionError.class);
        assertThat(t.getMessage()).contains(new WebElementExtension.WebElementDescription(notselected).value());
        assertThat(t.getMessage()).contains("should be selected");
      }
    }
	
}

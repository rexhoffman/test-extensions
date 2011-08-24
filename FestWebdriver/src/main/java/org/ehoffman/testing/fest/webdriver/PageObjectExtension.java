package org.ehoffman.testing.fest.webdriver;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;
import static org.fest.assertions.Assertions.assertThat;

import org.apache.commons.beanutils.PropertyUtils;
import org.fest.assertions.GenericAssert;
import org.openqa.selenium.WebElement;

public class PageObjectExtension extends GenericAssert<PageObjectExtension, Object> {

  private Object object;
  private Map<String, WebElement> extractedElementMap;
  
  public PageObjectExtension(Object pageObject){
    super(PageObjectExtension.class, pageObject);
    this.object = pageObject;
    this.extractedElementMap = extractWebElementMap();
  }
  
  private Map<String, WebElement> extractWebElementMap(){
    Map<String, WebElement> extractedElementMap = new HashMap<String, WebElement>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = PropertyUtils.describe(object);
      for (Entry<String, Object> entry : map.entrySet()){
        Object returned = entry.getValue();
        if (WebElement.class.isAssignableFrom(PropertyUtils.getPropertyType(object, entry.getKey()))){
          System.out.println("Putting " +entry);
          extractedElementMap.put(entry.getKey(),(WebElement)returned);
        }
      } 
    } catch (IllegalAccessException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    } catch (InvocationTargetException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    } catch (NoSuchMethodException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    }
    return extractedElementMap;
  }
  
  public PageObjectExtension allWebElementsNotNull(){
    for (Entry<String, WebElement> element : extractedElementMap.entrySet()){
      assertThat(element.getValue()).isNotNull();
    }
    return this;
  }
  
  public PageObjectExtension allWebElementsEnabled(){
    for (Entry<String, WebElement> element : extractedElementMap.entrySet()){
      assertThat(element.getValue()).isNotNull().isEnabled();
    }
    return this;
  }

  public PageObjectExtension allWebElementsDisplayed(){
    for (Entry<String, WebElement> element : extractedElementMap.entrySet()){
      assertThat(element.getValue()).isNotNull().isDisplayed();
    }
    return this;
  }

  
  public PageObjectExtension isEqualViaReflectiveBeanComparison(Object o){
    isEqualViaReflectiveMapComparison(extractBeanValues(o));
    return this;
  }

  private Map<String, String> extractBeanValues(Object object){
    Map<String, String> extractedBeanMap = new HashMap<String, String>();
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = PropertyUtils.describe(object);
      for (Entry<String, Object> entry : map.entrySet()){
        Object returned = entry.getValue();
        if (returned == null || !Class.class.isAssignableFrom(returned.getClass())){
          extractedBeanMap.put(entry.getKey(),(returned!=null?returned.toString():(String)null));
        }
      }
    } catch (IllegalAccessException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    } catch (InvocationTargetException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    } catch (NoSuchMethodException e1) {
      throw new RuntimeException("This should not be reachable",e1);
    }
    return extractedBeanMap;
  }
  
  public PageObjectExtension isEqualViaReflectiveMapComparison(Map<String, String> map){
    assertThat(extractedElementMap.keySet()).isEqualTo(map.keySet());
    for (Entry<String, WebElement> entry : extractedElementMap.entrySet()){
      String value = map.get(entry.getKey());
      WebElement element = entry.getValue();
      if (value == null){
        assertThat(element).doesNotHaveAttribute("value");
      } else {
        assertThat(element).hasValue(value);
      }
    }
    return this;
  }

  
}

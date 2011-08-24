package org.ehoffman.testing.fest.webdriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;
import static org.fest.assertions.Assertions.assertThat;

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
    for (Method method : object.getClass().getMethods()){
      if (WebElement.class.isAssignableFrom(method.getReturnType()) &&
          method.getParameterTypes().length == 0 &&
          method.getName().startsWith("get")){
          WebElement element = null;
          try{
            element = (WebElement) method.invoke(object, new Object[]{});
          } catch (IllegalAccessException access){
          } catch (InvocationTargetException target){
          } catch (IllegalArgumentException argument){
          }
          extractedElementMap.put(method.getName().substring("get".length()).toLowerCase(), element);
      }
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
    for (Method method : object.getClass().getMethods()){
      if (method.getReturnType() != null &&
          method.getParameterTypes().length == 0 &&
          method.getName().startsWith("get") &&
          method.getName().compareTo("getClass") != 0){
        Object returned = null;
        try{
          returned = method.invoke(object, new Object[]{});
        } catch (IllegalAccessException access){
        } catch (InvocationTargetException target){
        } catch (IllegalArgumentException argument){
        }
        extractedBeanMap.put(method.getName().substring("get".length()).toLowerCase(), (returned!=null?returned.toString():(String)null));
      }
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

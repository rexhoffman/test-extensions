package org.ehoffman.testing.fest.webdriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.ehoffman.testing.fest.webdriver.WebElementAssert.assertThat;
import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.AssertExtension;
import org.openqa.selenium.WebElement;

public class PageObjectExtension implements AssertExtension{

  private Object object;
  private Map<String, WebElement> extractedElementMap;
  
  public PageObjectExtension(Object pageObject){
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
    assertThat(extractedElementMap.values()).excludes((Object)null);
    return this;
  }
  
  public PageObjectExtension allWebElementsDisplayedAndVisibile(){
    assertThat(extractedElementMap.values()).excludes((Object)null);
    return this;
  }
  
  public PageObjectExtension isEqualViaReflectiveBeanComparison(Object o){
    isEqualViaReflectiveBeanComparison(extractBeanValues(o));
    return this;
  }

  private Map<String, Object> extractBeanValues(Object object){
    Map<String, Object> extractedBeanMap = new HashMap<String, Object>();
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
        extractedBeanMap.put(method.getName().substring("get".length()).toLowerCase(), returned);
      }
    }
    return extractedBeanMap;
  }
  
  public PageObjectExtension isEqualViaReflectiveBeanComparison(Map<String, Object> map){
    assertThat(extractedElementMap.keySet()).isEqualTo(map.keySet());
    for (Entry<String, WebElement> entry : extractedElementMap.entrySet()){
      assertThat(entry.getValue()).hasValue(""+map.get(entry.getKey()));
    }
    return this;
  }

  
}

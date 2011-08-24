package org.ehoffman.testing.fest.webdriver;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;

import org.fest.assertions.Description;
import org.fest.assertions.GenericAssert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public class WebElementExtension extends GenericAssert<WebElementExtension, WebElement> {
  private final WebElement      element;

  private final static String[] booleanParameters = "async, autofocus, autoplay, checked, compact, complete, controls, declare, defaultchecked, defaultselected, defer, disabled, draggable, ended, formnovalidate, hidden, indeterminate, iscontenteditable, ismap, itemscope, loop, multiple, muted, nohref, noresize, noshade, novalidate, nowrap, open, paused, pubdate, readonly, required, reversed, scoped, seamless, seeking, selected, spellcheck, truespeed, willvalidate"
                                                      .split(", ");

  public static class WebElementDescription implements Description {
    private WebElement element;
    
    public WebElementDescription(WebElement element){
      this.element = element;
    }
    
    @Override
    public String value() {
      if (element == null)
        return "[ WebElement null element";
      StringBuilder builder = new StringBuilder("WebElement ");
      builder.append("tag: ");
      builder.append(element.getTagName());
      if (element.getAttribute("id") != null) {
        builder.append(", id : ");
        builder.append(element.getAttribute("id"));
      }
      if (element.getAttribute("name") != null) {
        builder.append(", name : ");
        builder.append(element.getAttribute("name"));
      }
      builder.append(" ]");
      return builder.toString();
    } 
  }

  public WebElementExtension(WebElement element) {
    super(WebElementExtension.class, element);
    this.element = element;
    this.description(new WebElementDescription(element));
  }

  private String getShouldText() {
    return new WebElementDescription(element).value() + " should ";
  }

  public WebElementExtension isSelected() {
    isNotNull();
    assertThat(element.isSelected()).as(getShouldText() + "be selected").isTrue();
    return this;
  }

  public WebElementExtension isNotSelected() {
    isNotNull();
    assertThat(element.isSelected()).as(getShouldText() + "not be selected").isFalse();
    return this;
  }

  public WebElementExtension isEnabled() {
    isNotNull();
    assertThat(element.isEnabled()).as(getShouldText() + "be enabled").isTrue();
    return this;
  }

  public WebElementExtension isNotEnabled() {
    isNotNull();
    assertThat(element.isEnabled()).as(getShouldText() + "not be enabled").isFalse();
    return this;
  }

  public WebElementExtension hasTagName(String tagName) {
    isNotNull();
    assertThat(element.getTagName()).as(getShouldText() + "have expected tag name").isEqualToIgnoringCase(tagName);
    return this;
  }

  public WebElementExtension isDisplayed() {
    isNotNull();
    assertThat(element.isDisplayed()).as(getShouldText() + "be displayed").isTrue();
    return this;
  }

  public WebElementExtension isNotDisplayed() {
    isNotNull();
    assertThat(element.isDisplayed()).as(getShouldText() + "not be displayed").isFalse();
    return this;
  }

  public WebElementExtension hasHeight(Integer height) {
    isNotNull();
    assertThat(element.getSize()).as(getShouldText()+" have a non-null size bean property").isNotNull();
    assertThat(element.getSize().getHeight()).as(getShouldText() + "have a height of " + height + " but was " + element.getSize().getHeight()).isEqualTo(height);
    return this;
  }
  
  public WebElementExtension hasWidth(Integer width) {
    isNotNull();
    assertThat(element.getSize()).as(getShouldText()+" have a non-null size bean property").isNotNull();
    assertThat(element.getSize().getWidth()).as(getShouldText() + "have a width of " + width + " but was " + element.getSize().getWidth()).isEqualTo(width);
    return this;
  }

  public WebElementExtension hasSize(Dimension dimension) {
    isNotNull();
    assertThat(element.getSize()).as(getShouldText()+" have a non-null size bean property").isNotNull();
    assertThat(element.getSize().toString()).as(getShouldText() + "have dimensions of " + dimension + "but has dimensions of " + element.getSize()).isEqualTo(dimension.toString());
    return this;
  }
  
  public WebElementExtension doesNotHaveCssPropertyWithValue(String cssProperty, String value){
    isNotNull();
    if (element.getCssValue(cssProperty) == null) return this;
    assertThat(element.getCssValue(cssProperty).toLowerCase()).as(getShouldText() + "have not have a css property with the name of " + cssProperty + " and a value of "+value ).isNotEqualTo(value.toLowerCase());
    return this;
  }

  public WebElementExtension hasCssPropertyWithValue(String cssProperty, String value) {
    isNotNull();
    assertThat(value).as("Do not use hasCssPropertyWithValue for values or null or empty String, use doesNotCssProperty").isNotNull().isNotEmpty();
    assertThat(element.getCssValue(cssProperty)).as(getShouldText() + "have css property with the name of " + cssProperty + " with a current computed value of " + value).isNotNull().isNotEmpty().isEqualTo(value);
    return this;
  }
  
  public WebElementExtension doesNotHaveAttribute(String attribute) {
    isNotNull();
    assertThat(booleanParameters).as("hasAttribute() is a meaningless inspection for boolean parameters").excludes(attribute);
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText() + "have not have an attribute with the name of " + attribute).isNotSameAs("");
    return this;
  }

  public WebElementExtension hasAttribute(String attribute) {
    isNotNull();
    assertThat(booleanParameters).as("hasAttribute() is a meaningless inspection for boolean parameters").excludes(attribute);
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText() + "have attribute with the name of " + attribute).isNotNull().isNotSameAs("");
    return this;
  }

  /**
   * Uses the {@link WebElement#getAttribute(String)} to look the the attributes value.  The attribute input is taken as is, and no case-changes are made.
   * 
   * Ignores case differences in values if they exist.
   * 
   * @param attribute
   * @param value
   * @return
   */
  public WebElementExtension hasAttributeWithValue(String attribute, String value) {
    isNotNull();
    assertThat(value).as("Do not use hasAttributeWithValue for values or null or empty String, use doesNotHaveAttribute").isNotNull().isNotSameAs("");
    assertThat(element.getAttribute(attribute)).as(getShouldText() + "have attribute with the name of \"" + attribute + "\" with a value of \"" + value+"\"").isEqualToIgnoringCase(value);
    return this;
  }

  /**
   * Uses the {@link WebElement#getAttribute(String)} to look the the attributes value.  The attribute input is taken as is, and no case-changes are made.
   * 
   * Ignores case differences in values if they exist.
   * 
   * @param attribute
   * @param value
   * @return
   */
  public WebElementExtension hasAttributeWithValue(String attribute, Boolean value) {
    isNotNull();
    assertThat(value).as("Do not use hasAttributeWithValue for values or null, use doesNotHaveAttribute").isNotNull();
    hasAttributeWithValue(attribute, value.toString().toLowerCase());
    return this;
  }

  public WebElementExtension isHidden() {
    isNotNull();
    assertThat(element.getCssValue("visibility")).as(getShouldText() + "have css property \"visibility\" with a value of either hidden or collapsed").isIn(Arrays.asList("hidden", "collapse"));
    return this;
  }

  public WebElementExtension isNotHidden() {
    isNotNull();
    hasCssPropertyWithValue("visibility", "visible");
    return this;
  }

  public WebElementExtension hasName(String name) {
    isNotNull();
    hasAttributeWithValue("name", name);
    return this;
  }

  public WebElementExtension hasValue(String value) {
    isNotNull();
    hasAttributeWithValue("value", value);
    return this;
  }

  public WebElementExtension textContains(String value) {
    isNotNull();
    assertThat(element.getText()).as(getShouldText() + "contain inner text like \"" + value + "\"").contains(value);
    return this;
  }
}

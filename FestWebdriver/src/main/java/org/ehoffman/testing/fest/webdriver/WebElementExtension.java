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
    assertThat(element.isSelected()).as(getShouldText() + "be selected").isTrue();
    return this;
  }

  public WebElementExtension isNotSelected() {
    assertThat(element.isSelected()).as(getShouldText() + "not be selected").isFalse();
    return this;
  }

  public WebElementExtension isEnabled() {
    assertThat(element.isEnabled()).as(getShouldText() + "be enabled").isTrue();
    return this;
  }

  public WebElementExtension isNotEnabled() {
    assertThat(element.isEnabled()).as(getShouldText() + "not be enabled").isFalse();
    return this;
  }

  public WebElementExtension hasTagName(String tagName) {
    assertThat(element.getTagName()).as(getShouldText() + "have expected tag name").isEqualToIgnoringCase(tagName);
    return this;
  }

  public WebElementExtension isDisplayed() {
    assertThat(element.isDisplayed()).as(getShouldText() + "be displayed").isTrue();
    return this;
  }

  public WebElementExtension isNotDisplayed() {
    assertThat(element.isDisplayed()).as(getShouldText() + "not be displayed").isFalse();
    return this;
  }

  public WebElementExtension hasHeight(Integer height) {
    assertThat(element.getSize().getHeight()).as(getShouldText() + "have a height of " + height + " but was " + element.getSize().getHeight()).isEqualTo(height);
    return this;
  }

  public WebElementExtension hasWidth(Integer width) {
    assertThat(element.getSize().getWidth()).as(getShouldText() + "have a width of " + width + " but was " + element.getSize().getWidth()).isEqualTo(width);
    return this;
  }

  public WebElementExtension hasSize(Dimension dimension) {
    assertThat(element.getSize().toString()).as(getShouldText() + "have dimensions of " + dimension + "but has dimensions of " + element.getSize()).isEqualTo(dimension.toString());
    return this;
  }
  
  public WebElementExtension doesNotHaveCssPropertyWithValue(String cssProperty){
    assertThat(element.getCssValue(cssProperty)).as(getShouldText() + "have not have a css property with the name of " + cssProperty ).isNullOrEmpty();
    return this;
  }

  public WebElementExtension hasCssPropertyWithValue(String cssProperty, String value) {
    assertThat(value).as("Do not use hasCssPropertyWithValue for values or null or empty String, use doesNotCssProperty").isNotNull().isNotEmpty();
    assertThat(element.getCssValue(cssProperty)).as(getShouldText() + "have css property with the name of " + cssProperty + " with a current computed value of " + value).isNotNull().isNotEmpty().isEqualTo(value);
    return this;
  }
  
  public WebElementExtension doesNotHaveAttribute(String attribute) {
    assertThat(booleanParameters).as("hasAttribute() is a meaningless inspection for boolean parameters").excludes(attribute);
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText() + "have not have an attribute with the name of " + attribute).isNotSameAs("");
    return this;
  }

  public WebElementExtension hasAttribute(String attribute) {
    assertThat(booleanParameters).as("hasAttribute() is a meaningless inspection for boolean parameters").excludes(attribute);
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText() + "have attribute with the name of " + attribute).isNotNull().isNotSameAs("");
    return this;
  }

  public WebElementExtension hasAttributeWithValue(String attribute, String value) {
    assertThat(value).as("Do not use hasAttributeWithValue for values or null or empty String, use doesNotHaveAttribute").isNotNull().isNotSameAs("");
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText() + "have attribute with the name of \"" + attribute + "\" with a value of \"" + value+"\"").isEqualTo(value);
    return this;
  }

  public WebElementExtension hasAttributeWithValue(String attribute, Boolean value) {
    assertThat(value).as("Do not use hasAttributeWithValue for values or null, use doesNotHaveAttribute").isNull();
    hasAttributeWithValue(attribute, value.toString().toLowerCase());
    return this;
  }

  public WebElementExtension isHidden() {
    assertThat(element.getCssValue("visibility")).as(getShouldText() + "have css property \"visibility\" with a value of either hidden or collapsed").isIn(Arrays.asList("hidden", "collapse"));
    return this;
  }

  public WebElementExtension isNotHidden() {
    hasCssPropertyWithValue("visibility", "visible");
    return this;
  }

  public WebElementExtension hasName(String name) {
    hasAttributeWithValue("name", name);
    return this;
  }

  public WebElementExtension hasValue(String value) {
    hasAttributeWithValue("value", value);
    return this;
  }

  public WebElementExtension textContains(String value) {
    assertThat(element.getText()).as(getShouldText() + "contain inner text like \"" + value + "\"").contains(value);
    return this;
  }

}

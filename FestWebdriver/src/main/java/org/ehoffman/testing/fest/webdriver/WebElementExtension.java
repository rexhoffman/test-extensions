package org.ehoffman.testing.fest.webdriver;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.AssertExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

public class WebElementExtension implements AssertExtension  {
  private final WebElement element;

  private final static String[] booleanParameters = "async, autofocus, autoplay, checked, compact, complete, controls, declare, defaultchecked, defaultselected, defer, disabled, draggable, ended, formnovalidate, hidden, indeterminate, iscontenteditable, ismap, itemscope, loop, multiple, muted, nohref, noresize, noshade, novalidate, nowrap, open, paused, pubdate, readonly, required, reversed, scoped, seamless, seeking, selected, spellcheck, truespeed, willvalidate".split(", ");

  protected String getWebElementId(){
    return ""+element;
  }

  public WebElementExtension(WebElement element){
    this.element = element;
  }

  private String getShouldText(){
    return "WebElement "+getWebElementId()+" should ";
  }


  public WebElementExtension isSelected() {
    assertThat(element.isSelected()).as(getShouldText()+"be selected").isTrue();
    return this;
  }

  public WebElementExtension isNotSelected() {
    assertThat(element.isSelected()).as(getShouldText()+"not be selected").isFalse();
    return this;
  }

  public WebElementExtension isEnabled() {
    assertThat(element.isEnabled()).as(getShouldText()+"be enabled").isTrue();
    return this;
  }

  public WebElementExtension isNotEnabled() {
    assertThat(element.isEnabled()).as(getShouldText()+"not be enabled").isFalse();
    return this;
  }

  public WebElementExtension hasTagName(String tagName) {
    assertThat(element.getTagName()).as(getShouldText()+"have expected tag name").isEqualToIgnoringCase(tagName);
    return this;
  }

  public WebElementExtension isDisplayed(){
    assertThat(element.isDisplayed()).as(getShouldText()+"be displayed").isTrue();
    return this;
  }

  public WebElementExtension isNotDisplayed(){
    assertThat(element.isDisplayed()).as(getShouldText()+"not be displayed").isFalse();
    return this;
  }

  public WebElementExtension hasHeight(Integer height){
    assertThat(element.getSize().getHeight()).as(getShouldText()+"have a height of "+height+" but was "+element.getSize().getHeight()).isEqualTo(height);
    return this;
  }

  public WebElementExtension hasWidth(Integer width){
    assertThat(element.getSize().getWidth()).as(getShouldText()+"have a width of "+width+" but was "+element.getSize().getHeight()).isEqualTo(width);
    return this;
  }

  public WebElementExtension hasSize(Dimension dimension){
    assertThat(element.getSize().toString()).as(getShouldText()+"have dimensions of "+dimension+ "but has dimensions of "+element.getSize()).isEqualTo(dimension.toString());
    return this;
  }

  public WebElementExtension hasAttribute(String attribute){
    assertThat(booleanParameters).as("hasAttribute() is a meaningless inspection for boolean parameters").excludes(attribute);
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText()+"have attribute with the name of "+attribute).isNotNull().isNotEmpty();
    return this;
  }

  public WebElementExtension hasAttributeWithValue(String attribute, String value){
    assertThat(element.getAttribute(attribute.toLowerCase())).as(getShouldText()+"have attribute with the name of "+attribute+" with a value of "+value).isEqualTo(value);
    return this;
  }

  public WebElementExtension hasAttributeWithValue(String attribute, Boolean value){
    hasAttributeWithValue(attribute, value.toString().toLowerCase());
    return this;
  }

  public WebElementExtension isHidden(){
    hasAttributeWithValue("hidden", true);
    return this;
  }

  public WebElementExtension isNotHidden(){
    hasAttributeWithValue("hidden", false);
    return this;
  }

  public WebElementExtension hasName(String tagName){
    hasAttributeWithValue("name",tagName);
    return this;
  }

  public WebElementExtension hasValue(String value){
    hasAttributeWithValue("value",value);
    return this;
  }

  public WebElementExtension textContains(String value){
    assertThat(element.getText()).as(getShouldText()+"contain inner text like \""+value+"\"").contains(value);
    return this;
  }

}


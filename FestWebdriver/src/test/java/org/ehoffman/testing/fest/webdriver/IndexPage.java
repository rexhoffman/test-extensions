package org.ehoffman.testing.fest.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class IndexPage {

  public IndexPage(WebDriver driver) {
    if (!"Awesome Page".equals(driver.getTitle())) {
        throw new IllegalStateException("This is not the Awesome Page"); // or nav there if you know the url
    }
  }

  @FindBy(id = "input1")
  private WebElement input1;
  
  @FindBy(id = "input2hidden")
  private WebElement input2hidden;
  
  @FindBy(id = "input3disabled")
  private WebElement input3disabled;
  
  @FindBy(id = "linky")
  private WebElement linky;
  
  @FindBy(id = "selected")
  private WebElement selected;
  
  @FindBy(id = "not_selected")
  private WebElement not_selected;

  private Integer someOtherBeanProperty;

  public Integer getSomeOtherBeanProperty() {
    return someOtherBeanProperty;
  }

  public void setSomeOtherBeanProperty(Integer someOtherBeanProperty) {
    this.someOtherBeanProperty = someOtherBeanProperty;
  }

  public WebElement getInput1() {
    return input1;
  }

  public void setInput1(WebElement input1) {
    this.input1 = input1;
  }

  public WebElement getInput2hidden() {
    return input2hidden;
  }

  public void setInput2hidden(WebElement input2hidden) {
    this.input2hidden = input2hidden;
  }

  public WebElement getInput3disabled() {
    return input3disabled;
  }

  public void setInput3disabled(WebElement input3disabled) {
    this.input3disabled = input3disabled;
  }

  public WebElement getLinky() {
    return linky;
  }

  public void setLinky(WebElement linky) {
    this.linky = linky;
  }

  public WebElement getSelected() {
    return selected;
  }

  public void setSelected(WebElement selected) {
    this.selected = selected;
  }

  public WebElement getNot_selected() {
    return not_selected;
  }

  public void setNot_selected(WebElement not_selected) {
    this.not_selected = not_selected;
  }

}
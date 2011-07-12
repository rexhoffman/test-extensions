package org.github.fest.webdriver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage {
  private WebDriver driver;
  public LoginPage(WebDriver driver){
    this.setDriver(driver);
  }
  protected void setDriver(WebDriver driver) {
    this.driver = driver;
  }
  protected WebDriver getDriver() {
    return driver;
  }
  	
	@FindBy(id = "username")
	private WebElement username;
	@FindBy(id = "password")
	private WebElement password;
	@FindBy(id = "login")
	private WebElement loginButton;
	
  public WebElement getUsername() {
    return username;
  }
  public WebElement getPassword() {
    return password;
  }
  public WebElement getLoginButton() {
    return loginButton;
  }

	
	
}
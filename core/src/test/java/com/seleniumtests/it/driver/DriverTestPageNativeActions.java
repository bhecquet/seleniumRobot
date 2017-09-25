package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;

public class DriverTestPageNativeActions extends PageObject {
	
	public static final TextFieldElement textElement = new TextFieldElement("Text", By.id("text2"));

	public DriverTestPageNativeActions() throws Exception {
        super(textElement);
    }
    
    public DriverTestPageNativeActions(boolean openPageURL) throws Exception {
        super(textElement, openPageURL ? getPageUrl() : null);
    }
    
    public void sendKeys() {
    	driver.findElement(By.id("text2")).sendKeys("some text");
    }
    
    public void reset() {
    	driver.findElement(By.id("button2")).click();
    }
    
    public void select() {
    	new Select(driver.findElement(By.id("select"))).selectByVisibleText("option1");
    }
    
    public WebElement getElement() {
    	return driver.findElement(By.id("text2"));
    }
    
    public void switchToFrameByElement() {
    	WebElement el = driver.findElement(By.id("myIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToFrameByIndex() {
    	driver.switchTo().frame(0);
    }
    
    public void switchToFrameByNameOrId() {
    	driver.switchTo().frame("myIFrame");
    }
    
    public static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
    
}

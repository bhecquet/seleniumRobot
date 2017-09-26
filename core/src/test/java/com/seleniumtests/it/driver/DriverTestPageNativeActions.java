package com.seleniumtests.it.driver;

import java.util.List;

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
    
    public void switchToFirstFrameByElement() {
    	WebElement el = driver.findElement(By.id("myIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToSecondFrameByElement() {
    	WebElement el = driver.findElement(By.name("mySecondIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToFirstFrameByIndex() {
    	driver.switchTo().frame(0);
    }
    
    public void switchToFirstFrameByNameOrId() {
    	driver.switchTo().frame("myIFrame");
    }
    
    public void switchDefaultContent() {
    	driver.switchTo().defaultContent();
    }
    
    /** 
     * must be called after switchToFrameByElement
     * @return
     */
    public WebElement getElementInsideFrame() {
    	return driver.findElement(By.id("textInIFrameWithValue"));
    }
    
    /** 
     * must be called after switchToFrameByElement
     * @return
     */
    public List<WebElement> getElementsInsideFrame() {
    	return driver.findElements(By.tagName("input"));
    }
    
    /** 
     * must be called after switchToFirstFrameByElement and switchToSecondFrameByElement
     * @return
     */
    public WebElement getElementInsideFrameOfFrame() {
    	return driver.findElement(By.id("textInIFrameWithValue2"));
    }
    
    public static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
    
}

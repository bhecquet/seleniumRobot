/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver.support.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.uipage.PageObject;

public class DriverTestPageObjectFatory extends PageObject {
	
	@FindBy(id="text2")
	public WebElement textElement;
	
	@FindBy(tagName = "input")
	public List<WebElement> inputElements;
	
	@FindBy(id = "text20")
	public WebElement textElementNotFound;
	
	@FindBy(id = "button2")
	public WebElement resetButton;
	
	@FindBy(id = "select")
	public WebElement select;
	
	@FindBy(name = "mySecondIFrame")
	public WebElement frame2;
	
	@FindBy(id = "myIFrame")
	public WebElement frame1;
	
	@FindBy(id = "textInIFrameWithValue")
	public WebElement textInIFrameWithValue;
	
	@FindBy(id = "textInIFrameWithValue2")
	public WebElement textInIFrameWithValue2;

	@FindBy(id = "textInIFrameWithValue3")
	public WebElement textInIFrameWithValue3;
	
	@FindAll({@FindBy(id="text2"), @FindBy(id = "button2")})
	public List<WebElement> allElements;
	
	@FindBys({@FindBy(id="labelTests"), @FindBy(tagName = "input")})
	public List<WebElement> inputsUnderLabel;

	public DriverTestPageObjectFatory()  {
        super();
    }

    public DriverTestPageObjectFatory(boolean openPageURL) {
    	super(null, openPageURL ? getPageUrl() : null);
    	PageFactory.initElements(driver, this);
    }
    
    public DriverTestPageObjectFatory(boolean openPageURL, String url) {
    	super(null, openPageURL ? url : null);
    	PageFactory.initElements(driver, this);
    }
    
    public DriverTestPageObjectFatory sendKeys() {
    	textElement.sendKeys("some text");
    	return this;
    }
    
    public DriverTestPageObjectFatory sendKeysFailed() {
    	textElementNotFound.sendKeys("some text");
    	return this;
    }
    
    public DriverTestPageObjectFatory reset() {
    	resetButton.click();
    	return this;
    }
    
    public DriverTestPageObjectFatory select() {
    	new Select(select).selectByVisibleText("option1");
    	return this;
    }
    
    public WebElement getElement() {
    	return textElement;
    }
    
    public void switchToFirstFrameByElement() {
    	driver.switchTo().frame(frame1);
    }
    
    public void switchToSubFrame() {
    	driver.switchTo().frame(frame2);
    }
    
    public void switchToSecondFrameByElement() {
    	driver.switchTo().frame(frame2);
    }
    
    public DriverTestPageObjectFatory switchToFirstFrameByIndex() {
    	driver.switchTo().frame(0);
		return this;
    }
    
    public DriverTestPageObjectFatory switchToFrameWithExpectedConditionsById() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame1));
		return this;
    }
    
    public DriverTestPageObjectFatory switchToFrameWithExpectedConditionsByName() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("myIFrame"));
		return this;
    }
    
    public DriverTestPageObjectFatory switchToFrameWithExpectedConditionsByIndex() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));
		return this;
    }
    
    public DriverTestPageObjectFatory switchToFirstFrameByNameOrId() {
    	driver.switchTo().frame("myIFrame");
		return this;
    }
    
    public DriverTestPageObjectFatory switchDefaultContent() {
    	driver.switchTo().defaultContent();
		return this;
    }
    
    public DriverTestPageObjectFatory switchParentFrame() {
    	driver.switchTo().parentFrame();
		return this;
    }
    
    /** 
     * must be called after switchToFrameByElement
     */
    public WebElement getElementInsideFrame() {
    	return textInIFrameWithValue;
    }
    
    /** 
     * must be called after switchToFrameByElement
     */
    public List<WebElement> getElementsInsideFrame() {
    	return inputElements;
    }
    
    /**
     * Use  @FindAll
     */
    public List<WebElement> getFindAllElements() {
    	return allElements;
    }
    /**
     * Use  @FindBys
     */
    public List<WebElement> getFindBysElements() {
    	return inputsUnderLabel;
    }
    
    /** 
     * must be called after switchToFirstFrameByElement and switchToSecondFrameByElement
     */
    public WebElement getElementInsideFrameOfFrame() {
    	return textInIFrameWithValue3;
    }
    
    public static String getPageUrl() {
    	if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.FIREFOX) {
			return "file://" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		} else {
			return "file:///" + Thread.currentThread().getContextClassLoader().getResource("tu/test.html").getFile();
		}
    }
    
}

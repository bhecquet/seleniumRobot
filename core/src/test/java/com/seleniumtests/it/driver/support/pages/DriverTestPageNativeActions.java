/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver.support.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    public DriverTestPageNativeActions(boolean openPageURL, String url) throws Exception {
    	super(textElement, openPageURL ? url : null);
    }
    
    public DriverTestPageNativeActions sendKeys() {
    	driver.findElement(By.id("text2")).sendKeys("some text");
    	return this;
    }
    
    public DriverTestPageNativeActions sendKeysFailed() {
    	driver.findElement(By.id("text20")).sendKeys("some text");
    	return this;
    }
    
    public DriverTestPageNativeActions reset() {
    	driver.findElement(By.id("button2")).click();
    	return this;
    }
    
    public DriverTestPageNativeActions select() {
    	new Select(driver.findElement(By.id("select"))).selectByVisibleText("option1");
    	return this;
    }
    
    public WebElement getElement() {
    	return driver.findElement(By.id("text2"));
    }
    
    public void switchToFirstFrameByElement() {
    	WebElement el = driver.findElement(By.id("myIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToSubFrame() {
    	WebElement el = driver.findElement(By.name("mySecondIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToSecondFrameByElement() {
    	WebElement el = driver.findElement(By.name("mySecondIFrame"));
    	driver.switchTo().frame(el);
    }
    
    public void switchToFirstFrameByIndex() {
    	driver.switchTo().frame(0);
    }
    
    public void switchToFrameWithExpectedConditionsById() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("myIFrame")));
    }
    
    public void switchToFrameWithExpectedConditionsByName() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("myIFrame"));
    }
    
    public void switchToFrameWithExpectedConditionsByIndex() {
    	new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));
    }
    
    public void switchToFirstFrameByNameOrId() {
    	driver.switchTo().frame("myIFrame");
    }
    
    public void switchDefaultContent() {
    	driver.switchTo().defaultContent();
    }
    
    public void switchParentFrame() {
    	driver.switchTo().parentFrame();
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

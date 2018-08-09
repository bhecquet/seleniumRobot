/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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
package com.seleniumtests.uipage;

import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;

/**
 * Base html page abstraction. Used by PageObject and WebPageSection
 */
public abstract class BasePage {

    protected WebDriver driver;
    protected WebUIDriver webUXDriver;
    private int explictWaitTimeout = SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout();
    

    public BasePage() {
    	init();
    }
    
    public void init() {
    	driver = WebUIDriver.getWebDriver();
    	webUXDriver = WebUIDriver.getWebUIDriver(false);
    }

    public void acceptAlert() {
        Alert alert = getAlert();
        alert.accept();
        driver.switchTo().defaultContent();
    }
    
    public String cancelConfirmation() {
    	Alert alert = getAlert();
        String seenText = alert.getText();
        alert.dismiss();
        driver.switchTo().defaultContent();
        return seenText;
    }
    
    public Alert getAlert() {
    	new WebDriverWait(driver, 2).until(ExpectedConditions.alertIsPresent());
        return driver.switchTo().alert();
    }

    public String getAlertText() {
    	Alert alert = getAlert();
        return alert.getText();
    }
 
    protected void assertCurrentPage(final boolean log) { }

    
    public void assertHTML(final boolean condition, final String message) {
        if (!condition) {
            capturePageSnapshot();
            Assert.assertTrue(condition, message);
        }
    }

    protected abstract void capturePageSnapshot();

    public WebDriver getDriver() {
        return WebUIDriver.getWebDriver();
    }

    public boolean isTextPresent(final String text) {
    	if (SeleniumTestsContextManager.isWebTest()) {
	        Assert.assertNotNull(text, "isTextPresent: text should not be null!");
	        driver = getDriver();
	
	        WebElement body = driver.findElement(By.tagName("body"));
	
	        return body.getText().contains(text);
    	}
        return false;
    }    

    /**
     * If current window is closed then use driver.switchTo.window(handle).
     *
     * @param   windowName
     */
    public final void selectWindow(final String windowName) {
    	
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            throw new ScenarioException("Application are not compatible with Windows");
        }
    	
        if (windowName == null) {
        	driver.switchTo().defaultContent();
        } else {
        	driver.switchTo().window(windowName);
        }
    }

    /**
     * Wait for a condition
     * @param condition		condition available using ExpectedConditions
     * 						e.g: ExpectedConditions.presenceOfElementLocated(by)
     */
    public void waitForCondition(final ExpectedCondition<WebElement> condition) {
    	
    	WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
    	wait.until(condition);
    }

	public Set<String> getCurrentHandles() {
		return ((CustomEventFiringWebDriver)driver).getCurrentHandles();
	}

}

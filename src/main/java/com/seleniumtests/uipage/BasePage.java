/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import com.thoughtworks.selenium.webdriven.Windows;

/**
 * Base html page abstraction. Used by PageObject and WebPageSection
 */
public abstract class BasePage {

    protected WebDriver driver;// = WebUIDriver.getWebDriver();
    protected WebUIDriver webUXDriver; // = WebUIDriver.getWebUIDriver();
    private int explictWaitTimeout = SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout();
    

    public BasePage() {
    	init();
    }
    
    public void init() {
    	driver = WebUIDriver.getWebDriver();
    	webUXDriver = WebUIDriver.getWebUIDriver();
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
        Assert.assertNotNull(text, "isTextPresent: text should not be null!");
        driver = getDriver();

        WebElement body = driver.findElement(By.tagName("body"));

        if (SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.HTMLUNIT) {
            return body.getText().contains(text);
        }

        if (body.getText().contains(text)) {
            return true;
        }

        JavascriptLibrary js = new JavascriptLibrary();
        String script = js.getSeleniumScript("isTextPresent.js");

        Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript("return (" + script + ")(arguments[0]);", text);

        // Handle the null case
        return Boolean.TRUE == result;
    }

    public void selectFrame(final By by) {
        TestLogging.logWebStep("select frame, locator={\"" + by.toString() + "\"}", false);
        driver.switchTo().frame(driver.findElement(by));
    }
    
    public boolean isFrame() {
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
    	
    	Windows windows = null;
    	try {
    		windows = new Windows(driver);
    	} catch (NoSuchWindowException e) {
    		driver.switchTo().window(windowName);
    		return;
    	}
    	
        if (windowName == null) {
            try {
                windows.selectBlankWindow(driver);
            } catch (SeleniumException e) {
                driver.switchTo().defaultContent();
            }

        } else {
            windows.selectWindow(driver, "name=" + windowName);
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

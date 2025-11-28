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
package com.seleniumtests.uipage;

import java.time.Duration;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.util.logging.ScenarioLogger;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Base html page abstraction. Used by PageObject and WebPageSection
 */
public abstract class BasePage {

    protected CustomEventFiringWebDriver customEventFiringWebDriver;
    protected WebDriver driver;
    private final int explictWaitTimeout = SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout();

	protected static final ScenarioLogger logger = ScenarioLogger.getScenarioLogger(BasePage.class);  // with this logger, information will be added in test step + logs
	protected static final Logger internalLogger = SeleniumRobotLogger.getLogger(BasePage.class);
    
    public Alert getAlert() {
    	new WebDriverWait(customEventFiringWebDriver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
        return customEventFiringWebDriver.switchTo().alert();
    }

    public String getAlertText() {
    	Alert alert = getAlert();
        return alert.getText();
    }
 
    protected void assertCurrentPage(final boolean log) { }
    protected void assertCurrentPage(boolean log, HtmlElement pageIdentifierElement) { }

    public CustomEventFiringWebDriver getDriver() {
        return customEventFiringWebDriver;
    }
    
    /**
     * For unit tests because in test scenarios, driver is already created on page initialization
     */
	public void setDriver(CustomEventFiringWebDriver driver) {
		this.customEventFiringWebDriver = driver;
	}

    public boolean isTextPresent(final String text) {
    	if (customEventFiringWebDriver.isWebTest()) {
	        Assert.assertNotNull(text, "isTextPresent: text should not be null!");
	        
	        WebElement body;
	        try {
	        	body = customEventFiringWebDriver.findElement(By.tagName("body"));
	        } catch (WebDriverException e) {
	        	return false;
	        }
	
	        return body.getText().contains(text);
    	}
        return false;
    }    

    /**
     * If current window is closed then use driver.switchTo.window(handle).
     *
     * @param   windowName  name of the window
     */
    public final void selectWindow(final String windowName) {
    	
    	// app test are not compatible with window
        TestType testType = SeleniumTestsContextManager.getThreadContext().getTestType();
        if (testType.family() == TestType.APP && testType.isMobile()) {
            throw new ScenarioException("Mobile application are not compatible with Windows");
        }
    	
        if (windowName == null) {
            customEventFiringWebDriver.switchTo().defaultContent();
        } else {
            customEventFiringWebDriver.switchTo().window(windowName);
        }
    }

    /**
     * Wait for a condition
     * @param condition		condition available using ExpectedConditions
     * 						e.g: ExpectedConditions.presenceOfElementLocated(by)
     */
    public void waitForCondition(final ExpectedCondition<WebElement> condition) {
    	
    	WebDriverWait wait = new WebDriverWait(customEventFiringWebDriver, Duration.ofSeconds(explictWaitTimeout));
    	wait.until(condition);
    }

	public Set<String> getCurrentHandles() {
		return customEventFiringWebDriver.getCurrentHandles();
	}
	



}

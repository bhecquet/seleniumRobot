/*
 * Copyright 2015 www.seleniumtests.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.CustomAssertion;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.util.helper.WaitHelper;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.Wait;
import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
import com.thoughtworks.selenium.webdriven.Windows;

/**
 * Base html page abstraction. Used by PageObject and WebPageSection
 */
public abstract class BasePage {

    protected WebDriver driver = WebUIDriver.getWebDriver();
    protected final WebUIDriver webUXDriver = WebUIDriver.getWebUIDriver();
    private int explictWaitTimeout = SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout();
    private int sessionTimeout = SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout();
    

    public BasePage() { 
    }

    public void acceptAlert() {
        Alert alert = driver.switchTo().alert();
        alert.accept();
        driver.switchTo().defaultContent();
    }

    public void assertAlertPresent() {
        TestLogging.logWebStep("assert alert present.", false);
        try {
            driver.switchTo().alert();
        } catch (Exception ex) {
            assertAlertHTML(false, "assert alert present.");
        }
    }

    public void assertAlertText(final String text) {
        TestLogging.logWebStep("assert alert text.", false);

        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        assertAlertHTML(alertText.contains(text), "assert alert text.");
    }

    /**
     * @param  element
     * @param  attributeName
     * @param  value
     */
    public void assertAttribute(final HtmlElement element, final String attributeName, final String value) {
        TestLogging.logWebStep(String.format("assert %s attribute = %s, expectedValue ={%s}.",
        		element.toHTML(),
        		attributeName,
        		value),
            false);

        String attributeValue = element.getAttribute(attributeName);

        assertHTML(value != null && value.equals(attributeValue),
        		String.format("%s attribute = %s, expectedValue = {%s}, attributeValue = {%s}",
                        element.toString(),
                        attributeName,
                        attributeValue,
                        value
                        )
            );
    }

    public void assertAttributeContains(final HtmlElement element, final String attributeName, final String keyword) {
        TestLogging.logWebStep(String.format("assert %s attribute = %s, contains keyword ={%s}.",
        		element.toHTML(),
        		attributeName,
        		keyword),  
            false);

        String attributeValue = element.getAttribute(attributeName);

        assertHTML(attributeValue != null && keyword != null && attributeValue.contains(keyword),
        		String.format("%s attribute = %s, expected to contains keyword = {%s}, attributeValue = {%s}",
                        element.toString(),
                        attributeName,
                        keyword,
                        attributeValue
                        ));
    }

    public void assertAttributeMatches(final HtmlElement element, final String attributeName, final String regex) {
        TestLogging.logWebStep(String.format("assert %s attribute = %s, matches regex ={%s}.",
        		element.toHTML(),
        		attributeName,
        		regex),  
            false);

        String attributeValue = element.getAttribute(attributeName);

        assertHTML(attributeValue != null && regex != null && attributeValue.matches(regex),
        		String.format("%s attribute = %s, expected to match regex = {%s}, attributeValue = {%s}",
                        element.toString(),
                        attributeName,
                        regex,
                        attributeValue
                        )
        		);
    }

    public void assertConfirmationText(final String text) {
        TestLogging.logWebStep("assert confirmation text.", false);

        Alert alert = driver.switchTo().alert();
        String seenText = alert.getText();

        assertAlertHTML(seenText.contains(text), "assert confirmation text.");
    }

    protected void assertCurrentPage(final boolean log) { }

    public void assertElementNotPresent(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is not present.", element.toHTML()), false);
        assertHTML(!element.isElementPresent(), String.format("%s found.", element.toString()));
    }

    public void assertElementPresent(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is present.", element.toHTML()), false);
        assertHTML(element.isElementPresent(), String.format("%s not found.", element.toString()));
    }

    public void assertElementEnabled(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is enabled.", element.toHTML()), false);
        assertHTML(element.isEnabled(), String.format("%s not found.", element.toString()));
    }

    public void assertElementNotEnabled(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is not enabled.", element.toHTML()), false);
        assertHTML(!element.isEnabled(), String.format("%s not found.", element.toString()));
    }

    public void assertElementDisplayed(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is displayed.", element.toHTML()), false);
        assertHTML(element.isDisplayed(), String.format("%s not found.", element.toString()));
    }

    public void assertElementSelected(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is selected.", element.toHTML()), false);
        assertHTML(element.isSelected(), String.format("%s not found.", element.toString()));
    }

    public void assertElementNotSelected(final HtmlElement element) {
        TestLogging.logWebStep(String.format("assert %s is NOT selected.", element.toHTML()), false);
        assertHTML(!element.isSelected(), String.format("%s not found.", element.toString()));
    }

    public void assertCondition(final boolean condition, final String message) {
        TestLogging.logWebStep("assert that " + message, false);
        assert condition;
    }

    void assertHTML(final boolean condition, final String message) {
        if (!condition) {
            capturePageSnapshot();
            CustomAssertion.assertTrue(condition, message);
        }
    }

    void assertAlertHTML(final boolean condition, final String message) {
        if (!condition) {
            CustomAssertion.assertTrue(condition, message);
        }
    }

    public void assertPromptText(final String text) {
        TestLogging.logWebStep("assert prompt text.", false);

        Alert alert = driver.switchTo().alert();
        String seenText = alert.getText();
        assertAlertHTML(seenText.contains(text), "assert prompt text.");
    }

    public void assertTable(final Table table, final int row, final int col, final String text) {
        TestLogging.logWebStep(
        		String.format("assert text %s equals %s at (row, col) = (%d, %d).",
                		text,
                		table.toHTML(),
                		row, 
                		col), 
            false);

        String content = table.getContent(row, col);
        assertHTML(content != null && content.equals(text),
        		String.format("Text= {%s} not found on %s at cell(row, col) = {%d,%d}",
        				text,
        				table.toString(),
        				row,
        				col)
           );
    }

    public void assertTableContains(final Table table, final int row, final int col, final String text) {
        TestLogging.logWebStep(
        		String.format("assert text %s contains %s at (row, col) = (%d, %d).",
                		text,
                		table.toHTML(),
                		row, 
                		col), 
            false);

        String content = table.getContent(row, col);
        assertHTML(content != null && content.contains(text),
        		String.format("Text= {%s} not found on %s at cell(row, col) = {%d,%d}",
        				text,
        				table.toString(),
        				row,
        				col));
    }

    public void assertTableMatches(final Table table, final int row, final int col, final String text) {
        TestLogging.logWebStep(
        		String.format("assert text %s matches %s at (row, col) = (%d, %d).",
                		text,
                		table.toHTML(),
                		row, 
                		col), 
            false);

        String content = table.getContent(row, col);
        assertHTML(content != null && content.matches(text),
        		String.format("Text= {%s} not found on %s at cell(row, col) = {%d,%d}",
        				text,
        				table.toString(),
        				row,
        				col)
           );
    }

    public void assertTextNotPresent(final String text) {
        TestLogging.logWebStep("assert text \"" + text + "\" is not present.", false);
        assertHTML(!isTextPresent(text), "Text= {" + text + "} found.");
    }

    public void assertTextNotPresentIgnoreCase(final String text) {
        TestLogging.logWebStep("assert text \"" + text + "\" is not present.(ignore case)", false);
        assertHTML(!getBodyText().toLowerCase().contains(text.toLowerCase()), "Text= {" + text + "} found.");
    }

    public void assertTextPresent(final String text) {
        TestLogging.logWebStep("assert text \"" + text + "\" is present.", false);
        assertHTML(isTextPresent(text), "Text= {" + text + "} not found.");
    }

    public void assertTextPresentIgnoreCase(final String text) {
        TestLogging.logWebStep("assert text \"" + text + "\" is present.(ignore case)", false);
        assertHTML(getBodyText().toLowerCase().contains(text.toLowerCase()), "Text= {" + text + "} not found.");
    }

    public String cancelConfirmation() {
        Alert alert = driver.switchTo().alert();
        String seenText = alert.getText();
        alert.dismiss();
        driver.switchTo().defaultContent();
        return seenText;
    }

    protected abstract void capturePageSnapshot();

    public Alert getAlert() {
        return driver.switchTo().alert();
    }

    public String getAlertText() {
        Alert alert = driver.switchTo().alert();
        return alert.getText();
    }

    private String getBodyText() {
        WebElement body = driver.findElement(By.tagName("body"));
        return body.getText();
    }

    public String getConfirmation() {
        Alert alert = driver.switchTo().alert();
        return alert.getText();
    }

    public WebDriver getDriver() {
        driver = WebUIDriver.getWebDriver();
        return driver;

    }

    public String getPrompt() {
        Alert alert = driver.switchTo().alert();
        return alert.getText();
    }

    /**
     * Return true if element is present
     * @param by
     * @param timeout timeout in seconds
     * @return
     */
    public boolean isElementPresent(final By by, final int timeout) {
    	
    	try {
    		waitForElementPresent(by, timeout);
    		return true;
    	} catch (TimeoutException e) {
    		return false;
    	}
    }
    public boolean isElementPresent(final By by) {
    	return isElementPresent(by, 10);
    }
    
    

    public boolean isFrame() {
        return false;
    }

    public boolean isTextPresent(final String text) {
        CustomAssertion.assertNotNull(text, "isTextPresent: text should not be null!");
        driver = WebUIDriver.getWebDriver();

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
    

    /**
     * If current window is closed then use driver.switchTo.window(handle).
     *
     * @param   windowName
     */
    public final void selectWindow(final String windowName) {
    	
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
    
    private void checkNullElement(final HtmlElement element) {
    	Assert.assertNotNull(element, "Element can't be null");
    }

    public void waitForElementChecked(final HtmlElement element) {
        checkNullElement(element);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.elementToBeSelected(element.getBy()));
    }

    public void waitForElementEditable(final HtmlElement element) {
        checkNullElement(element);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.elementToBeClickable(element.getBy()));
    }

    public void waitForElementPresent(final By by) {

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    /**
     * 
     * @param by
     * @param timeout	timeout in seconds
     */
    public void waitForElementPresent(final By by, final int timeout) {

        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public void waitForElementPresent(final HtmlElement element) {
        checkNullElement(element);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(element.getBy()));
    }

    public void waitForElementToBeVisible(final HtmlElement element) {
        checkNullElement(element);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.visibilityOfElementLocated(element.getBy()));
    }

    public void waitForElementToDisappear(final HtmlElement element) {
        checkNullElement(element);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(element.getBy())); 
    }

    public void waitForPopup(final String locator) {
        waitForPopup(locator, Integer.toString(sessionTimeout) + "");
    }

    public void waitForPopup(final String windowID, final String timeout) {
        final long millis = Long.parseLong(timeout);
        final String current = driver.getWindowHandle();
        final Windows windows = new Windows(driver);

        if (webUXDriver.getConfig().getBrowser() == BrowserType.INTERNETEXPLORER) {
            WaitHelper.waitForSeconds(3);
        }

        new Wait() {
            @Override
            public boolean until() {
                try {
                    if ("_blank".equals(windowID)) {
                        windows.selectBlankWindow(driver);
                    } else {
                        driver.switchTo().window(windowID);
                    }

                    return !"about:blank".equals(driver.getCurrentUrl());
                } catch (SeleniumException|NoSuchWindowException e) {
                	// ignore
                }

                return false;
            }
        }.wait(String.format("Timed out waiting for %s. Waited %s", windowID, timeout), millis);

        driver.switchTo().window(current);

    }
    
    private void checkTextNull(String text) {
    	Assert.assertNotNull(text, "Text can't be null");
    }

    public void waitForTextPresent(final HtmlElement element, final String text) {
        checkTextNull(text);

        WebDriverWait wait = new WebDriverWait(driver, explictWaitTimeout);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(element.getBy(), text));
    }

    public void waitForTextPresent(final String text) {
        checkTextNull(text);

        boolean b = false;
        for (int millisec = 0; millisec < explictWaitTimeout * 1000; millisec += 1000) {
            try {
                if (isTextPresent(text)) {
                    b = true;
                    break;
                }
            } catch (Exception ignore) {
            	// ignore
            }

            WaitHelper.waitForSeconds(1);
        }

        assertHTML(b, "Timed out waiting for text \"" + text + "\" to be there.");
    }

    public void waitForTextToDisappear(final String text) {
        checkTextNull(text);

        boolean textPresent = true;
        for (int millisec = 0; millisec < explictWaitTimeout * 1000; millisec += 1000) {
            try {
                if (!(isTextPresent(text))) {
                    textPresent = false;
                    break;
                }
            } catch (Exception ignore) { 
            	// ignore
            }

            WaitHelper.waitForSeconds(1);
        }

        assertHTML(!textPresent, "Timed out waiting for text \"" + text + "\" to be gone.");
    }

	public Set<String> getCurrentHandles() {
		return ((CustomEventFiringWebDriver)driver).getCurrentHandles();
	}

}

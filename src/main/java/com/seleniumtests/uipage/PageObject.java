/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.SystemClock;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.api.DesktopScreenRegion;
import org.sikuli.api.robot.Mouse;
import org.sikuli.api.robot.desktop.DesktopMouse;
import org.testng.Assert;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.seleniumtests.core.CustomAssertion;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.SeleniumTestsPageListener;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.NotCurrentPageException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.JavaScriptError;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.WebUtility;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.util.helper.ContextHelper;
import com.seleniumtests.util.helper.WaitHelper;

public class PageObject extends BasePage implements IPage {

	private static final Logger logger = TestLogging.getLogger(PageObject.class);
    private boolean frameFlag = false;
    private HtmlElement pageIdentifierElement = null;
    private String popupWindowName = null;
    private String windowHandle = null;
    private String title = null;
    private String url = null;
    private String bodyText = null;
    private String htmlSource = null;
    private String htmlSavedToPath = null;
    private String suiteName = null;
    private String outputDirectory = null;
    private String htmlFilePath = null;
    private String imageFilePath = null;
    private SystemClock systemClock;

    /**
     * Constructor for non-entry point page. The control is supposed to have reached the page from other API call.
     *
     * @throws  Exception
     */
    public PageObject() throws IOException {
        this(null, null);
    }

    /**
     * Constructor for non-entry point page. The control is supposed to have reached the page from other API call.
     *
     * @param   pageIdentifierElement
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement) throws IOException  {
        this(pageIdentifierElement, null);
    }

    /**
     * Base Constructor.
     *
     * @param   url
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement, final String url) throws IOException {

    	systemClock = new SystemClock();
        Calendar start = Calendar.getInstance();
        start.setTime(new Date());

        if (SeleniumTestsContextManager.getGlobalContext() != null
                && SeleniumTestsContextManager.getGlobalContext().getTestNGContext() != null) {
            suiteName = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
            outputDirectory = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getOutputDirectory();
        }

        this.pageIdentifierElement = pageIdentifierElement;
        driver = WebUIDriver.getWebDriver();

        if (url != null) {
            open(url);
            ((CustomEventFiringWebDriver)driver).updateWindowsHandles();
        }

        // Wait for page load is applicable only for web test
        // When running tests on an iframe embedded site then test will fail if this command is not used
        // in case of mobile application, only capture screenshot
        if (SeleniumTestsContextManager.isWebTest()) {
            waitForPageToLoad();
        } else if (SeleniumTestsContextManager.isMobileAppTest()) {
        	capturePageSnapshot();
        }

        assertCurrentPage(false);

        try {
            this.windowHandle = driver.getWindowHandle();
        } catch (Exception ex) {
            // Ignore for OperaDriver
        }

        SeleniumTestsPageListener.informPageLoad(this);

        Calendar end = Calendar.getInstance();
        start.setTime(new Date());

        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        if ((endTime - startTime) / 1000 > 0) {
            TestLogging.log("Open web page in :" + (endTime - startTime) / 1000 + "seconds");
        }
    }

    public void assertCookiePresent(final String name) {
        TestLogging.logWebStep(null, "assert cookie " + name + " is present.", false);
        assertHTML(getCookieByName(name) != null, "Cookie: {" + name + "} not found.");
    }

    @Override
    protected void assertCurrentPage(final boolean log) throws NotCurrentPageException {

        if (pageIdentifierElement != null && !isElementPresent(pageIdentifierElement.getBy())) {
            try {
                if (!SeleniumTestsContextManager.getThreadContext().getCaptureSnapshot()) {
                    new ScreenshotUtil(driver).capturePageSnapshotOnException();
                }
            } catch (Exception e) {
            	logger.error(e);
            }

            throw new NotCurrentPageException(getClass().getCanonicalName()
                    + " is not the current page.\nPageIdentifierElement " + pageIdentifierElement.toString()
                    + " is not found.");
        }

        if (log) {
            TestLogging.logWebStep(null,
                "assert \"" + getClass().getSimpleName() + "\" is the current page"
                    + (pageIdentifierElement != null
                        ? " (assert PageIdentifierElement " + pageIdentifierElement.toHTML() + " is present)." : "."),
                false);
        }
    }
    
    /**
     * Get parameter from configuration
     * 
     * @param key
     * 
     * @return String
     */
    public static String param(String key) {
    	String value = SeleniumTestsContextManager.getThreadContext().getConfiguration().get(key);
    	if (value == null) {
    		TestLogging.errorLogger(String.format("Variable %s is not defined", key));
    		return "";
    	}
    	return value;
    }

    public void assertHtmlSource(final String text) {
        TestLogging.logWebStep(null, "assert text \"" + text + "\" is present in page source.", false);
        assertHTML(getHtmlSource().contains(text), "Text: {" + text + "} not found on page source.");
    }

    public void assertKeywordNotPresent(final String text) {
        TestLogging.logWebStep(null, "assert text \"" + text + "\" is present in page source.", false);
        Assert.assertFalse(getHtmlSource().contains(text), "Text: {" + text + "} not found on page source.");
    }

    public void assertLocation(final String urlPattern) {
        TestLogging.logWebStep(null, "assert location \"" + urlPattern + "\".", false);
        assertHTML(getLocation().contains(urlPattern), "Pattern: {" + urlPattern + "} not found on page location.");
    }

    public void assertPageSectionPresent(final WebPageSection pageSection) {
        TestLogging.logWebStep(null, "assert pagesection \"" + pageSection.getName() + "\"  is present.", false);
        assertElementPresent(new HtmlElement(pageSection.getName(), pageSection.getBy()));
    }

    public void assertTitle(final String text) {
        TestLogging.logWebStep(null, "assert text \"" + text + "\"  is present on title.", false);
        assertHTML(getTitle().contains(text), "Text: {" + text + "} not found on page title.");

    }

    @Override
    public void capturePageSnapshot() {
        ScreenShot screenShot = new ScreenshotUtil(driver).captureWebPageSnapshot();
        this.title = screenShot.getTitle();

        if (screenShot.getHtmlSourcePath() != null) {
            htmlFilePath = screenShot.getHtmlSourcePath().replace(suiteName, outputDirectory);
            htmlSavedToPath = screenShot.getHtmlSourcePath();
        }

        if (screenShot.getImagePath() != null) {
            imageFilePath = screenShot.getImagePath().replace(suiteName, outputDirectory);
        }

        TestLogging.logWebOutput(url, title + " (" + TestLogging.buildScreenshotLog(screenShot) + ")", false);

    }

    /**
     * Close a PageObject
     * 
     * @throws NotCurrentPageException
     */
    public final void close() throws NotCurrentPageException { 
    	
        if (WebUIDriver.getWebDriver() == null) {
            return;
        }

        SeleniumTestsPageListener.informPageUnload(this);
        TestLogging.logWebOutput(url, title +" close web page", false);

        boolean isMultipleWindow = false;
        if (driver.getWindowHandles().size() > 1) {
            isMultipleWindow = true;
        }
        
        try {
            driver.close();
        } catch (WebDriverException ignore) { 
        	logger.info("Error closing driver: " + ignore.getMessage());
        }

        if ("LOCAL".equalsIgnoreCase(WebUIDriver.getWebUIDriver().getMode())) {
        	WaitHelper.waitForSeconds(2);
        }

        try {
            if (isMultipleWindow) {
                this.selectMainWindow();
            } else {
                WebUIDriver.setWebDriver(null);
            }
        } catch (UnreachableBrowserException ex) {
            WebUIDriver.setWebDriver(null);

        }
        

    }

    /**
     * Drags an element a certain distance and then drops it.
     *
     * @param  element  to dragAndDrop
     * @param  offsetX  in pixels from the current location to which the element should be moved, e.g., 70
     * @param  offsetY  in pixels from the current location to which the element should be moved, e.g., -300
     */
    public void dragAndDrop(final HtmlElement element, final int offsetX, final int offsetY) {
        TestLogging.logWebStep(null,
            "dragAndDrop " + element.toHTML() + " to offset(x,y): (" + offsetX + "," + offsetY + ")", false);
        captureSnapshot("before draging");

        new Actions(driver).dragAndDropBy((WebElement) element.getElement(), offsetX, offsetY).perform();
        captureSnapshot("after dropping");
    }

    @Override
    public String getBodyText() {
        return bodyText;
    }

    public final String getCookieByName(final String name) {
        if (driver.manage().getCookieNamed(name) == null) {
            return null;
        }

        return driver.manage().getCookieNamed(name).getValue();
    }

    public final int getElementCount(final HtmlElement element) throws CustomSeleniumTestsException {
        return driver.findElements(element.getBy()).size();
    }

    public String getEval(final String expression) {
        CustomAssertion.assertTrue(false, "focus not implemented yet for " + expression);
        return null;
    }

    public String getHtmlFilePath() {
        return htmlFilePath;
    }

    @Override
    public String getHtmlSavedToPath() {
        return htmlSavedToPath;
    }

    @Override
    public String getHtmlSource() {
        return htmlSource;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    /**
     * Get JS Error by JSErrorCollector which only supports Firefox browser.
     *
     * @return  jsErrors in format "line number, errorLogger message, source name; "
     */
    @Override
    public String getJSErrors() {
        if (WebUIDriver.getWebUIDriver().isAddJSErrorCollectorExtension()) {
            List<JavaScriptError> jsErrorList = JavaScriptError.readErrors(driver);
            if (!jsErrorList.isEmpty()) {
                String jsErrors = "";
                for (JavaScriptError aJsErrorList : jsErrorList) {
                    jsErrors += aJsErrorList.getLineNumber() + ", " + aJsErrorList.getErrorMessage() + ", "
                            + aJsErrorList.getSourceName() + "; ";
                }

                return jsErrors;
            }
        }

        return null;
    }

    @Override
    public String getLocation() {
        return driver.getCurrentUrl();
    }

    public String getPopupWindowName() {
        return popupWindowName;
    }

    public int getTimeout() {
        return SeleniumTestsContextManager.getThreadContext().getWebSessionTimeout();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    public String getUrl() {
        return url;
    }

    public String getCanonicalURL() {
        return new LinkElement("Canonical URL", By.cssSelector("link[rel=canonical]")).getAttribute("href");
    }

    public String getWindowHandle() {
        return windowHandle;
    }

    public final void goBack() {
        TestLogging.logWebStep(null, "goBack", false);
        driver.navigate().back();
        frameFlag = false;
    }

    public final void goForward() {
        TestLogging.logWebStep(null, "goForward", false);
        driver.navigate().forward();
        frameFlag = false;
    }

    public final boolean isCookiePresent(final String name) {
        return getCookieByName(name) != null;
    }

    @Override
    public boolean isFrame() {
        return frameFlag;
    }

    public final void maximizeWindow() {
        new WebUtility(driver).maximizeWindow();
    }

    private void open(final String url) throws IOException {

        if (this.getDriver() == null) {
            TestLogging.logWebStep(url, "Launch application", false);
            driver = webUXDriver.createWebDriver();
        }

        setUrl(url);
        try {

            // Navigate to app URL for browser test
            if (SeleniumTestsContextManager.isWebTest()) {
            	maximizeWindow();
                driver.navigate().to(url);
            }
        } catch (UnreachableBrowserException e) {

            // handle if the last window is closed
            TestLogging.logWebStep(url, "Launch application", false);
            driver = webUXDriver.createWebDriver();
            maximizeWindow();
            driver.navigate().to(url);
        } catch (UnsupportedCommandException e) {
            TestLogging.log("get UnsupportedCommandException, retry");
            driver = webUXDriver.createWebDriver();
            maximizeWindow();
            driver.navigate().to(url);
        } catch (org.openqa.selenium.TimeoutException ex) {
            TestLogging.log("got time out when loading " + url + ", ignored");
        } catch (org.openqa.selenium.UnhandledAlertException ex) {
            TestLogging.log("got UnhandledAlertException, retry");
            driver.navigate().to(url);
        } catch (WebDriverException e) {
        	logger.error(e);
            throw new CustomSeleniumTestsException(e);
        }
    }

    private void populateAndCapturePageSnapshot() {
        try {
            setTitle(driver.getTitle());
            htmlSource = driver.getPageSource();
            bodyText = new HtmlElement("Body", By.tagName("body")).getText();
        } catch (UnreachableBrowserException e) { 
            throw new WebDriverException(e);
        } catch (WebDriverException e) {
            throw e;
        }

        capturePageSnapshot();
    }

    public final void refresh() throws NotCurrentPageException {
        TestLogging.logWebStep(null, "refresh", false);
        try {
            driver.navigate().refresh();
        } catch (org.openqa.selenium.TimeoutException ex) {
            TestLogging.log("got time out customexception, ignore");
        }
    }

    public final void resizeTo(final int width, final int height) {
        new WebUtility(driver).resizeWindow(width, height);
    }

    public final void selectFrame(final int index) {
        TestLogging.logWebStep(null, "select frame using index" + index, false);
        driver.switchTo().frame(index);
        frameFlag = true;
    }

    @Override
    public final void selectFrame(final By by) {
        TestLogging.logWebStep(null, "select frame, locator={\"" + by.toString() + "\"}", false);
        driver.switchTo().frame(driver.findElement(by));
        frameFlag = true;
    }

    public final void selectFrame(final String locator) {
        TestLogging.logWebStep(null, "select frame, locator={\"" + locator + "\"}", false);
        driver.switchTo().frame(locator);
        frameFlag = true;
    }

    public final void selectMainWindow() throws NotCurrentPageException {
        TestLogging.logWebStep(null, "select window, locator={\"" + getPopupWindowName() + "\"}", false);

        driver.switchTo().window((String) driver.getWindowHandles().toArray()[0]);
        WaitHelper.waitForSeconds(1);

        // Check whether it's the expected page.
        assertCurrentPage(true);
    }

    public final void selectWindow(final int index) throws NotCurrentPageException {
        TestLogging.logWebStep(null, "select window, locator={\"" + index + "\"}", false);
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[index]);
    }
    
    public final String selectNewWindow() throws NotCurrentPageException {
    	return selectNewWindow(6000);
    }
    
    public final String selectNewWindow(int waitMs) throws NotCurrentPageException {
        TestLogging.logWebStep(null, "select new window", false);
        
        // Keep the name of the current window handle before switching
        // sometimes, our action made window disappear
 		String mainWindowHandle;
 		try {
 			mainWindowHandle = driver.getWindowHandle();
 		} catch (Exception e) {
 			mainWindowHandle = "";
 		}

 		// wait for window to be displayed
 		long end = systemClock.laterBy(waitMs + 250L);
 		Set<String> handles = new TreeSet<>();
 		boolean found = false;
 		
 		while (systemClock.isNowBefore(end) && !found) {
 			
 			handles = driver.getWindowHandles();

 			for (String handle: handles) {
 				
 				// we already know this handle
 				if (getCurrentHandles().contains(handle)) {
 					continue;
 				} 
 				
				selectWindow(handle);
				
				// wait for a valid address
				String address = "";
				long endLoad = systemClock.laterBy(5000);
				while (address.isEmpty() && systemClock.isNowBefore(endLoad)) {
					address = driver.getCurrentUrl();
				}
				
				// make window display in foreground
				try {
					Point windowPosition  = driver.manage().window().getPosition();
					Mouse mouse = new DesktopMouse();
					mouse.click(new DesktopScreenRegion(Math.max(0, windowPosition.x) + driver.manage().window().getSize().width / 2, Math.max(0, windowPosition.y) + 5, 2, 2).getCenter());
				} catch (Exception e) {
					logger.warn("error while giving focus to window");
				}
				
				found = true;
				break;
 			}
 			WaitHelper.waitForMilliSeconds(300);
 		}
 		
 		// check window has changed
 		if (waitMs > 0 && mainWindowHandle.equals(driver.getWindowHandle())) {
 			throw new CustomSeleniumTestsException("new window has not been found. Handles: " + handles);
 		}
 		return mainWindowHandle;
        
    }

    protected void setHtmlSavedToPath(final String htmlSavedToPath) {
        this.htmlSavedToPath = htmlSavedToPath;
    }

    protected void setTitle(final String title) {
        this.title = title;
    }

    protected void setUrl(final String openUrl) {
        this.url = openUrl;
    }

    public void switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
        } catch (UnhandledAlertException e) {
        	logger.warn("Alert found, you should handle it");
        }
    }

    private void waitForPageToLoad() {
        new WebDriverWait(driver, 2).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"complete\") { return \"ok\"; }"));

        // populate page info
        try {
            populateAndCapturePageSnapshot();
        } catch (Exception ex) {
        	logger.error(ex);
            throw ex;
        }
    }
    
    /**
     * Captures snapshot of the current browser window.
     */
    public void captureSnapshot() {
        captureSnapshot(ContextHelper.getCallerMethod() + " on ");
    }

    /**
     * Captures snapshot of the current browser window, and prefix the file name with the assigned string.
     *
     * @param  messagePrefix
     */
    protected void captureSnapshot(final String messagePrefix) {
        ScreenshotUtil.captureSnapshot(messagePrefix);
    }

    public WebElement getElement(final By by, final String elementName) {
        WebElement element = null;
        try {
            element = driver.findElement(by);
        } catch (ElementNotFoundException e) {
            TestLogging.errorLogger(elementName + " is not found with locator - " + by.toString());
            throw e;
        }

        return element;
    }

    public String getElementUrl(final By by, final String name) {
        return getElement(by, name).getAttribute("href");
    }

    public String getElementText(final By by, final String name) {
        return getElement(by, name).getText();
    }

    public String getElementSrc(final By by, final String name) {
        return getElement(by, name).getAttribute("src");
    }
}

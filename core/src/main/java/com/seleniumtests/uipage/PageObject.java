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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.SystemClock;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.SeleniumTestsPageListener;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.NotCurrentPageException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class PageObject extends BasePage implements IPage {

	private static final Logger logger = SeleniumRobotLogger.getLogger(PageObject.class);
    private boolean frameFlag = false;
    private HtmlElement pageIdentifierElement = null;
    private String popupWindowName = null;
    private String title = null;
    private String url = null;
    private String htmlSource = null;
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

        // open page
        openPage(url);

        assertCurrentPage(false);

        SeleniumTestsPageListener.informPageLoad(this);

        Calendar end = Calendar.getInstance();
        start.setTime(new Date());

        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        if ((endTime - startTime) / 1000 > 0) {
            TestLogging.log("Open web page in :" + (endTime - startTime) / 1000 + "seconds");
        }
    }

    protected void setTitle(final String title) {
        this.title = title;
    }

    protected void setUrl(final String openUrl) {
        this.url = openUrl;
    }
    
    public String getHtmlFilePath() {
        return htmlFilePath;
    }

    @Override
    public String getHtmlSource() {
        return htmlSource;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    @Override
    public String getLocation() {
        return driver.getCurrentUrl();
    }

    public String getPopupWindowName() {
        return popupWindowName;
    }


    /**
     * Open page 
     * Wait for page loading
     * @param url
     * @throws IOException
     */
    private void openPage(String url) throws IOException {
    	if (url != null) {
            open(url);
            ((CustomEventFiringWebDriver)driver).updateWindowsHandles();
        }

        // Wait for page load is applicable only for web test
        // When running tests on an iframe embedded site then test will fail if this command is not used
        // in case of mobile application, only capture screenshot
        if (SeleniumTestsContextManager.isWebTest()) {
            waitForPageToLoad();
        } else if (SeleniumTestsContextManager.isAppTest()) {
        	capturePageSnapshot();
        }
    }

    public void assertCookiePresent(final String name) {
        assertHTML(getCookieByName(name) != null, "Cookie: {" + name + "} not found.");
    }

    @Override
    protected void assertCurrentPage(final boolean log) {

        if (pageIdentifierElement != null && !pageIdentifierElement.isElementPresent()) {
            new ScreenshotUtil(driver).captureWebPageSnapshot();

            throw new NotCurrentPageException(getClass().getCanonicalName()
                    + " is not the current page.\nPageIdentifierElement " + pageIdentifierElement.toString()
                    + " is not found.");
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
    		TestLogging.error(String.format("Variable %s is not defined", key));
    		return "";
    	}
    	return value;
    }

    public void assertHtmlSource(final String text) {
        assertHTML(getHtmlSource().contains(text), "Text: {" + text + "} not found on page source.");
    }

    public void assertKeywordNotPresent(final String text) {
        Assert.assertFalse(getHtmlSource().contains(text), "Text: {" + text + "} not found on page source.");
    }

    public void assertLocation(final String urlPattern) {
        assertHTML(getLocation().contains(urlPattern), "Pattern: {" + urlPattern + "} not found on page location.");
    }

    public void assertTitle(final String text) {
        assertHTML(getTitle().contains(text), "Text: {" + text + "} not found on page title.");

    }

    @Override
    public void capturePageSnapshot() {
        ScreenShot screenShot = new ScreenshotUtil(driver).captureWebPageSnapshot();
        this.title = screenShot.getTitle();

        if (screenShot.getHtmlSourcePath() != null) {
            htmlFilePath = screenShot.getHtmlSourcePath().replace(suiteName, outputDirectory);
            htmlSource = screenShot.getHtmlSource();
        }

        if (screenShot.getImagePath() != null) {
            imageFilePath = screenShot.getImagePath().replace(suiteName, outputDirectory);
        }

        TestLogging.logScreenshot(screenShot);

    }

    /**
     * Close a PageObject
     * 
     * @throws NotCurrentPageException
     */
    public final void close() { 
    	
        if (WebUIDriver.getWebDriver() == null) {
            return;
        }

        SeleniumTestsPageListener.informPageUnload(this);
        TestLogging.info(title +" close web page");

        boolean isMultipleWindow = false;
        if (driver.getWindowHandles().size() > 1) {
            isMultipleWindow = true;
        }
        
        try {
            driver.close();
        } catch (WebDriverException ignore) { 
        	logger.info("Error closing driver: " + ignore.getMessage());
        }

        if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
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
        new Actions(driver).dragAndDropBy((WebElement) element.getElement(), offsetX, offsetY).perform();
    }

    public final String getCookieByName(final String name) {
        if (driver.manage().getCookieNamed(name) == null) {
            return null;
        }

        return driver.manage().getCookieNamed(name).getValue();
    }

    public final int getElementCount(final HtmlElement element) {
        return driver.findElements(element.getBy()).size();
    }

    public String getEval(final String expression) {
        Assert.assertTrue(false, "focus not implemented yet for " + expression);
        return null;
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

    public final void goBack() {
        driver.navigate().back();
        frameFlag = false;
    }

    public final void goForward() {
        driver.navigate().forward();
        frameFlag = false;
    }

    public final boolean isCookiePresent(final String name) {
        return getCookieByName(name) != null;
    }

    public boolean isFrame() {
        return frameFlag;
    }

    public final void maximizeWindow() {
        try {
        	// app test are not compatible with window
        	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP || SeleniumTestsContextManager.getThreadContext().getBrowser() == BrowserType.BROWSER) {
                return;
            }

            driver.manage().window().maximize();
        } catch (Exception ex) {

            try {
                ((JavascriptExecutor) driver).executeScript(
                    "if (window.screen){window.moveTo(0, 0);window.resizeTo(window.screen.availWidth,window.screen.availHeight);}");
            } catch (Exception ignore) {
                TestLogging.log("Unable to maximize browser window. Exception occured: " + ignore.getMessage());
            }
        }
    }
    
    /**
     * On init set window to size requested by user. Window is maximized if no size is set
     */
    public final void setWindowToRequestedSize() {
    	if (!SeleniumTestsContextManager.isWebTest()) {
    		return;
    	}
    	
    	Integer width = SeleniumTestsContextManager.getThreadContext().getViewPortWidth();
    	Integer height = SeleniumTestsContextManager.getThreadContext().getViewPortHeight();
    	
    	if (width == null || height == null) {
    		maximizeWindow();
    	} else {
    		resizeTo(width, height);
    	}
    }

    private void open(final String url) {

        if (this.getDriver() == null) {
            driver = webUXDriver.createWebDriver();
        }

        setUrl(url);
        try {

            // Navigate to app URL for browser test
            if (SeleniumTestsContextManager.isWebTest()) {
            	setWindowToRequestedSize();
                driver.navigate().to(url);
            }
        } catch (UnreachableBrowserException e) {

            driver = webUXDriver.createWebDriver();
            if (SeleniumTestsContextManager.isWebTest()) {
	            setWindowToRequestedSize();
	            driver.navigate().to(url);
            }
        } catch (UnsupportedCommandException e) {
            TestLogging.log("get UnsupportedCommandException, retry");
            driver = webUXDriver.createWebDriver();
            if (SeleniumTestsContextManager.isWebTest()) {
            	setWindowToRequestedSize();
	            driver.navigate().to(url);
            }
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

    public final void refresh()  {
        try {
            driver.navigate().refresh();
        } catch (org.openqa.selenium.TimeoutException ex) {
            TestLogging.log("got time out customexception, ignore");
        }
    }

    /**
     * Resize window to given dimensions.
     *
     * @param  width
     * @param  height
     */
    public final void resizeTo(final int width, final int height) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            return;
        }
    	
        try {
            Dimension setSize = new Dimension(width, height);
            driver.manage().window().setPosition(new Point(0, 0));
            int retries = 5;
            
            for (int i=0; i < retries; i++) {
            	driver.manage().window().setSize(setSize);
            	Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
            	
            	if (viewPortSize.height == height && viewPortSize.width == width) {
            		break;
            	} else {
            		setSize = new Dimension(2 * width - viewPortSize.width, 2 * height - viewPortSize.height);
            	}
            }
            
        } catch (Exception ex) {
        	logger.error(ex);
        }
    }

    public final void selectFrame(final Integer index) {
        driver.switchTo().frame(index);
        frameFlag = true;
    }

    public final void selectFrame(final By by) {
    	WebElement element = driver.findElement(by);
        driver.switchTo().frame(element);
        frameFlag = true;
    }

    public final void selectFrame(final String locator) {
        driver.switchTo().frame(locator);
        frameFlag = true;
    }
    
    public final void exitFrame() {
    	driver.switchTo().defaultContent();
    	frameFlag = false;
    }

    public final void selectMainWindow() {
    	selectWindow(0);

        // Check whether it's the expected page.
        assertCurrentPage(true);
    }

    public final void selectWindow(final int index) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            throw new ScenarioException("Application are not compatible with Windows");
        }
    	    
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[index]);
        WaitHelper.waitForSeconds(1);
    }
    
    public final String selectNewWindow() {
    	return selectNewWindow(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
    }
    
    public final String selectNewWindow(int waitMs) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            throw new ScenarioException("Application are not compatible with Windows");
        }
    	        
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
				// TODO: reactivate feature
				try {
//					Point windowPosition  = driver.manage().window().getPosition();
//					org.openqa.selenium.interactions.Mouse mouse = ((HasInputDevices) driver).getMouse();
//					mouse.click();
//					Mouse mouse = new DesktopMouse();
//					mouse.click(new DesktopScreenRegion(Math.max(0, windowPosition.x) + driver.manage().window().getSize().width / 2, Math.max(0, windowPosition.y) + 5, 2, 2).getCenter());
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

    public void switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
        } catch (UnhandledAlertException e) {
        	logger.warn("Alert found, you should handle it");
        }
    }

    private void waitForPageToLoad() {
    	try {
    		new WebDriverWait(driver, 5).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"complete\") { return \"ok\"; }"));
    	} catch (TimeoutException e) {
    		// nothing
    	}
    	

        // populate page info
        try {
        	capturePageSnapshot();
        } catch (Exception ex) {
        	logger.error(ex);
            throw ex;
        }
    }
    
	public Alert waitForAlert(int waitInSeconds) {
		long end = systemClock.laterBy(waitInSeconds * 1000);

		while (systemClock.isNowBefore(end)) {
			return driver.switchTo().alert();
		}
		return null;
	}
    
    /**
     * Method to handle file upload through robot class
     * /!\ This should only be used as the last option when uploading file cannot be done an other way
     * https://saucelabs.com/resources/articles/best-practices-tips-selenium-file-upload
     * <code>
     * driver.setFileDetector(new LocalFileDetector());
     * driver.get("http://sso.dev.saucelabs.com/test/guinea-file-upload");
     *   WebElement upload = driver.findElement(By.id("myfile"));
     *   upload.sendKeys("/Users/sso/the/local/path/to/darkbulb.jpg");
     *   </code> 
     * @param filePath
     */
	public void uploadFile(String filePath) {
		try {
			byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(new File(filePath)));
			((JavascriptExecutor) driver).executeScript(CustomEventFiringWebDriver.NON_JS_UPLOAD_FILE_THROUGH_POPUP, new File(filePath).getName(), new String(encoded));
			
			// Upload fichier de X ko
			Alert alert = waitForAlert(5);
			if (alert != null) {
				driver.switchTo().alert().accept();
			}
			WaitHelper.waitForSeconds(2);
			
		} catch (IOException e) {
			throw new ScenarioException(String.format("could not read file to upload %s: %s", filePath, e.getMessage()));
		}
	}
}

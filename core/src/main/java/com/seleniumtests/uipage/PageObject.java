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
package com.seleniumtests.uipage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.PageLoadStrategy;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestTasks;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import com.seleniumtests.customexception.NotCurrentPageException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.ScreenshotUtil.Target;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.ScenarioLogger;

public class PageObject extends BasePage implements IPage {

    private boolean frameFlag = false;
    private HtmlElement pageIdentifierElement = null;
    private String windowHandle = null; // store the window / tab on which this page is loaded
    private String url = null;
    private String suiteName = null;
    private String outputDirectory = null;
    private String htmlFilePath = null;
    private String imageFilePath = null;
    private Clock systemClock;

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
    
    public PageObject(final HtmlElement pageIdentifierElement, final String url) throws IOException {
    	this(pageIdentifierElement, 
    			url, 
    			SeleniumTestsContextManager.getThreadContext().getBrowser(), 
    			WebUIDriver.getCurrentWebUiDriverName(), 
    			null);
    }

    /**
     * Base Constructor.
     * Represents a page on our web site or mobile application.
     *
     * @param	pageIdentifierElement	The element to search for so that we check we are on the right page. 
     * 									May be null if we do not want to check we are on the page
     * @param   url						the URL to which we should connect. May be null if we do not want to go to a specific URL
     * @param	browserType				the new browser type to create
     * @param	driverName				a logical name to give to the created driver
     * @param	attachExistingDriverPort 	 if we need to attach to an existing browser instead of creating one, then specify the port here
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(HtmlElement pageIdentifierElement, String url, BrowserType browserType, String driverName, Integer attachExistingDriverPort) throws IOException {

    	systemClock = Clock.systemUTC();
        Calendar start = Calendar.getInstance();
        start.setTime(new Date());

        if (SeleniumTestsContextManager.getGlobalContext() != null
                && SeleniumTestsContextManager.getGlobalContext().getTestNGContext() != null) {
            suiteName = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
            outputDirectory = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getOutputDirectory();
        }

        this.pageIdentifierElement = pageIdentifierElement;
        
        // creates the driver and switch to it. It may be done twice as when the driver is created, we automatically switch to it, but in cas driver
        // is already created, 
        driver = WebUIDriver.getWebDriver(true, browserType, driverName, attachExistingDriverPort);
        
        if (driver == null && url != null) {
        	throw new ConfigurationException("driver is null, 'browser' configuration may be empty");
        }
        
        // open page
        openPage(url);
        
        // in case browser has been created outside of selenium and we attach to it, get initial window handles
        if (driver != null && attachExistingDriverPort != null && url == null) {
        	((CustomEventFiringWebDriver)driver).updateWindowsHandles();
        }

        assertCurrentPage(false);

        Calendar end = Calendar.getInstance();
        start.setTime(new Date());

        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        if ((endTime - startTime) / 1000 > 0) {
            ((ScenarioLogger)logger).log("Open web page in :" + (endTime - startTime) / 1000 + "seconds");
        }
        
        
    }

    protected void setUrl(final String openUrl) {
        this.url = openUrl;
    }
    
    public String getHtmlFilePath() {
        return htmlFilePath;
    }

    @Override
    public String getHtmlSource() {
    	return driver.getPageSource();
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    @Override
    public String getLocation() {
        return driver.getCurrentUrl();
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
    	return TestTasks.param(key);
    }
    
    /**
     * returns the robot configuration
     * @return
     */
    public SeleniumTestsContext robotConfig() {
    	return SeleniumTestsContextManager.getThreadContext();
    }
    
    /**
     * Add step inside a page
     * @param stepName
     * @param passwordsToMask	array of strings that must be replaced by '*****' in reports
     */
    public void addStep(String stepName) {
    	TestTasks.addStep(stepName);
    }
    public void addStep(String stepName, String ... passwordToMask) {
    	TestTasks.addStep(stepName, passwordToMask);
    }
    
    /**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * @param key
     * @param value
     */
    public void createOrUpdateParam(String key, String value) {
    	TestTasks.createOrUpdateParam(key, value);
    }
    
    /**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * @param key					name of the param
     * @param value				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     */
    public void createOrUpdateParam(String key, String value, boolean specificToVersion) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion);
    }
    
    /**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param newValue				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     * @param timeToLive			if > 0, this variable will be destroyed after some days (defined by variable)
     * @param reservable			if true, this variable will be set as reservable in variable server. This means it can be used by only one test at the same time
     */
    public void createOrUpdateParam(String key, String value, boolean specificToVersion, int timeToLive, boolean reservable) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion, timeToLive, reservable);
    }
    
    /**
     * In case the scenario uses several drivers, switch to one or another using this method, so that any new calls will go through this driver
     * @param driverName
     */
    public WebDriver switchToDriver(String driverName) {
    	driver = TestTasks.switchToDriver(driverName);
    	return driver;
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
        capturePageSnapshot(null);

    }
    
    public void capturePageSnapshot(String snapshotName) {
    	ScreenShot screenShot = new ScreenshotUtil().capture(Target.PAGE, ScreenShot.class);

        if (screenShot.getHtmlSourcePath() != null) {
            htmlFilePath = screenShot.getHtmlSourcePath().replace(suiteName, outputDirectory);
        }

        if (screenShot.getImagePath() != null) {
            imageFilePath = screenShot.getImagePath().replace(suiteName, outputDirectory);
        }
        if (snapshotName != null) {
        	screenShot.setTitle(snapshotName);
        }

        ((ScenarioLogger)logger).logScreenshot(screenShot, snapshotName);
        
        // store the window / tab on which this page is loaded
        windowHandle = driver.getWindowHandle();
    }
    
    /**
     * Get focus on this page, using the handle we stored when creating it
     * When called, you should write {@code myPage.<MyPageClassName>getFocus().someMethodOfMyPage();}
     * @return
     */
    @SuppressWarnings("unchecked")
	public <T extends PageObject> T getFocus() {
    	selectWindow(windowHandle);
    	return (T)this;
    }

    /**
     * Close a PageObject. This method can be called when a web session opens several pages and one of them has to be closed
     * In case there are multiple windows opened, switch back to the previous window in the list
     * 
     * @throws NotCurrentPageException
     */
    public final void close() { 
    	
        if (WebUIDriver.getWebDriver(false) == null) {
            return;
        }

        boolean isMultipleWindow = false;
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (handles.size() > 1) {
            isMultipleWindow = true;
        }
        internalLogger.debug("Current handles: " + handles);
        
        try {
        	logger.info("close web page: " + getTitle());
            driver.close();
        } catch (WebDriverException ignore) { 
        	internalLogger.info("Error closing driver: " + ignore.getMessage());
        }

        // wait a bit before going back to main window
        WaitHelper.waitForSeconds(2);

        try {
            if (isMultipleWindow) {
            	try { 
            		selectWindow(handles.get(handles.indexOf(windowHandle) - 1));
            	} catch (IndexOutOfBoundsException | NoSuchWindowException e) {
            		selectMainWindow();
            	}
            } else {
                WebUIDriver.setWebDriver(null);
            }
        } catch (UnreachableBrowserException ex) {
            WebUIDriver.setWebDriver(null);

        }
    }
    
    /**
     * Close the current tab / window which leads to the previous window / tab in the list.
     * This uses the default constructor which MUST be available
     * @param previousPage		the page we go back to, so that we can check we are on the right page
     * @return
     */
    public <T extends PageObject> T close(Class<T> previousPage) {
    	close();
    	try {
			return previousPage.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new ScenarioException("Cannot check for previous page: " + e.getMessage(), e);
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
            	((ScenarioLogger)logger).log("Unable to maximize browser window. Exception occured: " + ignore.getMessage());
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

        setUrl(url);
        try {

            // Navigate to app URL for browser test
            if (SeleniumTestsContextManager.isWebTest()) {
            	setWindowToRequestedSize();
                driver.navigate().to(url);
            }
        } catch (UnreachableBrowserException e) {
        	// recreate the driver without recreating the enclosing WebUiDriver
        	driver = WebUIDriver.getWebUIDriver(false).createWebDriver();
            if (SeleniumTestsContextManager.isWebTest()) {
	            setWindowToRequestedSize();
	            driver.navigate().to(url);
            }
        } catch (UnsupportedCommandException e) {
        	logger.error("get UnsupportedCommandException, retry");
            // recreate the driver without recreating the enclosing WebUiDriver
            driver = WebUIDriver.getWebUIDriver(false).createWebDriver();
            if (SeleniumTestsContextManager.isWebTest()) {
            	setWindowToRequestedSize();
	            driver.navigate().to(url);
            }
        } catch (org.openqa.selenium.TimeoutException ex) {
        	logger.error("got time out when loading " + url + ", ignored");
        } catch (org.openqa.selenium.UnhandledAlertException ex) {
        	logger.error("got UnhandledAlertException, retry");
            driver.navigate().to(url);
        } catch (WebDriverException e) {
        	internalLogger.error(e);
            throw new CustomSeleniumTestsException(e);
        }
    }

    public final void refresh()  {
        try {
            driver.navigate().refresh();
        } catch (org.openqa.selenium.TimeoutException ex) {
        	logger.error("got time out customexception, ignore");
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
        	internalLogger.error(ex);
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
    }

    public final void selectWindow(final int index) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.getThreadContext().getTestType().family() == TestType.APP) {
            throw new ScenarioException("Application are not compatible with Windows");
        }
    	    
        driver.switchTo().window((String) driver.getWindowHandles().toArray()[index]);
        WaitHelper.waitForSeconds(1);
    }
    
    /**
     * Selects the first unknown window. To use we an action creates a new window or tab
     * @return
     */
    public final String selectNewWindow() {
    	return selectNewWindow(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
    }
    
    /**
     * Selects the first unknown window. To use we an action creates a new window or tab
     * @param waitMs	wait for N milliseconds before raising error
     * @return
     */
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
 		internalLogger.debug("Current handle: " + mainWindowHandle);

 		// wait for window to be displayed
 		Instant end = systemClock.instant().plusMillis(waitMs + 250L);
 		Set<String> handles = new TreeSet<>();
 		boolean found = false;
 		
 		while (end.isAfter(systemClock.instant()) && !found) {
 			
 			handles = driver.getWindowHandles();
 			internalLogger.debug("All handles: " + handles.toString());

 			for (String handle: handles) {
 				
 				// we already know this handle
 				if (getCurrentHandles().contains(handle)) {
 					continue;
 				} 
 				
				selectWindow(handle);
				
				// wait for a valid address
				String address = "";
				Instant endLoad = systemClock.instant().plusMillis(5000);
				while (address.isEmpty() && endLoad.isAfter(systemClock.instant())) {
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
					internalLogger.warn("error while giving focus to window");
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

    /**
     * Switch to the default content
     */
    public void switchToDefaultContent() {
        try {
            driver.switchTo().defaultContent();
        } catch (UnhandledAlertException e) {
        	logger.warn("Alert found, you should handle it");
        }
    }

    private void waitForPageToLoad() {
    	try {
    		if (robotConfig().getPageLoadStrategy() == PageLoadStrategy.NORMAL) {
    			new WebDriverWait(driver, 5).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"complete\") { return \"ok\"; }"));
    		} else if (robotConfig().getPageLoadStrategy() == PageLoadStrategy.EAGER) {
    			new WebDriverWait(driver, 5).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"interactive\") { return \"ok\"; }"));
    		}
    	} catch (TimeoutException e) {
    		// nothing
    	}
    	

        // populate page info
        try {
        	capturePageSnapshot();
        } catch (Exception ex) {
        	internalLogger.error(ex);
            throw ex;
        }
    }
    
	public Alert waitForAlert(int waitInSeconds) {
		Instant end = systemClock.instant().plusSeconds(waitInSeconds);

		while (end.isAfter(systemClock.instant())) {
			try {
				return driver.switchTo().alert();
			} catch (NoAlertPresentException e) {
				WaitHelper.waitForSeconds(1);
			} catch (NoSuchWindowException e) {
				return null;
			}
		}
		return null;
	}
    
    /**
     * Method to handle file upload through robot class
     * /!\ This should only be used as the last option when uploading file cannot be done an other way as explained below
     * https://saucelabs.com/resources/articles/best-practices-tips-selenium-file-upload
     * <code>
     * driver.setFileDetector(new LocalFileDetector());
     * driver.get("http://sso.dev.saucelabs.com/test/guinea-file-upload");
     *   WebElement upload = driver.findElement(By.id("myfile"));
     *   upload.sendKeys("/Users/sso/the/local/path/to/darkbulb.jpg");
     *   </code> 
     *   
     *   
     * To use this method, first click on the upload file button / link, then call this method.
     * 
     * /!\ on firefox, clicking MUST be done through 'clickAction'. 'click()' is not supported by browser. 
     * /!\ on firefox, using the uploadFile method several times without other actions between usage may lead to error. Firefox will never click to the button the second time, probably due to focus problems
     * 
     * @param filePath
     */
	public void uploadFile(String filePath) {
		try {
			byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(new File(filePath)));
			CustomEventFiringWebDriver.uploadFile(new File(filePath).getName(), 
					new String(encoded), 
					SeleniumTestsContextManager.getThreadContext().getRunMode(), 
					SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());
//			((JavascriptExecutor) driver).executeScript(CustomEventFiringWebDriver.NON_JS_UPLOAD_FILE_THROUGH_POPUP, new File(filePath).getName(), new String(encoded));
			
			Alert alert = waitForAlert(5);
			if (alert != null) {
				alert.accept();
			}
			
		} catch (IOException e) {
			throw new ScenarioException(String.format("could not read file to upload %s: %s", filePath, e.getMessage()));
		}
	}
	
	 
	/**
	 * get the name of the PageObject that made the call
	 * 
	 * @param stack : the stacktrace of the caller
	 */
	public static String getCallingPage(StackTraceElement[] stack) {
		String page = null;
		Class<?> stackClass = null;
		
		//find the PageObject Loader
		for(int i=0; i<stack.length;i++){
			try{
				 stackClass = Class.forName(stack[i].getClassName());
			} catch (ClassNotFoundException e){
				continue;
			}
			
			if (PageObject.class.isAssignableFrom(stackClass)){
				page = stack[i].getClassName();	
			}
		}
		return page;
	}
}

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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import com.seleniumtests.uipage.annotations.Context;
import com.seleniumtests.uipage.annotations.ContextNativeApp;
import com.seleniumtests.uipage.annotations.ContextWebView;
import io.appium.java_client.NoSuchContextException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
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
import com.seleniumtests.driver.screenshots.SnapshotCheckType;
import com.seleniumtests.driver.screenshots.SnapshotCheckType.Control;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.htmlelements.CheckBoxElement;
import com.seleniumtests.uipage.htmlelements.Element;
import com.seleniumtests.uipage.htmlelements.GenericPictureElement;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.LinkElement;
import com.seleniumtests.uipage.htmlelements.RadioButtonElement;
import com.seleniumtests.uipage.htmlelements.SelectList;
import com.seleniumtests.uipage.htmlelements.Table;
import com.seleniumtests.util.helper.WaitHelper;

public class PageObject extends BasePage implements IPage {

    private static final String ERROR_ELEMENT_S_IS_NOT_AN_TABLE_ELEMENT = "Element %s is not an Table element";
	private static final String ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS = "Element %s is not an HtmlElement subclass";
	private static final String ERROR_ELEMENT_IS_PRESENT = "Element %s is present";
	private boolean frameFlag = false;
    private String windowHandle = null; // store the window / tab on which this page is loaded
    private String url = null;
    private String suiteName = null;
    private String outputDirectory = null;
    private boolean captureSnapshot = true;
    private ScreenshotUtil screenshotUtil;
    private Clock systemClock;
    private PageLoadStrategy pageLoadStrategy;
    
    private static final String ERROR_ELEMENT_NOT_PRESENT = "Element %s is not present";

    /**
     * Constructor for non-entry point page. The control is supposed to have reached the page from other API call.
     *
     * @throws  Exception
     */
    public PageObject() {
        this(null, (String)null);
    }


    /**
     * Constructor for non-entry point page. The control is supposed to have reached the page from other API call.
     *
     * @param	pageIdentifierElement	The element to search for so that we check we are on the right page. 
     * 									May be null if we do not want to check we are on the page
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement)   {
        this(pageIdentifierElement, (String)null);
    }

    
    /**
     * Base Constructor.
     * Represents a page on our web site or mobile application.
     *
     * @param	pageIdentifierElement	The element to search for so that we check we are on the right page. 
     * 									May be null if we do not want to check we are on the page
     * @param   url						the URL to which we should connect. May be null if we do not want to go to a specific URL
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement, final String url) {
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
     * @param	pageLoadStrategy		whether to wait for the page to load or not (this is complementary to Selenium driver strategy. If not null, it will override selenium
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement, final String url, PageLoadStrategy pageLoadStrategy) {
    	this(pageIdentifierElement, 
    			url, 
    			SeleniumTestsContextManager.getThreadContext().getBrowser(), 
    			WebUIDriver.getCurrentWebUiDriverName(), 
    			null,
    			pageLoadStrategy,
    			true);
    }
    
    /**
     * Base Constructor.
     * Represents a page on our web site or mobile application.
     *
     * @param	pageIdentifierElement	The element to search for so that we check we are on the right page. 
     * 									May be null if we do not want to check we are on the page
     * @param   url						the URL to which we should connect. May be null if we do not want to go to a specific URL
     * @param	pageLoadStrategy		whether to wait for the page to load or not (this is complementary to Selenium driver strategy. If not null, it will override selenium
     * @param	captureSnapshot			if true, snapshot will be captured after page loading. 'false' should only be used when capturing snapshot interfere with a popup alert
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(final HtmlElement pageIdentifierElement, final String url, PageLoadStrategy pageLoadStrategy, boolean captureSnapshot) {
    	this(pageIdentifierElement, 
    			url, 
    			SeleniumTestsContextManager.getThreadContext().getBrowser(), 
    			WebUIDriver.getCurrentWebUiDriverName(), 
    			null,
    			pageLoadStrategy,
    			captureSnapshot);
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
    public PageObject(HtmlElement pageIdentifierElement, String url, BrowserType browserType, String driverName, Integer attachExistingDriverPort) {
    	this(pageIdentifierElement, url, browserType, driverName, attachExistingDriverPort, null, true);
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
     * @param	captureSnapshot			if true, snapshot will be captured after page loading. 'false' should only be used when capturing snapshot interfere with a popup alert
     * @throws IOException 
     *
     * @throws  Exception
     */
    public PageObject(HtmlElement pageIdentifierElement, String url, BrowserType browserType, String driverName, Integer attachExistingDriverPort, boolean captureSnapshot) {
    	this(pageIdentifierElement, url, browserType, driverName, attachExistingDriverPort, null, captureSnapshot);
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
     * @param	pageLoadStrategy		whether to wait for the page to load or not (this is complementary to Selenium driver strategy. If not null, it will override selenium
     * @param	captureSnapshot			if true, snapshot will be captured after page loading. 'false' should only be used when capturing snapshot interfere with a popup alert
     * @throws IOException
     *
     * @throws  Exception
     */
    public PageObject(HtmlElement pageIdentifierElement, String url, BrowserType browserType, String driverName, Integer attachExistingDriverPort, PageLoadStrategy pageLoadStrategy, boolean captureSnapshot)  {
    	
    	systemClock = Clock.systemUTC();
    	this.captureSnapshot = captureSnapshot;
    	
    	if (pageLoadStrategy == null) {
    		this.pageLoadStrategy = robotConfig().getPageLoadStrategy();
    	} else {
    		this.pageLoadStrategy = pageLoadStrategy;
    	}
    	
        Calendar start = Calendar.getInstance();
        start.setTime(new Date());

        if (SeleniumTestsContextManager.getGlobalContext() != null
                && SeleniumTestsContextManager.getGlobalContext().getTestNGContext() != null) {
            suiteName = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getSuite().getName();
            outputDirectory = SeleniumTestsContextManager.getGlobalContext().getTestNGContext().getOutputDirectory();
        }
        
        // set the URL to context if it has been provided. This will then be used on Edge(IE-mode) creation to avoid cross zone boundaries
        if (SeleniumTestsContextManager.getThreadContext() != null && url != null) {
        	SeleniumTestsContextManager.getThreadContext().setInitialUrl(url);
        }
        
        // creates the driver and switch to it. It may be done twice as when the driver is created, we automatically switch to it, but in case driver
        // is already created,
        driver = WebUIDriver.getWebDriver(true, browserType, driverName, attachExistingDriverPort);
        
        if (driver == null && url != null) {
        	throw new ConfigurationException("driver is null, 'browser' configuration may be empty");
        }

        screenshotUtil = new ScreenshotUtil(driver);

        // open page
        openPage(url);
        
        // in case browser has been created outside of selenium and we attach to it, get initial window handles
        if (driver != null && attachExistingDriverPort != null && url == null) {
        	((CustomEventFiringWebDriver)driver).updateWindowsHandles();
        }

        // add calling page and field name on element
        setPageOnElements();

        assertCurrentPage(false, pageIdentifierElement);

        Calendar end = Calendar.getInstance();

        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        logger.log("Open web page in :" + (endTime - startTime) / 1000.0 + " seconds");

    }

    /**
     * Allow to switch to the appropriate context depending on the Page type
     */
    private void switchToContext() {
        Instant end = systemClock.instant().plusSeconds(SeleniumTestsContextManager.getThreadContext().getReplayTimeout());

        String context = null;
        if (getClass().isAnnotationPresent(ContextWebView.class)) {
            context = "web";
        } else if (getClass().isAnnotationPresent(ContextNativeApp.class)) {
            context = "native";
        } else if (getClass().isAnnotationPresent(Context.class)) {
            context = getClass().getAnnotation(Context.class).name();
        }
        final String finalContext = context;

        while (end.isAfter(systemClock.instant())) {
            if (context != null) {
                Optional<String> foundContext = getContexts().stream().filter(ctx -> ctx.toLowerCase().contains(finalContext.toLowerCase())).findFirst();
                if (foundContext.isPresent()) {
                    switchToContext(foundContext.get());
                    return;
                }
                WaitHelper.waitForSeconds(1);

            } else {
                return;
            }
        }
    }
    
    /**
     * associates the calling page instance ('this') to each Element instance of the page, so that it can create a sort of cache based on calling page
     * associates the name of the field to the element
     */
    private void setPageOnElements() {

    	for (Field field: getClass().getDeclaredFields()) {

            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);

                // associate the object with field name / calling page
                if (Element.class.isAssignableFrom(field.getType())) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        throw new ScenarioException(String.format("'%s' field must be static", field.getName()));
                    }
                    ((Element) field.get(this)).setFieldName(field.getName());
                    ((Element) field.get(this)).setCallingPage(this);
                }

                field.setAccessible(accessible);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new CustomSeleniumTestsException("Problem occured while setting element on page");
            }
        }
    }

    protected void setUrl(final String openUrl) {
        this.url = openUrl;
    }

    @Override
    public String getHtmlSource() {
    	return driver.getPageSource();
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
    private void openPage(String url) {
    	if (url != null) {
            open(url);
            ((CustomEventFiringWebDriver)driver).updateWindowsHandles();
        }

        // switch to the context if we are on mobile app
        switchToContext();

        // Wait for page load is applicable only for web test and mobile webview
        // When running tests on an iframe embedded site then test will fail if this command is not used
        // in case of mobile application and native context, only capture page
        if (driver != null && ((CustomEventFiringWebDriver)driver).isWebTest()) {
            waitForPageToLoad();
        } else if (SeleniumTestsContextManager.isAppTest() && captureSnapshot) {
        	capturePageSnapshot();
        
        }
    }

    @Override
    protected void assertCurrentPage(boolean log) {
    	// not used
    }
    
    @Override
    protected void assertCurrentPage(boolean log, HtmlElement pageIdentifierElement) {

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
     * Get parameter from configuration using pattern
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @return
     */
    public static String param(Pattern keyPattern) {
    	return TestTasks.param(keyPattern);
    }
    
    /**
     * Get parameter from configuration using pattern
     * If multiple variables match the pattern, only one is returned
     * @param keyPattern	Pattern for searching key. If null, no filtering will be done on key
     * @param valuePattern	Pattern for searching value. If null, no filtering will be done on value
     * @return
     */
    public static String param(Pattern keyPattern, Pattern valuePattern) {
    	return TestTasks.param(keyPattern, valuePattern);
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
     */
    public void addStep(String stepName) {
    	TestTasks.addStep(stepName);
    }

    /**
     * Add step inside a page
     * @param stepName
     * @param passwordToMask	array of strings that must be replaced by '*****' in reports
     */
    public void addStep(String stepName, String ... passwordToMask) {
    	TestTasks.addStep(stepName, passwordToMask);
    }
    
    /**
	 * Method for creating or updating a variable on the seleniumRobot server (or locally if server is not used)
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * Variable will be stored as a variable of the current tested application
     * @param key				name of the param
     * @param value				value of the parameter (or new value if we update it)
     */
    public void createOrUpdateParam(String key, String value) {
    	TestTasks.createOrUpdateParam(key, value);
    }
    
    /**
	 * Method for creating or updating a variable locally. If selenium server is not used, there is no difference with 'createOrUpdateParam'. 
	 * If seleniumRobot server is used, then, this method will only change variable value locally, not updating the remote one
	 * @param key
	 * @param newValue
	 */
	public void createOrUpdateLocalParam(String key, String newValue) {
		TestTasks.createOrUpdateLocalParam(key, newValue);
	}
    
	/**
     * Method for creating or updating a variable on the seleniumRobot server ONLY. This will raise a ScenarioException if variables are get from
     * env.ini file 
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param value				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     */
    public void createOrUpdateParam(String key, String value, boolean specificToVersion) {
    	TestTasks.createOrUpdateParam(key, value, specificToVersion);
    }

    /**
     * Deletes the variable associated to the provided key if it's stored on server
     * @param key		Name of the variable to delete
     */
    public void deleteParam(String key) {
        TestTasks.deleteParam(key);
    }
    
    /**
     * Method for creating or updating a variable. If variables are get from seleniumRobot server, this method will update the value on the server
     * Moreover, created custom variable is specific to tuple (application, version, test environment)
     * @param key					name of the param
     * @param value 				value of the parameter (or new value if we update it)
     * @param specificToVersion		if true, this param will be stored on server with a reference to the application version. This will have no effect if changing a 
     * 								current variable.
     * @param timeToLive			if > 0, this variable will be destroyed after some days (defined by variable). A positive value is mandatory if reservable is set to true 
     * 								because multiple variable can be created
     * @param reservable			if true, this variable will be set as reservable in variable server. This means it can be used by only one test at the same time
     * 								True value also means that multiple variables of the same name can be created and a timeToLive > 0 MUST be provided so that server database is regularly purged
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

    /**
     * Capture the whole page, scrolling if necessary
     * @param <T>
     * @return
     */
    public <T extends PageObject> T capturePageSnapshot() {
        capturePageSnapshot(null);
        return (T)this;
    }
    
    /**
     * Capture only the visible part of the page, no scrolling will be done
     * @param <T>
     * @return
     */
    public <T extends PageObject> T captureViewportSnapshot() {
    	captureViewportSnapshot(null);
    	return (T)this;
    }
    
    /**
     * Capture the desktop
     * @param <T>
     * @return
     */
    public <T extends PageObject> T captureDesktopSnapshot() {
    	captureDesktopSnapshot(null);
    	return (T)this;
    }
    
    /**
     * Capture a page snapshot for storing in test step
     * @param snapshotName	the snapshot name
     */
    public void capturePageSnapshot(String snapshotName) {
    	capturePageSnapshot(snapshotName, SnapshotCheckType.FALSE);
    }
    
    /**
     * Capture a viewport snapshot for storing in test step
     * @param snapshotName	the snapshot name
     */
    public void captureViewportSnapshot(String snapshotName) {
    	captureViewportSnapshot(snapshotName, SnapshotCheckType.FALSE);
    }
    
    /**
     * Capture a viewport snapshot for storing in test step
     * @param snapshotName	the snapshot name
     */
    public void captureDesktopSnapshot(String snapshotName) {
    	captureDesktopSnapshot(snapshotName, SnapshotCheckType.FALSE);
    }
    
    /**
     * Capture a page snapshot for storing in test step
     * @param snapshotName		the snapshot name
     * @param checkSnapshot		if true, will send snapshot to server (when seleniumRobot is configured for this) for comparison 
     */
    public void capturePageSnapshot(String snapshotName, SnapshotCheckType checkSnapshot) {
    	
    	ScreenShot screenShot = screenshotUtil.capture(SnapshotTarget.PAGE, ScreenShot.class, computeScrollDelay(checkSnapshot));
    	
    	// check SnapshotCheckType configuration is compatible with the snapshot
    	checkSnapshot.check(SnapshotTarget.PAGE);
    	
    	storeSnapshot(snapshotName, screenShot, checkSnapshot);
    }
    
    /**
     * Capture a page snapshot for storing in test step
     * @param snapshotName		the snapshot name
     * @param checkSnapshot		if true, will send snapshot to server (when seleniumRobot is configured for this) for comparison 
     */
    public void captureViewportSnapshot(String snapshotName, SnapshotCheckType checkSnapshot) {
    	
    	ScreenShot screenShot = screenshotUtil.capture(SnapshotTarget.VIEWPORT, ScreenShot.class);
    	
    	// check SnapshotCheckType configuration is compatible with the snapshot
    	checkSnapshot.check(SnapshotTarget.VIEWPORT);
    	
    	storeSnapshot(snapshotName, screenShot, checkSnapshot);
    }
    
    /**
     * Capture a desktop snapshot (only the main screen in multiple screen environment) for storing in test step
     * @param snapshotName		the snapshot name
     * @param checkSnapshot		if true, will send snapshot to server (when seleniumRobot is configured for this) for comparison 
     */
    public void captureDesktopSnapshot(String snapshotName, SnapshotCheckType checkSnapshot) {
    	
    	ScreenShot screenShot = screenshotUtil.capture(SnapshotTarget.MAIN_SCREEN, ScreenShot.class);	
    	storeSnapshot(snapshotName, screenShot, checkSnapshot);
    }
    
    /**
     * Returns 0 if snapshot is only taken for test report. Else, returns the defined scrollDelay from params
     * @param checkSnapshot
     */
    private int computeScrollDelay(SnapshotCheckType checkSnapshot) {
    	if (checkSnapshot.getControl() != Control.NONE) {
    		return robotConfig().getSnapshotScrollDelay().intValue();
    	} else {
    		return 0;
    	}
    }
    
    /**
     * Capture a portion of the page by giving the element to capture
     * @param element			the element to capture
     */
    public void captureElementSnapshot(WebElement element) {
    	captureElementSnapshot(null, element);
    }
    
    /**
     * Capture a portion of the page by giving the element to capture
     * @param snapshotName		the snapshot name
     * @param element			the element to capture
     */
    public void captureElementSnapshot(String snapshotName, WebElement element) {
    	captureElementSnapshot(snapshotName, element, SnapshotCheckType.FALSE);
    }
    
    /**
     * Capture a portion of the page by giving the element to capture
     * @param snapshotName		the snapshot name
     * @param element			the element to capture
     * @param checkSnapshot		if true, will send snapshot to server (when seleniumRobot is configured for this) for comparison 
     */
    public void captureElementSnapshot(String snapshotName, WebElement element, SnapshotCheckType checkSnapshot) {

    	SnapshotTarget snapshotTarget = new SnapshotTarget(element);
    	ScreenShot screenShot = screenshotUtil.capture(snapshotTarget, ScreenShot.class, computeScrollDelay(checkSnapshot));

    	// check SnapshotCheckType configuration is compatible with the snapshot
    	checkSnapshot.check(snapshotTarget);
    	
    	storeSnapshot(snapshotName, screenShot, checkSnapshot);
    }
    
    /**
     * Store the snapshot to test step
     * Check if name is provided, in case we need to compare it to a baseline on server
     * @param snapshotName
     * @param screenShot
     * @param checkSnapshot
     */
    private void storeSnapshot(String snapshotName, ScreenShot screenShot, SnapshotCheckType checkSnapshot) {
    	
    	if ((snapshotName == null || snapshotName.isEmpty()) && !checkSnapshot.equals(SnapshotCheckType.FALSE)) {
    		throw new ScenarioException("Cannot check snapshot if no name is provided");
    	}
    	
    	if (screenShot != null) { // may be null if user request not to take snapshots

	    	if (snapshotName != null) {
	    		screenShot.setTitle(snapshotName);
	    	}
	    	
	    	logger.logScreenshot(screenShot, snapshotName, checkSnapshot);
    	}
    	
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
            	selectPreviousOrMainWindow(handles);
            } else {
                WebUIDriver.setWebDriver(null);
            }
        } catch (UnreachableBrowserException ex) {
            WebUIDriver.setWebDriver(null);

        }
    }
	private void selectPreviousOrMainWindow(List<String> handles) {
		try { 
			selectWindow(handles.get(handles.indexOf(windowHandle) - 1));
		} catch (IndexOutOfBoundsException | NoSuchWindowException e) {
			selectMainWindow();
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
        new Actions(driver).dragAndDropBy( element.getElement(), offsetX, offsetY).perform();
    }

    /**
     * Get the number of elements in page
     * @param element
     * @return
     */
    public final int getElementCount(final HtmlElement element) {
        return element.findElements().size();
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
        return new LinkElement("Canonical URL", By.cssSelector("link[rel=canonical]")).getDomAttribute("href");
    }

    /**
     * Returns the value of the named cookie
     * @param name	name of the cookie
     * @return
     */
    public final String getCookieByName(final String name) {
        if (driver.manage().getCookieNamed(name) == null) {
            return null;
        }

        return driver.manage().getCookieNamed(name).getValue();
    }
    
    /**
     * Check if named cookie is present
     * @param name	name of the cookie
     * @return
     */
    public boolean isCookiePresent(final String name) {
        return getCookieByName(name) != null;
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

    /**
     * Hides keyboard
     * This only affects mobile tests
     */
    public void hideKeyboard() {
        ((CustomEventFiringWebDriver)driver).hideKeyboard();
    }

    /**
     * Returns the list of context. This only applies to mobile
     * For desktop web tests, "WEB" will be returned
     * @return
     */
    public List<String> getContexts() {
        return new ArrayList<>(((CustomEventFiringWebDriver)driver).getContextHandles());
    }

    public void switchToContext(String name) {
        try {
            ((CustomEventFiringWebDriver) driver).context(name);
        } catch (NoSuchContextException e) {
            throw new ScenarioException(String.format("Only %s contexts are available", getContexts()));
        }
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
            	logger.log("Unable to maximize browser window. Exception occured: " + ignore.getMessage());
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

    /**
     * Resize window to given dimensions.
     *
     * @param  width
     * @param  height
     */
    public final void resizeTo(final int width, final int height) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.isAppTest()) {
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

    /**
     * @deprecated useless
     * @return
     */
    @Deprecated
    public boolean isFrame() {
        return frameFlag;
    }

    /**
     * @deprecated useless
     * @return
     */
    @Deprecated
    public final void selectFrame(final Integer index) {
        driver.switchTo().frame(index);
        frameFlag = true;
    }

    /**
     * @deprecated useless
     * @return
     */
    @Deprecated
    public final void selectFrame(final By by) {
    	WebElement element = driver.findElement(by);
        driver.switchTo().frame(element);
        frameFlag = true;
    }

    /**
     * @deprecated useless
     * @return
     */
    @Deprecated
    public final void selectFrame(final String locator) {
        driver.switchTo().frame(locator);
        frameFlag = true;
    }

    /**
     * @deprecated useless
     * @return
     */
    @Deprecated
    public final void exitFrame() {
    	driver.switchTo().defaultContent();
    	frameFlag = false;
    }

    /**
     * Switch to first window in the list
     */
    public final void selectMainWindow() {
    	selectWindow(0);
    }

    /**
     * Switch to nth window in the list
     * It may not reflect the order of tabs in browser if some windows have been closed before
     * @param index		index of the window
     */
    public final void selectWindow(final int index) {
    	// app test are not compatible with window
    	if (SeleniumTestsContextManager.isAppTest()) {
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
    
    private boolean hasAlreadySwitchedWindow(String currentWindowHandle) {
    	// handles obtained before the click
    	Set<String> knownHandles  = getCurrentHandles();
    	return !knownHandles.contains(currentWindowHandle);
    	
    }
    
    /**
     * Selects the first unknown window. To use immediately after an action creates a new window or tab
     * Each time we do a click, but just before it (selenium click, JS click or action click), we record the list of windows.
     * I a new window or tab is displayed, we select it.
     * This also check if the switch has already been done (it's the case for HtmlUnit 3.57 where it switchs automatically
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

    	// in case switch has already been done (HtmlUnit case)
    	if (hasAlreadySwitchedWindow(mainWindowHandle)) {

     		internalLogger.info("Driver already switched to new window");
    		return (String) getCurrentHandles().toArray()[0];
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
    		
    		
    		if (pageLoadStrategy == PageLoadStrategy.NORMAL) {
    			new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"complete\") { return \"ok\"; }"));
    		} else if (pageLoadStrategy == PageLoadStrategy.EAGER) {
    			new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.jsReturnsValue("if (document.readyState === \"interactive\") { return \"ok\"; }"));
    		}
    	} catch (TimeoutException e) {
    		// nothing
    	}
    	

        // populate page info
    	if (captureSnapshot) {
	        try {
	        	capturePageSnapshot();
	        } catch (Exception ex) {
	        	internalLogger.error(ex);
	            throw ex;
	        }
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

    public String cancelConfirmation() {
    	Alert alert = getAlert();
        String seenText = alert.getText();
        alert.dismiss();
        driver.switchTo().defaultContent();
        return seenText;
    }
	
	/**
	 * Returns an Element object based on field name
	 * @return
	 */
	private Element getElement(String fieldName) {
		
		try {
			Field field = getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (Element)field.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new ScenarioException(String.format("Field %s does not exist in class %s", fieldName, getClass().getSimpleName()));
		}
	}
	
    // --------------------- Actions --------------------------
    // these are essentially present for cucumber test for which we can use generic actions without grouping them into methods

	@GenericStep
    public <T extends PageObject> T goBack() {
        driver.navigate().back();
        return (T)this;
    }
	
	@GenericStep
    public <T extends PageObject> T goForward() {
        driver.navigate().forward();
        return (T)this;
    }

	@GenericStep
	public <T extends PageObject> T sendKeysToField(String fieldName, String value) {
		Element element = getElement(fieldName);
		element.sendKeys(value);
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T sendRandomKeysToField(Integer charNumber, String fieldName) {
		Element element = getElement(fieldName);
		element.sendKeys(RandomStringUtils.random(charNumber, true, false));
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	@GenericStep
	public <T extends PageObject> T clear(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).clear();
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@SuppressWarnings("unchecked")
	@GenericStep
    public <T extends PageObject> T selectOption(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof SelectList) {
			((SelectList)element).selectByText(value);
		} else {
			throw new ScenarioException(String.format("Element %s is not an SelectList", fieldName));
		}
		return (T)this;
    }

	@SuppressWarnings("unchecked")
	@GenericStep
	public <T extends PageObject> T click(String fieldName) {
		Element element = getElement(fieldName);
		element.click();
		return (T)this;
	}
	
	/**
	 * Click on element and creates a new PageObject of the type of following page
	 * @param fieldName
	 * @param nextPage		Class of the next page
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@GenericStep
	public <T extends PageObject> T clickAndChangeToPage(String fieldName, Class<T> nextPage) {
		Element element = getElement(fieldName);
		element.click();
		try {
			return nextPage.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ScenarioException(String.format("Cannot switch to next page %s, maybe default constructor does not exist", nextPage.getSimpleName()), e);
		}
	}
	
	/**
	 * Return the next page
	 * @param <T>
	 * @param nextPage
	 * @return
	 */
	@GenericStep
	public <T extends PageObject> T changeToPage(Class<T> nextPage) {
		try {
			return nextPage.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ScenarioException(String.format("Cannot switch to next page %s, maybe default constructor does not exist", nextPage.getSimpleName()), e);
		}
	} 

	@GenericStep
	public <T extends PageObject> T doubleClick(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).doubleClickAction();
		} else {
			((GenericPictureElement)element).doubleClick();
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T wait(Integer waitMs) {
		WaitHelper.waitForMilliSeconds(waitMs);
		return (T)this;
	}

	@GenericStep
    public <T extends PageObject> T clickTableCell(Integer row, Integer column, String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			((Table)element).getCell(row, column).click();
		} else {
			throw new ScenarioException(String.format(ERROR_ELEMENT_S_IS_NOT_AN_TABLE_ELEMENT, fieldName));
		}
		return (T)this;
    }

    /**
     * Switch to the newly created window
     * @return
     */
	@GenericStep
    public <T extends PageObject> T switchToNewWindow() {
    	selectNewWindow();
    	return (T)this;
    }
    
    /**
     * Switch to the newly created window with wait
     * @return
     */
	@GenericStep
    public <T extends PageObject> T switchToNewWindow(int waitMs) {
    	selectNewWindow(waitMs);
    	return (T)this;
    }
    
    /**
     * Select first window in the list
     * @return
     */
	@GenericStep
    public <T extends PageObject> T switchToMainWindow() {
    	selectMainWindow();
    	return (T)this;
    }
    
    /**
     * Selects the nth window in list
     * @param index
     * @return
     */
	@GenericStep
    public <T extends PageObject> T switchToWindow(int index) {
    	selectWindow(index);
    	return (T)this;
    }

    /**
     * Refresh browser window
     * @return
     */
	@GenericStep
    public <T extends PageObject> T refresh()  {
    	if (SeleniumTestsContextManager.isWebTest()) {
	        try {
	            driver.navigate().refresh();
	        } catch (org.openqa.selenium.TimeoutException ex) {
	        	logger.error("got time out customexception, ignore");
	        }
    	}
        return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T acceptAlert() {
        Alert alert = getAlert();
        if (alert != null) {
        	alert.accept();
        }
        driver.switchTo().defaultContent();
        return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T cancelAlert() {
    	cancelConfirmation();
    	return (T)this;
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
	@GenericStep
	public <T extends PageObject> T uploadFile(String filePath) {
		try {
			byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(new File(filePath)));
			CustomEventFiringWebDriver.uploadFile(new File(filePath).getName(), 
					new String(encoded), 
					SeleniumTestsContextManager.getThreadContext().getRunMode(), 
					SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector());

			Alert alert = waitForAlert(5);
			if (alert != null) {
				alert.accept();
			}
			
		} catch (IOException e) {
			throw new ScenarioException(String.format("could not read file to upload %s: %s", filePath, e.getMessage()));
		}
		return (T)this;
	}
	
    // --------------------- Waits --------------------------
	@GenericStep
	public <T extends PageObject> T waitForPresent(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForPresent();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (!present) {
				throw new TimeoutException(String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			}
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T waitForVisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForVisibility();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (!present) {
				throw new TimeoutException(String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			}
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T waitForNotPresent(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForNotPresent();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (present) {
				throw new TimeoutException(String.format(ERROR_ELEMENT_IS_PRESENT, fieldName));
			}
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T waitForInvisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitForInvisibility();
		} else {
			boolean present = ((GenericPictureElement)element).isElementPresent(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout() * 1000);
			if (present) {
				throw new TimeoutException(String.format(ERROR_ELEMENT_IS_PRESENT, fieldName));
			}
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T waitForValue(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			((HtmlElement) element).waitFor(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 
					ExpectedConditions.or(
							ExpectedConditions.domPropertyToBe((HtmlElement)element, "value", value),
							ExpectedConditions.textToBePresentInElement((HtmlElement)element, value)
							));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
    public <T extends PageObject> T waitTableCellValue(Integer row, Integer column, String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			((HtmlElement) element).waitFor(SeleniumTestsContextManager.getThreadContext().getExplicitWaitTimeout(), 
					ExpectedConditions.textToBePresentInElement(((Table)element).getCell(row, column), value));
			
		} else {
			throw new ScenarioException(String.format(ERROR_ELEMENT_S_IS_NOT_AN_TABLE_ELEMENT, fieldName));
		}
		return (T)this;
    }
	
    // --------------------- Assertions --------------------------
	@GenericStep
	public <T extends PageObject> T assertForInvisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertFalse(((HtmlElement) element).isElementPresent(0) && ((HtmlElement) element).isDisplayed(), String.format("Element %s is visible", fieldName));
		} else {
			Assert.assertFalse(((GenericPictureElement)element).isElementPresent(), String.format("Element %s is visible", fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForVisible(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(((HtmlElement) element).isDisplayed(), String.format("Element %s is not visible", fieldName));
		} else {
			Assert.assertTrue(((GenericPictureElement)element).isElementPresent(), String.format("Element %s is not visible", fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForDisabled(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertFalse(((HtmlElement) element).isEnabled(), String.format("Element %s is enabled", fieldName));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForEnabled(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(((HtmlElement) element).isEnabled(), String.format("Element %s is disabled", fieldName));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForValue(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(((HtmlElement) element).getText().equals(value) || ((HtmlElement) element).getValue().equals(value), String.format("Value of element %s is not %s", fieldName, value));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForEmptyValue(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(((HtmlElement) element).getValue().isEmpty(), String.format("Value or Element %s is not empty", fieldName));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForNonEmptyValue(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertFalse(((HtmlElement) element).getValue().isEmpty(), String.format("Element %s is empty", fieldName));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertForMatchingValue(String fieldName, String regex) {
		Element element = getElement(fieldName);
		if (element instanceof HtmlElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(Pattern.compile(regex).matcher(((HtmlElement) element).getText()).find()
					|| Pattern.compile(regex).matcher(((HtmlElement) element).getValue()).find(),
					String.format("Value of Element %s does not match %s ", fieldName, regex));
		} else {
			throw new ScenarioException(String.format(ELEMENT_S_IS_NOT_AN_HTML_ELEMENT_SUBCLASS, fieldName));
		}
		return (T)this;
	}

	@GenericStep
    public <T extends PageObject> T assertSelectedOption(String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof SelectList) {
			try {
				WebElement selectedOption = ((SelectList)element).getFirstSelectedOption();
				Assert.assertNotNull(selectedOption, "No selected option found");
				Assert.assertEquals(selectedOption.getText(), value, "Selected option is not the expected one");
			} catch (WebDriverException e) {
				Assert.assertTrue(false, String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			}
		} else {
			throw new ScenarioException(String.format("Element %s is not an SelectList subclass", fieldName));
		}
		return (T)this;
    }

	@GenericStep
	public <T extends PageObject> T assertChecked(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof CheckBoxElement || element instanceof RadioButtonElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertTrue(((HtmlElement)element).isSelected(), String.format("Element %s is unchecked", fieldName));
		} else {
			throw new ScenarioException(String.format("Element %s is not an CheckBoxElement/RadioButtonElement", fieldName));
		}
		return (T)this;
	}

	@GenericStep
	public <T extends PageObject> T assertNotChecked(String fieldName) {
		Element element = getElement(fieldName);
		if (element instanceof CheckBoxElement || element instanceof RadioButtonElement) {
			Assert.assertTrue(((HtmlElement) element).isElementPresent(0), String.format(ERROR_ELEMENT_NOT_PRESENT, fieldName));
			Assert.assertFalse(((HtmlElement)element).isSelected(), String.format("Element %s is checked", fieldName));
		} else {
			throw new ScenarioException(String.format("Element %s is not an CheckBoxElement/RadioButtonElement", fieldName));
		}
		return (T)this;
	}

	@GenericStep
    public <T extends PageObject> T assertTableCellValue(Integer row, Integer column, String fieldName, String value) {
		Element element = getElement(fieldName);
		if (element instanceof Table) {
			try {
				Assert.assertEquals(((Table)element).getCell(row, column).getText(), value, String.format("Value of cell [%d,%d] in table %s is not %s", row, column, fieldName, value));
			} catch (WebDriverException e) {
				Assert.assertTrue(false, "Table or cell not found");
			}
		} else {
			throw new ScenarioException(String.format(ERROR_ELEMENT_S_IS_NOT_AN_TABLE_ELEMENT, fieldName));
		}
		return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T assertTextPresentInPage(String text) {
    	Assert.assertTrue(isTextPresent(text));
    	return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T assertTextNotPresentInPage(String text) {
    	Assert.assertFalse(isTextPresent(text));
    	return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T assertCookiePresent(String name) {
    	Assert.assertTrue(isCookiePresent(name), "Cookie: {" + name + "} not found.");
    	return (T)this;
    }

	@GenericStep
    public <T extends PageObject> T assertElementCount(String fieldName, int elementCount) {
    	Element element = getElement(fieldName);
    	if (element instanceof HtmlElement) {
			Assert.assertEquals(((HtmlElement)element).findElements().size(), elementCount);
		} else {
			throw new ScenarioException(String.format("Element %s is not an HtmlElement", fieldName));
		}
		return (T)this;
    }
    
    /**
     * Check page title matches parameter
     * @param regexTitle
     * @return
     */
	@GenericStep
    public <T extends PageObject> T assertPageTitleMatches(String regexTitle) {
    	Assert.assertTrue(getTitle().matches(regexTitle));
    	return (T)this;
    }

	@GenericStep
    public void assertHtmlSource(String text) {
        Assert.assertTrue(getHtmlSource().contains(text), String.format("Text: {%s} not found on page source.", text));
    }

	
	/**
	 * @deprecated useless
	 * @param text
	 */
    @Deprecated
    public void assertKeywordNotPresent(String text) {
        Assert.assertFalse(getHtmlSource().contains(text), String.format("Text: {%s} not found on page source.", text));
    }

	@GenericStep
    public void assertLocation(String urlPattern) {
        Assert.assertTrue(getLocation().contains(urlPattern), "Pattern: {" + urlPattern + "} not found on page location.");
    }

	/**
	 * @deprecated useless
	 * @param text
	 */
    @Deprecated
    public void assertTitle(final String text) {
        Assert.assertTrue(getTitle().contains(text), String.format("Text: {%s} not found on page title.", text));
    }
	

	public ScreenshotUtil getScreenshotUtil() {
		return screenshotUtil;
	}

	public void setScreenshotUtil(ScreenshotUtil screenshotUtil) {
		this.screenshotUtil = screenshotUtil;
	}
}

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
package com.seleniumtests.browserfactory;

import java.net.URL;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.SkipException;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridNodeNotAvailable;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.helper.WaitHelper;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class SeleniumGridDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
	private List<SeleniumGridConnector> gridConnectors;
	private SeleniumGridConnector activeGridConnector;
	public static final int DEFAULT_RETRY_TIMEOUT = 1800; // timeout in seconds (wait 30 mins for connecting to grid)
	private static int retryTimeout = DEFAULT_RETRY_TIMEOUT;
	private int instanceRetryTimeout; 
	private static AtomicInteger counter = new AtomicInteger(0); // a global counter counting times where we never get matching nodes

    public SeleniumGridDriverFactory(final DriverConfig cfg) {
		this(cfg, true);
	}

	public List<SeleniumGridConnector> getGridConnectors() {
		return gridConnectors;
	}

	/**
	 *
	 * @param cfg
	 * @param shuffle	if true, grid connector list will be shuffled so that it's not the same hub that gets driver
	 */
    public SeleniumGridDriverFactory(final DriverConfig cfg, boolean shuffle) {
        super(cfg);
        gridConnectors = new ArrayList<>(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors());
        instanceRetryTimeout = retryTimeout;
        
        // reorder list so that we do not always use the same grid for connection
		if (shuffle) {
			Collections.shuffle(gridConnectors);
		}
    }    

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if (SeleniumTestsContextManager.isDesktopWebTest()) {
			switch (webDriverConfig.getBrowserType()) {

				case FIREFOX:
					return new FirefoxCapabilitiesFactory(webDriverConfig);
				case INTERNET_EXPLORER:
					return new IECapabilitiesFactory(webDriverConfig);
				case CHROME:
					return new ChromeCapabilitiesFactory(webDriverConfig);
				case HTMLUNIT:
					return new HtmlUnitCapabilitiesFactory(webDriverConfig);
				case SAFARI:
					return new SafariCapabilitiesFactory(webDriverConfig);
				case EDGE:
					return new EdgeCapabilitiesFactory(webDriverConfig);
				default:
					throw new ConfigurationException(String.format("Browser %s is unknown for desktop tests", webDriverConfig.getBrowserType()));
			}
		} else if (SeleniumTestsContextManager.isDesktopAppTest()) {
			if ("windows".equalsIgnoreCase(webDriverConfig.getPlatform())) {
				return new WindowsAppCapabilitiesFactory(webDriverConfig);
			} else {
				throw new ConfigurationException(String.format("Platform %s is not supported for application tests", webDriverConfig.getPlatform()));
			}
		} else if (SeleniumTestsContextManager.isMobileTest()) {
			if ("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
				return new AndroidCapabilitiesFactory(webDriverConfig);
			} else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())) {
				return new IOsCapabilitiesFactory(webDriverConfig);
			} else {
				throw new ConfigurationException(String.format("Platform %s is unknown for mobile tests", webDriverConfig.getPlatform()));
			}
		} else {
			throw new ConfigurationException("Wrong test format detected. Should be either mobile or desktop");
		}
	}
    
    /**
     * Creates capabilities specific to seleniumGrid
     * For example, Appium needs PLATFORM_NAME and PLATFORM_VERSION capabilities, but seleniumGrid matcher (default seleniumGrid)
     * looks at PLATFORM and VERSION capabilities. This method adds them
     * OS version is only updated for mobile. It has no real sense on desktop
     * @return
     */
    public MutableCapabilities createSpecificGridCapabilities(DriverConfig webDriverConfig) {
    	MutableCapabilities capabilities = new MutableCapabilities();
    	
    	if (SeleniumTestsContextManager.isMobileTest()) {
			capabilities = new BaseOptions(capabilities).setPlatformVersion(webDriverConfig.getMobilePlatformVersion());
    	} else {
    		capabilities.setCapability(CapabilityType.PLATFORM_NAME, webDriverConfig.getPlatform().toLowerCase());
    		if (webDriverConfig.getBrowserVersion() != null && capabilities.getCapability(CapabilityType.BROWSER_VERSION) == null) {
    			capabilities.setCapability(CapabilityType.BROWSER_VERSION, webDriverConfig.getBrowserVersion());
    		}
    	}
    	
    	// configure the node were browser should be created
    	if (webDriverConfig.getRunOnSameNode() != null) {
    		capabilities.setCapability(SeleniumRobotCapabilityType.ATTACH_SESSION_ON_NODE, webDriverConfig.getRunOnSameNode());
    		instanceRetryTimeout = Math.min(90, DEFAULT_RETRY_TIMEOUT / 2);
    	}
    	
    	return capabilities;
    }
    
   
    @Override
    public WebDriver createWebDriver() {

        // create capabilities, specific to OS
        MutableCapabilities capabilities = createSpecificGridCapabilities(webDriverConfig);
		capabilities = capabilities.merge(driverOptions);
        
        // app must be uploaded before driver creation because driver will need it in mobile app testing
        // upload file on all available grids as we don't know which one will be chosen before driver has been created
        for (SeleniumGridConnector gridConnector: gridConnectors) {
			capabilities = gridConnector.uploadMobileApp(capabilities);
        }

        // connection to grid is made here
        for (int i = 0; i < 3; i++) {
	        Instant start = Instant.now();
	        driver = getDriver(capabilities);
	        long duration = Duration.between(start, Instant.now()).toMillis();
	
	        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
	        if (webDriverConfig.getPageLoadTimeout() >= 0 && SeleniumTestsContextManager.isWebTest()) {
	            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout());
	        }
	
	        this.setWebDriver(driver);
	
	        // if session has not been really created, we retry
	        try {
	        	activeGridConnector.getSessionInformationFromGrid((RemoteWebDriver) driver, duration);
	        } catch (SessionNotCreatedException e) {
	        	logger.error(e.getMessage());
				try {
					driver.quit();
				} catch (Exception e1) {
					// nothing to do
				}
	        	continue;
	        }
	
	        // sets a file detector. This is only useful for remote drivers
	        ((RemoteWebDriver)driver).setFileDetector(new LocalFileDetector());
	        
	        // create a BrowserInfo based on information get from grid hub
	        selectedBrowserInfo = new BrowserInfo(BrowserType.getBrowserTypeFromSeleniumBrowserType(((RemoteWebDriver)driver).getCapabilities().getBrowserName()), 
		        									((RemoteWebDriver)driver).getCapabilities().getBrowserVersion());
	
	        return driver;
        }
        
        throw new SessionNotCreatedException("Session not created on any grid hub, after 3 tries");
    }
    
    public WebDriver getDriverInstance(URL hubUrl, MutableCapabilities capabilities) {
		if (SeleniumTestsContextManager.isDesktopWebTest()) {
			return new RemoteWebDriver(hubUrl, capabilities);
		} else if (SeleniumTestsContextManager.isDesktopAppTest()) {
			return new AppiumDriver(hubUrl, capabilities);
		} else if (SeleniumTestsContextManager.isMobileTest()) {
			if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
        		return new AndroidDriver(hubUrl, capabilities);
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	return new IOSDriver(hubUrl, capabilities);
	        } else {
	        	throw new ConfigurationException(String.format("Platform %s is unknown for mobile tests", webDriverConfig.getPlatform()));
	        }
		} else {
			throw new ConfigurationException("Wrong test format detected. Should be either mobile or desktop");
		}
	}
    
    /**
     * Connect to grid using RemoteWebDriver
     * As we may have several grid available, takes the first one where driver is created
     * 
     * Several waits are defined
     * By default, we wait 30 mins for a node to be found. For this, we loop through all available hubs
     * In case we do not find any node after 30 mins, we fail and increment a fail counter
     * This fail counter is reset every time we find a node
     * If this counter reaches 3, then we don't even try to get a driver
     *
     * @param capabilities
     * @return
     */
    private WebDriver getDriver(MutableCapabilities capabilities){
    	driver = null;
    	
    	Clock clock = Clock.systemUTC();
		Instant end = clock.instant().plusSeconds(instanceRetryTimeout);
		Exception currentException = null;
		
		if (webDriverConfig.getRunOnSameNode() != null && webDriverConfig.getSeleniumGridConnector() == null) {
			throw new ScenarioException("Cannot create a driver on the same node as an other driver if no previous driver has been created through grid");
		}
		
		// issue #311: stop after 3 consecutive failure getting nodes
		int noDriverCount = counter.get();
		if (noDriverCount > 2) {
			throw new SkipException("Skipping as the 3 previous tests could not get any matching node. Check your test configuration and grid setup");
		}
    	
		int i = 0;
		while (end.isAfter(clock.instant())) {

			capabilities.setCapability(SeleniumRobotCapabilityType.SESSION_CREATION_TRY, i);
			i += 1;
			
			for (SeleniumGridConnector gridConnector: gridConnectors) {
			
				// if grid is not active, try the next one
				if (!gridConnector.isGridActive()) {
					logger.warn(String.format("grid %s is not active, looking for the next one", gridConnector.getHubUrl().toString()));
					continue;
				}
				
				// if we are launching a second driver for the same test, do it on the same hub
				if (webDriverConfig.getRunOnSameNode() != null && webDriverConfig.getSeleniumGridConnector() != gridConnector) {
					continue;
				}

				// set time of request, for debug
				capabilities.setCapability(SeleniumRobotCapabilityType.SESSION_CREATION_REQUEST_TIME, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
				
				try {
					driver = getDriverInstance(gridConnector.getHubUrl(), capabilities);
					activeGridConnector = gridConnector;
					break;
				} catch (WebDriverException e) {
					logger.warn(String.format("Error creating driver on hub %s: %s", gridConnector.getHubUrl().toString(), e.getMessage()));
					currentException = e;
				}
			}
			
			// do not wait more
			if (driver != null) {
				break;
			}
			
			if (currentException != null) {
				WaitHelper.waitForSeconds(5);
			} else {
				// we are here if no grid connector is available
				logger.warn("No grid available, wait 30 secs and retry");
				
				// for test only, reduce wiat
				if (instanceRetryTimeout > 30) {
					WaitHelper.waitForSeconds(30);
				} else {
					WaitHelper.waitForSeconds(1);
				}
			}
		}
		
		if (driver == null) {
			noDriverCount = counter.getAndIncrement();
			throw new SeleniumGridNodeNotAvailable(String.format("Cannot create driver on grid, it may be fully used [%d times]", noDriverCount), currentException);
		} else {
			// reset counter as we got a driver
			counter.set(0); 
		}
    	
    	return driver;
    }

	@Override
	protected WebDriver createNativeDriver() {
		return null;
	}

	public static int getRetryTimeout() {
		return retryTimeout;
	}

	public static void setRetryTimeout(int retryTimeout) {
		SeleniumGridDriverFactory.retryTimeout = retryTimeout;
	}

	public SeleniumGridConnector getActiveGridConnector() {
		return activeGridConnector;
	}

	public int getInstanceRetryTimeout() {
		return instanceRetryTimeout;
	}

	public static int getCounter() {
		return counter.get();
	}

	/**
	 * For tests
	 */
	public static void resetCounter() {
		counter.set(0);
	}
}

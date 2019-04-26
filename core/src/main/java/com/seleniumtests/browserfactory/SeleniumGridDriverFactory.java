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

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumGridDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
	private List<SeleniumGridConnector> gridConnectors;
	private SeleniumGridConnector activeGridConnector;
	public static final int DEFAULT_RETRY_TIMEOUT = 1800; // timeout in seconds (wait 30 mins for connecting to grid)
	private static int retryTimeout = DEFAULT_RETRY_TIMEOUT;

    public SeleniumGridDriverFactory(final DriverConfig cfg) {
        super(cfg);
        gridConnectors = new ArrayList<>(SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnectors());
        
        // reorder list so that we do not always use the same grid for connection
        Collections.shuffle(gridConnectors);
    }    

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if (SeleniumTestsContextManager.isDesktopWebTest()) {
			switch (webDriverConfig.getBrowserType()) {
	
		        case FIREFOX :
		            return new FirefoxCapabilitiesFactory(webDriverConfig);
		        case INTERNET_EXPLORER :
		        	return new IECapabilitiesFactory(webDriverConfig);
		        case CHROME :
		        	return new ChromeCapabilitiesFactory(webDriverConfig);
		        case HTMLUNIT :
		        	return new HtmlUnitCapabilitiesFactory(webDriverConfig);
		        case SAFARI :
		        	return new SafariCapabilitiesFactory(webDriverConfig);
		        case PHANTOMJS :
		        	return new PhantomJSCapabilitiesFactory(webDriverConfig);
		        case EDGE :
		        	return new EdgeCapabilitiesFactory(webDriverConfig);
		        default :
		        	throw new ConfigurationException(String.format("Browser %s is unknown for desktop tests", webDriverConfig.getBrowserType()));
		    }
		} else if (SeleniumTestsContextManager.isMobileTest()) {
        	if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
        		return new AndroidCapabilitiesFactory(webDriverConfig);
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
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
    private DesiredCapabilities createSpecificGridCapabilities(DriverConfig webDriverConfig) {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	
    	if (SeleniumTestsContextManager.isMobileTest()) {
    		capabilities.setCapability(CapabilityType.VERSION, webDriverConfig.getMobilePlatformVersion());
    	} else {
    		capabilities.setCapability(CapabilityType.PLATFORM, webDriverConfig.getPlatform().toLowerCase());
    		if (webDriverConfig.getBrowserVersion() != null) {
    			capabilities.setCapability(CapabilityType.VERSION, webDriverConfig.getBrowserVersion());
    		}
    	}
    	
    	// configure the node were browser should be created
    	if (webDriverConfig.getRunOnSameNode() != null) {
    		capabilities.setCapability(SeleniumRobotCapabilityType.ATTACH_SESSION_ON_NODE, webDriverConfig.getRunOnSameNode());
    	}
    	
    	return capabilities;
    }
    
    @Override
    public WebDriver createWebDriver() {

        // create capabilities, specific to OS
        MutableCapabilities capabilities = createSpecificGridCapabilities(webDriverConfig);
        capabilities.merge(driverOptions);
        
        // app must be uploaded before driver creation because driver will need it in mobile app testing
        // upload file on all available grids as we don't know which one will be chosen before driver has been created
        for (SeleniumGridConnector gridConnector: gridConnectors) {
        	gridConnector.uploadMobileApp(capabilities);
        }

        // connection to grid is made here
        driver = getDriver(capabilities);

        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0 && SeleniumTestsContextManager.isWebTest()) {
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout());
        }

        this.setWebDriver(driver);

        runWebDriver();

        ((RemoteWebDriver)driver).setFileDetector(new LocalFileDetector());

        return driver;
    }
    
    /**
     * Connect to grid using RemoteWebDriver
     * As we may have several grid available, takes the first one where driver is created
     * @param url
     * @param capability
     * @return
     */
    private WebDriver getDriver(MutableCapabilities capability){
    	driver = null;
    	
    	Clock clock = Clock.systemUTC();
		Instant end = clock.instant().plusSeconds(retryTimeout);
		Exception currentException = null;
    	
		while (end.isAfter(clock.instant())) {
			
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
				
				try {
					driver = new RemoteWebDriver(gridConnector.getHubUrl(), capability);
					activeGridConnector = gridConnector;
					break;
				} catch (WebDriverException e) {
					logger.warn(String.format("Error creating driver on hub %s: %s", gridConnector.getHubUrl().toString(), e.getMessage()));
					currentException = e;
					continue;
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
				if (retryTimeout > 1) {
					WaitHelper.waitForSeconds(30);
				} else {
					WaitHelper.waitForSeconds(1);
				}
			}
		}
		
		if (driver == null) {
			throw new SeleniumGridException("Cannot create driver on grid, it may be fully used", currentException);
		}
    	
    	return driver;
    }
    
    private void runWebDriver(){
    	activeGridConnector.runTest((RemoteWebDriver) driver);
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
}

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
package com.seleniumtests.browserfactory;

import java.net.URL;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.SystemClock;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumGridDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
	private SeleniumGridConnector gridConnector;
	public static final int DEFAULT_RETRY_TIMEOUT = 1800; // timeout in seconds (wait 30 mins for connecting to grid)
	private static int retryTimeout = DEFAULT_RETRY_TIMEOUT;

    public SeleniumGridDriverFactory(final DriverConfig cfg) {
        super(cfg);
        gridConnector = SeleniumTestsContextManager.getThreadContext().getSeleniumGridConnector();
    }    

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if (SeleniumTestsContextManager.isDesktopWebTest()) {
			switch (webDriverConfig.getBrowser()) {
	
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
		        	throw new ConfigurationException(String.format("Browser %s is unknown for desktop tests", webDriverConfig.getBrowser()));
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
    	
    	return capabilities;
    }
    
    @Override
    public WebDriver createWebDriver() {

        // create capabilities, specific to OS
        MutableCapabilities capabilities = createSpecificGridCapabilities(webDriverConfig);
        capabilities.merge(driverOptions);
        
        // 
        gridConnector.uploadMobileApp(capabilities);

        // connection to grid is made here
        driver = getDriver(gridConnector.getHubUrl(), capabilities);

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
     * @param url
     * @param capability
     * @return
     */
    private WebDriver getDriver(URL url, MutableCapabilities capability){
    	driver = null;
    	
    	SystemClock clock = new SystemClock();
		long end = clock.laterBy(retryTimeout * 1000L);
		Exception currentException = null;
    	
		while (clock.isNowBefore(end)) {
			try {
				driver = new RemoteWebDriver(url, capability);
				break;
			} catch (WebDriverException e) {
				logger.warn("Error creating driver, retrying: " + e.getMessage());
				WaitHelper.waitForSeconds(2);
				currentException = e;
				continue;
			}
		}
		
		if (driver == null) {
			throw new SeleniumGridException("Cannot create driver on grid, it may be fully used", currentException);
		}
    	
    	return driver;
    }
    
    private void runWebDriver(){
    	gridConnector.runTest((RemoteWebDriver) driver);
    }

	@Override
	protected WebDriver createNativeDriver() {
		return null;
	}
}

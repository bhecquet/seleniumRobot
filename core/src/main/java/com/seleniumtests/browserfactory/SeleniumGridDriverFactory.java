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
package com.seleniumtests.browserfactory;

import java.net.URL;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.screenshots.ScreenShotRemoteWebDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.helper.WaitHelper;

public class SeleniumGridDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
	private SeleniumGridConnector gridConnector;

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
        // TODO: remote ScreenShotRemoteWebDriver reference (seems useless)
        // 			check TAKE_SCREENSHOT capability is always present
        if (SeleniumTestsContextManager.isWebTest() && (BrowserType.FIREFOX).equals(webDriverConfig.getBrowser())) {
            driver = getDriverFirefox(gridConnector.getHubUrl(), capabilities);
        } else {
            driver = new ScreenShotRemoteWebDriver(gridConnector.getHubUrl(), capabilities);
        }

        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0 && SeleniumTestsContextManager.isWebTest()) {
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout());
        }

        this.setWebDriver(driver);

        runWebDriver();

        ((RemoteWebDriver)driver).setFileDetector(new LocalFileDetector());

        return driver;
    }
    
    private WebDriver getDriverFirefox(URL url, MutableCapabilities capability){
    	driver = null;
    	try {
            driver = new ScreenShotRemoteWebDriver(url, capability);
        } catch (RuntimeException e) {
            if (e.getMessage().contains(
                        "Unable to connect to host localhost on port 7062 after 45000 ms. Firefox console output")) {
                TestLogging.log("Firefox Driver creation got port customexception, retry after 5 seconds");
                WaitHelper.waitForSeconds(5);
                driver = new ScreenShotRemoteWebDriver(url, capability);
            } else {
                throw e;
            }
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

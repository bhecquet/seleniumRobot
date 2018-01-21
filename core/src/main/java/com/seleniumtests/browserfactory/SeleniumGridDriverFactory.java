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
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.UnsupportedCommandException;
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
        gridConnector = SeleniumGridConnectorFactory.getInstance(cfg.getHubUrl());
    }

    /**
     * Create a capability depending on the browser type.
     * @param webDriverConfig
     * @return the capability for a given browser
     */
    public MutableCapabilities createCapabilityByBrowser(DriverConfig webDriverConfig, MutableCapabilities capabilities){

    	switch (webDriverConfig.getBrowser()) {

	        case FIREFOX :
	            capabilities.merge(new FirefoxCapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	
	        case INTERNET_EXPLORER :
	        	capabilities.merge(new IECapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	
	        case CHROME :
	        	capabilities.merge(new ChromeCapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	
	        case HTMLUNIT :
	        	capabilities.merge(new HtmlUnitCapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	
	        case SAFARI :
	        	capabilities.merge(new SafariCapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	
	        case PHANTOMJS :
	        	capabilities.merge(new PhantomJSCapabilitiesFactory(webDriverConfig).createCapabilities());
	            break;
	            
	        case EDGE :
	        	capabilities.merge(new EdgeCapabilitiesFactory(webDriverConfig).createCapabilities());
	        	break;
	
	        default :
	            break;
	    }
    	
    	return capabilities;
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
        if (SeleniumTestsContextManager.isDesktopWebTest()) {
        	capabilities = createCapabilityByBrowser(webDriverConfig, capabilities);
        } else if (SeleniumTestsContextManager.isMobileTest()) {
        	if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
        		capabilities = new AndroidCapabilitiesFactory(webDriverConfig).createCapabilities();
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	capabilities = new IOsCapabilitiesFactory(webDriverConfig).createCapabilities();
	        } else {
	        	throw new ConfigurationException(String.format("Platform %s is unknown for mobile tests", webDriverConfig.getPlatform()));
	        }
        } else {
        	throw new ConfigurationException("Remote driver is supported for mobile and desktop web tests");
        }
        
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
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout(), webDriverConfig.getBrowser());
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

    protected void setPageLoadTimeout(final long timeout, final BrowserType type) {
        switch (type) {

            case CHROME :
            case FIREFOX :
            case INTERNET_EXPLORER :
            	setPageLoadTimeoutCommonBrowser(timeout);
                break;

            default :
        }
    }
    
    protected void setPageLoadTimeoutCommonBrowser(final long timeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        } catch (UnsupportedCommandException e) {
        	logger.error(e);
        }
    }

	@Override
	protected WebDriver createNativeDriver() {
		return null;
	}
}

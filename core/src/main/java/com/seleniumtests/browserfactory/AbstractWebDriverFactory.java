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

import java.time.Duration;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class AbstractWebDriverFactory {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(AbstractWebDriverFactory.class);
    protected DriverConfig webDriverConfig;
    protected ICapabilitiesFactory capsFactory;
    protected BrowserInfo selectedBrowserInfo;
    protected MutableCapabilities driverOptions;
    protected WebDriver driver;

    protected AbstractWebDriverFactory(final DriverConfig cfg) {
        this.webDriverConfig = cfg;
        
        capsFactory = getCapabilitiesFactory();
        driverOptions = capsFactory.createCapabilities();
        
        // add user defined capabilities
        driverOptions = driverOptions.merge(webDriverConfig.getCapabilites());
        
        if (capsFactory instanceof IDesktopCapabilityFactory iDesktopCapabilityFactory) {
        	selectedBrowserInfo = iDesktopCapabilityFactory.getSelectedBrowserInfo();
        } else {
        	selectedBrowserInfo = new BrowserInfo(BrowserType.NONE, "0.0");
        }
    }

    public void cleanUp() {
        driver = null;
    }
    
	public BrowserInfo getSelectedBrowserInfo() {
		return selectedBrowserInfo;
	}
	
    protected abstract WebDriver createNativeDriver();
    
    protected abstract ICapabilitiesFactory getCapabilitiesFactory(); 
    
    public WebDriver createWebDriver() {

        driver = createNativeDriver();

        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0 && webDriverConfig.getTestType().family() == TestType.WEB) {
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout());
        }

        this.setWebDriver(driver);
        return driver;
    }

    protected void setPageLoadTimeout(final long timeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(timeout));
        } catch (WebDriverException e) {
            // chromedriver does not support pageLoadTimeout
        }
    }

    /**
     * Accessed by sub classes so that they don't have be declared abstract class.
     *
     * @return  driver instance
     */
    public WebDriver getWebDriver() {
        return driver;
    }

    public DriverConfig getWebDriverConfig() {
        return webDriverConfig;
    }

    public void setImplicitWaitTimeout(final double timeout) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds((int)timeout));
        } catch (Exception ex) {
        	logger.error(ex);
        }
    }

    public void setWebDriver(final WebDriver driver) {
        this.driver = driver;
    }

    public void setWebDriverConfig(final DriverConfig cfg) {
        this.webDriverConfig = cfg;
    }

	public MutableCapabilities getDriverOptions() {
		return driverOptions;
	}
}

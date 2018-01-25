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

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class AbstractWebDriverFactory {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(AbstractWebDriverFactory.class);
    protected DriverConfig webDriverConfig;
    protected ICapabilitiesFactory capsFactory;
    protected BrowserInfo selectedBrowserInfo;
    protected MutableCapabilities driverOptions;
    protected WebDriver driver;

    public AbstractWebDriverFactory(final DriverConfig cfg) {
        this.webDriverConfig = cfg;
        
        capsFactory = getCapabilitiesFactory();
        driverOptions = capsFactory.createCapabilities();
        
        if (capsFactory instanceof IDesktopCapabilityFactory) {
        	selectedBrowserInfo = ((IDesktopCapabilityFactory)capsFactory).getSelectedBrowserInfo();
        }
    }

    public void cleanUp() {
        if (driver != null) {
            try {
                TestLogging.log("quiting webdriver" + Thread.currentThread().getId());
                driver.quit();
            } catch (Exception ex) {
                TestLogging.log("Exception encountered when quiting driver:" + ex.getMessage());
            }

            driver = null;
        }
    }
    
	public BrowserInfo getSelectedBrowserInfo() {
		return selectedBrowserInfo;
	}
	
    protected abstract WebDriver createNativeDriver();
    
    protected abstract ICapabilitiesFactory getCapabilitiesFactory(); 
    
    public WebDriver createWebDriver() {

        driver = createNativeDriver();

        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0) {
            setPageLoadTimeout(webDriverConfig.getPageLoadTimeout());
        }

        this.setWebDriver(driver);
        return driver;
    }

    protected void setPageLoadTimeout(final long timeout) {
        try {
            driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
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
        if (timeout < 1) {
            driver.manage().timeouts().implicitlyWait((long) (timeout * 1000), TimeUnit.MILLISECONDS);
        } else {
            try {
                driver.manage().timeouts().implicitlyWait((int)timeout, TimeUnit.SECONDS);
            } catch (Exception ex) {
            	logger.error(ex);
            }
        }
    }

    public void setWebDriver(final WebDriver driver) {
        this.driver = driver;
    }

    public void setWebDriverConfig(final DriverConfig cfg) {
        this.webDriverConfig = cfg;
    }
}

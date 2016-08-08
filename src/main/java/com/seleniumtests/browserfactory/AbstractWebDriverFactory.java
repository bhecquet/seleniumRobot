/*
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.browserfactory;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;

public abstract class AbstractWebDriverFactory {

	protected static final Logger logger = TestLogging.getLogger(AbstractWebDriverFactory.class);
    protected DriverConfig webDriverConfig;

    protected WebDriver driver;

    public AbstractWebDriverFactory(final DriverConfig cfg) {
        this.webDriverConfig = cfg;
    }

    public void cleanUp() {
        try {
            if (driver != null) {
                try {
                    TestLogging.log("quiting webdriver" + Thread.currentThread().getId());
                    driver.quit();
                } catch (WebDriverException ex) {
                    TestLogging.log("Exception encountered when quiting driver: "
                            + WebUIDriver.getWebUIDriver().getConfig().getBrowser().name() + ":" + ex.getMessage());
                }

                driver = null;
            }
        } catch (Exception e) {
        	logger.error(e);
        }
    }
    
    protected abstract WebDriver createNativeDriver();
    
    public WebDriver createWebDriver() {
        final DriverConfig cfg = this.getWebDriverConfig();

        driver = createNativeDriver();

        setImplicitWaitTimeout(cfg.getImplicitWaitTimeout());
        if (cfg.getPageLoadTimeout() >= 0) {
            setPageLoadTimeout(cfg.getPageLoadTimeout());
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

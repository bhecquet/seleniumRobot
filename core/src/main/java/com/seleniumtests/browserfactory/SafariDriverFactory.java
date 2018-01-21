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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.osutility.OSUtility;

public class SafariDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    public SafariDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    @Override
    protected WebDriver createNativeDriver() {
    	synchronized (this.getClass()) {
    		SafariCapabilitiesFactory capsFactory = new SafariCapabilitiesFactory(webDriverConfig);
    		SafariOptions options = (SafariOptions)capsFactory.createCapabilities();
        	selectedBrowserInfo = capsFactory.getSelectedBrowserInfo();
    		
    		return new SafariDriver(options);
    	}
    }

    @Override
    public WebDriver createWebDriver() {
    	driver = createNativeDriver();

        this.setWebDriver(driver);

        // Implicit Waits handles dynamic element.
        setImplicitWaitTimeout(webDriverConfig.getImplicitWaitTimeout());
        if (webDriverConfig.getPageLoadTimeout() >= 0) {
            TestLogging.log("Safari doesn't support pageLoadTimeout");
        }

        return driver;
    }

}

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

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.seleniumtests.core.runner.CucumberRunner;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.OSUtility;

public class IEDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
	private static final Logger logger = TestLogging.getLogger(IEDriverFactory.class);

    public IEDriverFactory(final DriverConfig webDriverConfig1) {
        super(webDriverConfig1);
    }

    @Override
    public void cleanUp() {
        try {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (WebDriverException ex) {
                	logger.error(ex);
                }

                driver = null;
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
    
    protected WebDriver createNativeDriver() {
        return new InternetExplorerDriver(new IECapabilitiesFactory().createCapabilities(webDriverConfig));
    }

    @Override
    public WebDriver createWebDriver() throws IOException {

        // killProcess();
        if (!OSUtility.isWindows()) {
            throw new DriverExceptions("IE is only supported on windows");
        }

        return super.createWebDriver();
    }

    private void killProcess() {
        if (OSUtility.isWindows()) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM IEDriverServer.exe");
                Runtime.getRuntime().exec("taskkill /F /IM Iexplore.exe");
            } catch (IOException e) {
            	logger.error(e);
            }
        }
    }

}

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class AppiumDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {


    public AppiumDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    protected WebDriver createNativeDriver() throws MalformedURLException {
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();

        if(webDriverConfig.getPlatform().equalsIgnoreCase("android")) {
            return new AndroidDriver(new URL(webDriverConfig.getAppiumServerURL()), new AndroidCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
        } else if (webDriverConfig.getPlatform().equalsIgnoreCase("ios")){
            return new IOSDriver(new URL(webDriverConfig.getAppiumServerURL()), new IOsCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
        }

        return new RemoteWebDriver(new URL(webDriverConfig.getAppiumServerURL()), new SauceLabsCapabilitiesFactory().createCapabilities(webDriverConfig));

    }

    @Override
    public WebDriver createWebDriver() {
        final DriverConfig cfg = this.getWebDriverConfig();

        try {
            driver = createNativeDriver();
        } catch (final MalformedURLException me){
            throw new DriverExceptions("Problem with creating driver", me);
        }

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

}

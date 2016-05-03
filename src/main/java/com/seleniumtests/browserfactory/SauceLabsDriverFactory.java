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

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class SauceLabsDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {


    public SauceLabsDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    private DesiredCapabilities cloudSpecificCapabilities() {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability("app-package", webDriverConfig.getAppPackage());
        capabilities.setCapability("app-activity", webDriverConfig.getAppActivity());
        return capabilities;
    }

    protected WebDriver createNativeDriver() throws MalformedURLException {
    	
    	DesiredCapabilities capabilities;
    	// TODO: updload application on SauceLabs cloud
    	if (webDriverConfig.getTestType().equals(TestType.APPIUM_APP_ANDROID.getTestType()) 
    			|| webDriverConfig.getTestType().equals(TestType.APPIUM_APP_IOS.getTestType())) {
    		capabilities = cloudSpecificCapabilities();
    	} else {
    		capabilities = new DesiredCapabilities();
    	}

        if(webDriverConfig.getTestType().equals(TestType.APPIUM_WEB_ANDROID.getTestType())
        		|| webDriverConfig.getTestType().equals(TestType.APPIUM_APP_ANDROID.getTestType())){
            return new AndroidDriver(new URL(webDriverConfig.getCloudURL()), new AndroidCapabilitiesFactory(capabilities)
                    .createCapabilities(webDriverConfig));
        } else if (webDriverConfig.getTestType().equals(TestType.APPIUM_WEB_IOS.getTestType())
        		|| webDriverConfig.getTestType().equals(TestType.APPIUM_APP_IOS.getTestType())){
            return new IOSDriver(new URL(webDriverConfig.getCloudURL()), new IOsCapabilitiesFactory(capabilities)
                    .createCapabilities(webDriverConfig));
        }

        return new RemoteWebDriver(new URL(webDriverConfig.getCloudURL()), new SauceLabsCapabilitiesFactory()
                    .createCapabilities(webDriverConfig));

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

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

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
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
    
    @Override
    protected WebDriver createNativeDriver() {
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();

    	try {
	        if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
	            return new AndroidDriver(new URL(webDriverConfig.getAppiumServerURL()), new AndroidCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	            return new IOSDriver(new URL(webDriverConfig.getAppiumServerURL()), new IOsCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig));
	        }
	
	        return new RemoteWebDriver(new URL(webDriverConfig.getAppiumServerURL()), new SauceLabsCapabilitiesFactory().createCapabilities(webDriverConfig));
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

}

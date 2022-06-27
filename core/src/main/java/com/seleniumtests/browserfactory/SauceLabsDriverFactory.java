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

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class SauceLabsDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {


    public SauceLabsDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    @Override
    protected WebDriver createNativeDriver() {

    	try {
	        if(ICloudCapabilityFactory.ANDROID_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	            return new AndroidDriver(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	            
	        } else if (ICloudCapabilityFactory.IOS_PLATFORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	return new IOSDriver(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	            
	        } else {
	        	return new RemoteWebDriver(new URL(webDriverConfig.getHubUrl().get(0)), driverOptions);
	        }
	
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
        return new SauceLabsCapabilitiesFactory(webDriverConfig);
	}

}

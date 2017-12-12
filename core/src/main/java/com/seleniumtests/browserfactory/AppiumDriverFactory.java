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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.browserfactory.mobile.AppiumLauncher;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;

public class AppiumDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

	private AppiumLauncher appiumLauncher;

    public AppiumDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    private void extractAndroidDriver(DesiredCapabilities capabilities) {
    	String chromeDriverFile = (String)capabilities.getCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE);
		if (chromeDriverFile != null) {
			String driverPath;
			try {
				driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(chromeDriverFile));
				capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, driverPath);
			} catch (UnsupportedEncodingException e) {
				logger.error("cannot get driver path", e);
			}
			
		}
    }
    
    @Override
    protected WebDriver createNativeDriver() {
    	
    	// start appium
    	// TODO: may be useful to connect to an existing appium server
    	appiumLauncher = AppiumLauncherFactory.getInstance();
    	appiumLauncher.startAppium();
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	try {
	        if("android".equalsIgnoreCase(webDriverConfig.getPlatform())) {
	        	DesiredCapabilities androidCaps = new AndroidCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig);
	        	androidCaps = new MobileDeviceSelector().initialize().updateCapabilitiesWithSelectedDevice(androidCaps, webDriverConfig.getMode());
	        	extractAndroidDriver(androidCaps);
	            return new AndroidDriver<WebElement>(new URL(((LocalAppiumLauncher)appiumLauncher).getAppiumServerUrl()), androidCaps);
	            
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	        	DesiredCapabilities iosCaps = new IOsCapabilitiesFactory(capabilities).createCapabilities(webDriverConfig);
	        	iosCaps = new MobileDeviceSelector().initialize().updateCapabilitiesWithSelectedDevice(iosCaps, webDriverConfig.getMode());
	            return new IOSDriver<WebElement>(new URL(((LocalAppiumLauncher)appiumLauncher).getAppiumServerUrl()), iosCaps);
	            
	        } else {
	        	throw new ConfigurationException(String.format("Platform %s is unknown for Appium tests", webDriverConfig.getPlatform()));
	        }
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

	public AppiumLauncher getAppiumLauncher() {
		return appiumLauncher;
	}

}

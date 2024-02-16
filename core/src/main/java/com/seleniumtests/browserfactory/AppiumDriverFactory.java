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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;

import com.seleniumtests.browserfactory.mobile.AppiumLauncher;
import com.seleniumtests.browserfactory.mobile.ExistingAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.MobileDeviceSelector;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class AppiumDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

	private static final String ANDROID_PLATORM = "android";
	private AppiumLauncher appiumLauncher;

    public AppiumDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }
    
    private MutableCapabilities extractAndroidDriver(MutableCapabilities capabilities) {
		UiAutomator2Options androidOptions = new UiAutomator2Options(capabilities);
		Optional<String> chromeDriverFile = androidOptions.getChromedriverExecutable();
		if (chromeDriverFile.isPresent() &&  chromeDriverFile.get() != null) {
			String driverPath;
			try {
				driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(chromeDriverFile.get()));
				androidOptions.setChromedriverExecutable(driverPath);
			} catch (UnsupportedEncodingException e) {
				logger.error("cannot get driver path", e);
			}
			
		}
		return androidOptions;
    }
    
    @Override
    protected WebDriver createNativeDriver() {
    	
    	// start appium
    	appiumLauncher = AppiumLauncherFactory.getInstance();
    	appiumLauncher.startAppium();
    	
    	try {
    		MutableCapabilities capabilities = getMobileCapabilities();
	        if(ANDROID_PLATORM.equalsIgnoreCase(webDriverConfig.getPlatform())) {
	        	capabilities = extractAndroidDriver(capabilities);

	            return new AndroidDriver(new URL(appiumLauncher.getAppiumServerUrl()), capabilities);
	            
	        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
	            return new IOSDriver(new URL(appiumLauncher.getAppiumServerUrl()), capabilities);
	            
	        } else {
	        	throw new ConfigurationException(String.format("Platform %s is unknown for Appium tests", webDriverConfig.getPlatform()));
	        }
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

	private MutableCapabilities getMobileCapabilities() {
		MutableCapabilities capabilities;
		try {
			capabilities = new MobileDeviceSelector().initialize().updateCapabilitiesWithSelectedDevice(driverOptions, webDriverConfig.getMode());
		} catch (ConfigurationException e) {
			logger.warn(e.getMessage());

			// when connecting to an existing appium server that may be a remote appium, we cannot know which devices are started / connected
			// driverOptions should contain every capabilities needed to start the test
			if (appiumLauncher instanceof ExistingAppiumLauncher) {
				capabilities = new MutableCapabilities(driverOptions);
				
				if (webDriverConfig.getDeviceId() == null) {
					throw new ConfigurationException("'deviceId' option MUST be set as we are using a remote appium server");
				}
				
				if (capabilities.getCapability(CapabilityType.PLATFORM_NAME).toString().equalsIgnoreCase(ANDROID_PLATORM)) {
					UiAutomator2Options androidCaps = new UiAutomator2Options(capabilities);
					androidCaps.setDeviceName(webDriverConfig.getDeviceId());

					return androidCaps;
				} else { // iOS
					XCUITestOptions iosCaps = new XCUITestOptions(capabilities);
					iosCaps.setUdid(webDriverConfig.getDeviceId());

					return iosCaps;
					//capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + IOSMobileCapabilityType.XCODE_CONFIG_FILE, (String)null); // remove this capability as it may not be accurate

				}
			} else {
				throw e;
			}
		}
		return capabilities;
	}

	public AppiumLauncher getAppiumLauncher() {
		return appiumLauncher;
	}

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if(ANDROID_PLATORM.equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new AndroidCapabilitiesFactory(webDriverConfig);
            
        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new IOsCapabilitiesFactory(webDriverConfig);
            
        } else {
        	throw new ConfigurationException(String.format("Platform %s is unknown for Appium tests", webDriverConfig.getPlatform()));
        }
	}

}

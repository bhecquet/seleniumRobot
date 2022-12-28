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

import java.io.File;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

import io.appium.java_client.remote.MobileCapabilityType;

public abstract class IMobileCapabilityFactory extends ICapabilitiesFactory {
	

	protected IMobileCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	protected abstract String getAutomationName();
	
	protected abstract MutableCapabilities getSystemSpecificCapabilities();
	
	protected abstract MutableCapabilities getBrowserSpecificCapabilities();
	  
	@Override
    public MutableCapabilities createCapabilities() {

    	String app = webDriverConfig.getApp().trim();
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.AUTOMATION_NAME, getAutomationName());
    
    	if (app != null && !app.trim().isEmpty()) {
	    	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.FULL_RESET, webDriverConfig.isFullReset());
    	}
    	
    	// Set up version and device name else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	capabilities.setCapability(CapabilityType.PLATFORM_NAME, webDriverConfig.getPlatform());
    	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.PLATFORM_VERSION, webDriverConfig.getMobilePlatformVersion());
    	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.DEVICE_NAME, webDriverConfig.getDeviceName());
    	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.NEW_COMMAND_TIMEOUT, webDriverConfig.getNewCommandTimeout());
    	
    	// in case app has not been specified for cloud provider
        if (capabilities.getCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP) == null && app != null && !app.isEmpty()) {
        	
        	// in case of local file, give absolute path to file. For remote files (e.g: http://myapp.apk), it will be transmitted as is
        	if (new File(app).isFile()) {
        		app = new File(app).getAbsolutePath();
        	}
        	capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + MobileCapabilityType.APP, app.replace("\\", "/"));
        }
    	
    	// do not configure application and browser as they are mutualy exclusive
        if (app == null || app.isEmpty() && webDriverConfig.getBrowserType() != BrowserType.NONE) {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, webDriverConfig.getBrowserType().toString().toLowerCase());
        	capabilities.merge(getBrowserSpecificCapabilities());
        } else {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, (String)null);
        }
        
        // add node tags
        if (!webDriverConfig.getNodeTags().isEmpty() && webDriverConfig.getMode() == DriverMode.GRID) {
        	capabilities.setCapability(SeleniumRobotCapabilityType.NODE_TAGS, webDriverConfig.getNodeTags());
        }
        
        // add OS specific capabilities
        capabilities.merge(getSystemSpecificCapabilities());
        
        
        return capabilities;
    }

}

/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

import io.appium.java_client.remote.MobileCapabilityType;

public abstract class IMobileCapabilityFactory extends ICapabilitiesFactory {

	public IMobileCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	protected abstract String getAutomationName();
	
	protected abstract MutableCapabilities getSystemSpecificCapabilities();
	
	protected abstract MutableCapabilities getBrowserSpecificCapabilities();
	  
	@Override
    public MutableCapabilities createCapabilities() {

    	String app = webDriverConfig.getApp();
    	
    	DesiredCapabilities capabilities = new DesiredCapabilities();
    	capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, getAutomationName());
    
    	if (app != null && !"".equals(app.trim())) {
	    	if (webDriverConfig.isFullReset()) {
	    		capabilities.setCapability(MobileCapabilityType.FULL_RESET, "true");
	    	} else {
	    		capabilities.setCapability(MobileCapabilityType.FULL_RESET, "false");
	    	}
    	}
    	
    	// Set up version and device name else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, webDriverConfig.getPlatform());
    	capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, webDriverConfig.getMobilePlatformVersion());
    	capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, webDriverConfig.getDeviceName());
    	capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, webDriverConfig.getNewCommandTimeout());
    	
    	// in case app has not been specified for cloud provider
        if (capabilities.getCapability(MobileCapabilityType.APP) == null && app != null) {
        	capabilities.setCapability(MobileCapabilityType.APP, app.replace("\\", "/"));
        }
    	
    	// do not configure application and browser as they are mutualy exclusive
        if (app == null || "".equals(app.trim()) && webDriverConfig.getBrowser() != BrowserType.NONE) {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, webDriverConfig.getBrowser().toString().toLowerCase());
        	capabilities.merge(getBrowserSpecificCapabilities());
        } else {
        	capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
        }
        
        // add node tags
        if (webDriverConfig.getNodeTags().size() > 0 && webDriverConfig.getMode() == DriverMode.GRID) {
        	capabilities.setCapability(SeleniumRobotCapabilityType.NODE_TAGS, webDriverConfig.getNodeTags());
        }

        if (webDriverConfig.getExternalPrograms().size() > 0 && webDriverConfig.getMode() == DriverMode.GRID) {
        	capabilities.setCapability(SeleniumRobotCapabilityType.TOOLS, webDriverConfig.getExternalPrograms());
        }
        
        // add OS specific capabilities
        capabilities.merge(getSystemSpecificCapabilities());
        
        
        return capabilities;
    }

}

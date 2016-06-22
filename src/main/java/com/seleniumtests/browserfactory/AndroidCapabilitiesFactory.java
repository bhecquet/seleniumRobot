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

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.DriverConfig;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

/**
 * Sets Android capabilities.
 */
public class AndroidCapabilitiesFactory implements ICapabilitiesFactory {
	
	private DesiredCapabilities capabilities;
	
	public AndroidCapabilitiesFactory(DesiredCapabilities caps) {
		capabilities = caps;
	}
	
	public AndroidCapabilitiesFactory() {
		capabilities = new DesiredCapabilities();
	}

    public DesiredCapabilities createCapabilities(final DriverConfig cfg) {

    	DesiredCapabilities caps = new DesiredCapabilities(this.capabilities);
    	if (Integer.parseInt(cfg.getMobilePlatformVersion().substring(0, 1)) < 4) {
    		caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Selendroid");
    	} else {
    		caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Appium");
    	}
    	
    	caps.setCapability(MobileCapabilityType.FULL_RESET, "true");
    	caps.setCapability(MobileCapabilityType.PLATFORM_NAME, cfg.getPlatform());

        // Set up version and device name else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, cfg.getMobilePlatformVersion());
    	caps.setCapability(MobileCapabilityType.DEVICE_NAME, cfg.getDeviceName());

    	// in case app has not been specified for cloud provider
        String app = cfg.getApp();
        if (caps.getCapability("app") == null) {
        	caps.setCapability("app", app);
        }
        caps.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, cfg.getAppPackage());
        caps.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, cfg.getAppActivity());
        
        if (cfg.getAppWaitActivity() != null) {
        	caps.setCapability(AndroidMobileCapabilityType.APP_WAIT_ACTIVITY, cfg.getAppWaitActivity());
        }

        // do not configure application and browser as they are mutualy exclusive
        if (app != null && app.trim().equals("")) {
        	caps.setCapability(CapabilityType.BROWSER_NAME, cfg.getBrowser());
        }
        caps.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, cfg.getNewCommandTimeout());

        return caps;

    }
}

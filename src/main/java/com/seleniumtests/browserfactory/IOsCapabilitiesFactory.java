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

import com.seleniumtests.driver.DriverConfig;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class IOsCapabilitiesFactory implements ICapabilitiesFactory {
	
	private DesiredCapabilities capabilities;
	
	public IOsCapabilitiesFactory(DesiredCapabilities caps) {
		capabilities = caps;
	}
	
	public IOsCapabilitiesFactory() {
		capabilities = new DesiredCapabilities();
	}

    public DesiredCapabilities createCapabilities(final DriverConfig cfg) {
    	
    	DesiredCapabilities caps = new DesiredCapabilities(this.capabilities);
    	caps.setCapability("automationName", "Appium");
    	caps.setCapability("fullReset", "true");
    	caps.setCapability("platformName", cfg.getPlatform());

        // Set up version and device name else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	caps.setCapability("platformVersion", cfg.getMobilePlatformVersion());
    	caps.setCapability("deviceName", cfg.getDeviceName());

    	String app = cfg.getApp();
    	if (caps.getCapability("app") == null) {
        	caps.setCapability("app", app);
        }
    	caps.setCapability("appPackage", cfg.getAppPackage());
    	caps.setCapability("appActivity", cfg.getAppActivity());

    	// do not configure application and browser as they are mutualy exclusive
        if (app != null && app.trim().equals("")) {
        	caps.setCapability(CapabilityType.BROWSER_NAME, cfg.getBrowser());
        }
    	caps.setCapability("newCommandTimeout", cfg.getNewCommandTimeout());

        return caps;
    }
}

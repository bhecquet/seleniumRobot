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

import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

/**
 * Sets Android capabilities.
 */
public class AndroidCapabilitiesFactory extends IMobileCapabilityFactory {
	
	public AndroidCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected String getAutomationName() {
		if (webDriverConfig.getAutomationName() == null) {
    		return "UiAutomator2";
    	} else {
    		return webDriverConfig.getAutomationName();
    	}
	}

	@Override
	protected MutableCapabilities getSystemSpecificCapabilities() {

    	// automatically hide keyboard
//    	capabilities.setCapability(AndroidMobileCapabilityType.RESET_KEYBOARD, true);
//    	capabilities.setCapability(AndroidMobileCapabilityType.UNICODE_KEYBOARD, true);

		UiAutomator2Options options = new UiAutomator2Options()
				.setAppPackage(webDriverConfig.getAppPackage())
				.setAppActivity(webDriverConfig.getAppActivity())
				.setDeviceName(webDriverConfig.getDeviceName());

        if (webDriverConfig.getAppWaitActivity() != null) {
			options.setAppWaitActivity(webDriverConfig.getAppWaitActivity());
        }
        
        return options;
	}

	@Override
	protected MutableCapabilities getBrowserSpecificCapabilities() {

    	// set specific configuration for chrome
    	if (webDriverConfig.getBrowserType() == BrowserType.CHROME) {
    		return new ChromeCapabilitiesFactory(webDriverConfig).createMobileCapabilities(webDriverConfig);
    	} else {
    		return new MutableCapabilities();
    	}
	}
}

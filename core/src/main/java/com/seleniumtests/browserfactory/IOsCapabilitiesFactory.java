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

import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.IOSMobileCapabilityType;

public class IOsCapabilitiesFactory extends IMobileCapabilityFactory {

	public IOsCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected String getAutomationName() {
		if (webDriverConfig.getAutomationName() == null) {
			return "XCUITest"; // for iOS 10+
		} else {
			return webDriverConfig.getAutomationName();
		}
	}

	@Override
	protected MutableCapabilities getSystemSpecificCapabilities() {
		MutableCapabilities capabilities = new MutableCapabilities();

    	if (webDriverConfig.getMode() == DriverMode.LOCAL) {
    		capabilities.setCapability(SeleniumRobotCapabilityType.APPIUM_PREFIX + IOSMobileCapabilityType.XCODE_CONFIG_FILE, System.getenv("APPIUM_HOME") + "/node_modules/appium/node_modules/appium-xcuitest-driver/WebDriverAgent/xcodeConfigFile.xcconfig");
    	}
    	
    	return capabilities;
	}

	@Override
	protected MutableCapabilities getBrowserSpecificCapabilities() {
		return new MutableCapabilities();
	}
}

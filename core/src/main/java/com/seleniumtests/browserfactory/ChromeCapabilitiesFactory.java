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

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

import io.appium.java_client.remote.AndroidMobileCapabilityType;

public class ChromeCapabilitiesFactory extends IDesktopCapabilityFactory {

	public ChromeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	/**
	 * Create capabilities for mobile chrome
	 */
	public DesiredCapabilities createMobileCapabilities(final DriverConfig webDriverConfig) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		ChromeOptions options = new ChromeOptions();
        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
        options.addArguments("--disable-translate");

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
        }
        
        return capabilities;
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
		ChromeOptions options = new ChromeOptions();

        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
        options.addArguments("--disable-translate");
        
        if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting chrome in headless mode. Supported for chrome version >= 60");
	        options.addArguments("--headless");
	        options.addArguments("--window-size=1280,1024");
	        options.addArguments("--disable-gpu");
        }
        
        return options;
	}

	@Override
	protected String getDriverPath() {
		return webDriverConfig.getChromeDriverPath();
	}

	@Override
	protected String getDriverExeProperty() {
		return ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY;
	}

	@Override
	protected String getBrowserBinaryPath() {
		return webDriverConfig.getChromeBinPath();
	}

	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		((ChromeOptions)options).setBinary(selectedBrowserInfo.getPath());
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.CHROME;
	}
}

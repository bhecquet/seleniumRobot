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
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

public class IECapabilitiesFactory extends IDesktopCapabilityFactory {

    public IECapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
		InternetExplorerOptions options = new InternetExplorerOptions();
		
		options.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
        options.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        options.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");
        options.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);

        return options;
	}

	@Override
	protected String getDriverPath() {
		return webDriverConfig.getIeDriverPath();
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.INTERNET_EXPLORER;
	}

	@Override
	protected String getDriverExeProperty() {
		return InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY;
	}

	@Override
	protected String getBrowserBinaryPath() {
		return null;
	}

	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// nothing to do
	}
}

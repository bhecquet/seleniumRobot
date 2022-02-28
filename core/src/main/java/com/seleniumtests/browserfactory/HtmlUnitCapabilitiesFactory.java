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

import static org.openqa.selenium.remote.Browser.HTMLUNIT;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;

public class HtmlUnitCapabilitiesFactory extends IDesktopCapabilityFactory {

	public HtmlUnitCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
		return new DesiredCapabilities(HTMLUNIT.browserName(), "", Platform.ANY);
	}

	@Override
	protected String getDriverPath() {
		return null;
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.HTMLUNIT;
	}

	@Override
	protected String getDriverExeProperty() {
		return null;
	}

	@Override
	protected String getBrowserBinaryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// nothing to do
	}

	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// TODO Auto-generated method stub
		
	}
}

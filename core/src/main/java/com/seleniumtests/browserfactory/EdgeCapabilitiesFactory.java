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

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

public class EdgeCapabilitiesFactory extends IDesktopCapabilityFactory {

    public EdgeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
        if (!SystemUtils.IS_OS_WINDOWS_10 && webDriverConfig.getMode() == DriverMode.LOCAL) {
        	throw new ConfigurationException("Edge browser is only available on Windows 10");
        }
		return new EdgeOptions();
	}

	@Override
	protected String getDriverPath() {
		return webDriverConfig.getEdgeDriverPath();
	}

	@Override
	protected String getDriverExeProperty() {
		return EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY;
	}

	@Override
	protected String getBrowserBinaryPath() {
		return null;
	}

	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// TODO Auto-generated method stub
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.EDGE;
	} 
}

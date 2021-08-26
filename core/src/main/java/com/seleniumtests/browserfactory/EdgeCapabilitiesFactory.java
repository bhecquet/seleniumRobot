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

import java.nio.file.Paths;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriverService;

import com.microsoft.edge.seleniumtools.EdgeDriverService;
import com.microsoft.edge.seleniumtools.EdgeOptions;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

public class EdgeCapabilitiesFactory extends IDesktopCapabilityFactory {

    public EdgeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
        if (!OSUtility.isWindows10() && webDriverConfig.getMode() == DriverMode.LOCAL) {
        	throw new ConfigurationException("Edge browser is only available on Windows 10");
        }
        
        EdgeOptions options = new EdgeOptions();
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());
        

        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setLogging();
        }
        
		return options;
	}
	

	private void setLogging() {

    	// driver logging
    	if (webDriverConfig.getDebug().contains(DebugMode.DRIVER)) {
    		String edgeDriverLogPath = Paths.get(webDriverConfig.getOutputDirectory(), "edgedriver.log").toString();
        	System.setProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY, "true");
        	System.setProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY, edgeDriverLogPath);
        	logger.info("Edgedriver logs will be written to " + edgeDriverLogPath);
    	} else {
    		System.clearProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY);
    		System.clearProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY);
    	}
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
		((EdgeOptions)options).setBinary(selectedBrowserInfo.getPath());
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.EDGE;
	}

	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// TODO Auto-generated method stub
		
	} 
}

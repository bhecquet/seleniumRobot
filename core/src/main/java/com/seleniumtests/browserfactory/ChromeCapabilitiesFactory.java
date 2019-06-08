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

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;

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
        options.addArguments("--disable-web-security");
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        
        // TEST_MOBILE
        capabilities.setCapability(AndroidMobileCapabilityType.NATIVE_WEB_SCREENSHOT, true);
        // TEST_MOBILE
        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
        	
        	// driver logging
        	setLogging();
        }
        
        return capabilities;
	}
	
	private void setLogging() {

    	// driver logging
    	if (webDriverConfig.getDebug().contains(DebugMode.DRIVER)) {
    		String chromeDriverLogPath = Paths.get(webDriverConfig.getOutputDirectory(), "chromedriver.log").toString();
        	System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
        	System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, chromeDriverLogPath);
        	logger.info("Chromedriver logs will be written to " + chromeDriverLogPath);
    	} else {
    		// avoid keeping these properties as it breaks chrome during integration tests
    		System.clearProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY);
    		System.clearProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY);
    	}
	}
 
	@Override
	protected MutableCapabilities getDriverOptions() {
		ChromeOptions options = new ChromeOptions();

        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
        options.addArguments("--disable-translate");
        options.addArguments("--disable-web-security");
        options.addArguments("--no-sandbox");
        
        if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting chrome in headless mode. Supported for chrome version >= 60");
	        options.addArguments("--headless");
	        options.addArguments("--window-size=1280,1024");
	        options.addArguments("--disable-gpu");
        }
        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setLogging();
        }
        
        // extensions
        List<BrowserExtension> extensions = BrowserExtension.getExtensions(webDriverConfig.getTestContext().getConfiguration());
        if (!extensions.isEmpty()) {
        	options.addExtensions(extensions.stream()
        		.map(BrowserExtension::getExtensionPath)
        		.collect(Collectors.toList()));
        }
        
        if (webDriverConfig.getAttachExistingDriverPort() != null) {
        	options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + webDriverConfig.getAttachExistingDriverPort());
        }
        
        
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());
        
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

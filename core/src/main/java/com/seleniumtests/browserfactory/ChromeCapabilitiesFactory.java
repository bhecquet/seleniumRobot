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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;

import io.appium.java_client.remote.AndroidMobileCapabilityType;

public class ChromeCapabilitiesFactory extends IDesktopCapabilityFactory {

	private static final String USER_DATA_DIR_OPTION = "--user-data-dir=";

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
        options.addArguments("--disable-site-isolation-trials");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");

        if (webDriverConfig.getChromeOptions() != null) {
        	for (String option: webDriverConfig.getChromeOptions().split(" ")) {
        		options.addArguments(option);
        	}
        }
        
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
        options.addArguments("--disable-site-isolation-trials");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");
        
        if (webDriverConfig.getChromeOptions() != null) {
        	for (String option: webDriverConfig.getChromeOptions().split(" ")) {
        		options.addArguments(option);
        	}
        }
        
        if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting chrome in headless mode");
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
        } else {
        	 // issue #480: disable "restore pages" popup, but not when we attach an existing browser as it crashes driver (from invalid argument: cannot parse capability: goog:chromeOptions, from invalid argument: unrecognized chrome option: prefs)
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.exit_type", "Normal");
            options.setExperimentalOption("prefs", prefs);
            
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
		
        if (webDriverConfig.getChromeProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getChromeProfilePath()) && (webDriverConfig.getChromeProfilePath().contains("/") || webDriverConfig.getChromeProfilePath().contains("\\"))) {
        		((ChromeOptions)options).addArguments(USER_DATA_DIR_OPTION + webDriverConfig.getChromeProfilePath()); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Google\\Chrome\\User Data
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getChromeProfilePath())) {
        		((ChromeOptions)options).addArguments(USER_DATA_DIR_OPTION + selectedBrowserInfo.getDefaultProfilePath()); 
        	} else {
        		logger.warn(String.format("Chrome profile %s could not be set", webDriverConfig.getChromeProfilePath()));
        	}
        }
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.CHROME;
	}

	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		if (webDriverConfig.getChromeProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getChromeProfilePath()) && (webDriverConfig.getChromeProfilePath().contains("/") || webDriverConfig.getChromeProfilePath().contains("\\"))) {
        		((ChromeOptions)options).addArguments(USER_DATA_DIR_OPTION + webDriverConfig.getChromeProfilePath()); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Google\\Chrome\\User Data
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getChromeProfilePath())) {
        		options.setCapability(SeleniumRobotCapabilityType.CHROME_PROFILE, BrowserInfo.DEFAULT_BROWSER_PRODFILE);
        	} else {
        		logger.warn(String.format("Chrome profile %s could not be set", webDriverConfig.getChromeProfilePath()));
        	}
        }
	}
}

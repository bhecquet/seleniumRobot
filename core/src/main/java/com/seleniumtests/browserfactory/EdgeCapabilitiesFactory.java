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
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;

public class EdgeCapabilitiesFactory extends IDesktopCapabilityFactory {

	private static final String USER_DATA_DIR_OPTION = "--user-data-dir=";

    public EdgeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
        
        EdgeOptions options = new EdgeOptions();
        
        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
        
        options.addArguments("--remote-allow-origins=*"); // workaround for https://github.com/SeleniumHQ/selenium/issues/11750 on chrome >= 111 
        options.addArguments("--disable-translate");
        options.addArguments("--disable-web-security");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-site-isolation-trials");
        options.addArguments("--disable-features=IsolateOrigins,site-per-process");
        
        if (webDriverConfig.getEdgeOptions() != null) {
        	for (String option: webDriverConfig.getEdgeOptions().split(" ")) {
        		options.addArguments(option);
        	}
        }
        
        if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting edge in headless mode");
	        options.addArguments("--headless");
	        options.addArguments("--window-size=1280,1024");
	        options.addArguments("--disable-gpu");
        }

        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setLogging();
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
		
		if (webDriverConfig.getEdgeProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getEdgeProfilePath()) && (webDriverConfig.getEdgeProfilePath().contains("/") || webDriverConfig.getEdgeProfilePath().contains("\\"))) {
        		((EdgeOptions)options).addArguments(USER_DATA_DIR_OPTION + webDriverConfig.getEdgeProfilePath()); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Microsoft\\Edge\\User Data
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getEdgeProfilePath())) {
        		((EdgeOptions)options).addArguments(USER_DATA_DIR_OPTION + selectedBrowserInfo.getDefaultProfilePath()); 
        	} else {
        		logger.warn(String.format("Edge profile %s could not be set", webDriverConfig.getEdgeProfilePath()));
        	}
        }
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.EDGE;
	}

	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		if (webDriverConfig.getEdgeProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getEdgeProfilePath()) && (webDriverConfig.getEdgeProfilePath().contains("/") || webDriverConfig.getEdgeProfilePath().contains("\\"))) {
        		((EdgeOptions)options).addArguments(USER_DATA_DIR_OPTION + webDriverConfig.getEdgeProfilePath()); // e.g: C:\\Users\\MyUser\\AppData\\Local\\Microsoft\\Edge\\User Data
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getEdgeProfilePath())) {
        		options.setCapability(SeleniumRobotCapabilityType.EDGE_PROFILE, BrowserInfo.DEFAULT_BROWSER_PRODFILE);
        	} else {
        		logger.warn(String.format("Edge profile %s could not be set", webDriverConfig.getEdgeProfilePath()));
        	}
        }
		
	} 
}

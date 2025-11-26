/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.browserfactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.DebugMode;

public class EdgeCapabilitiesFactory extends ChromiumCapabilitiesFactory {

    public EdgeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}


	protected String getUserStartupOptions() {
		return webDriverConfig.getEdgeOptions();
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
        
        EdgeOptions options = new EdgeOptions();
		Map<String, Object> experimentalOptions = new HashMap<>();
        
        addChromiumDriverOptions(options);

		// configure options for file download
		if (webDriverConfig.getMode() == DriverMode.LOCAL && webDriverConfig.getAttachExistingDriverPort() == null) {
			// inspired by LocalNode.java
			Path downloadDir;
			try {
				downloadDir = Files.createDirectories(Paths.get(webDriverConfig.getDownloadOutputDirectory()));
				experimentalOptions.putAll(
						Map.of(
								"download.prompt_for_download",
								false,
								"download.default_directory",
								downloadDir.toAbsolutePath().toString(),
								"savefile.default_directory",
								downloadDir.toAbsolutePath().toString()));
			} catch (IOException e) {
				logger.error("Error creating 'downloads' directory: {}", e.getMessage());
			}
		} else {
			options.setEnableDownloads(true);
		}

        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setLogging();
        }
        
        if (webDriverConfig.getAttachExistingDriverPort() != null) {
        	options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + webDriverConfig.getAttachExistingDriverPort());
        } else {
        	 // issue #480: disable "restore pages" popup, but not when we attach an existing browser as it crashes driver (from invalid argument: cannot parse capability: goog:chromeOptions, from invalid argument: unrecognized chrome option: prefs)
			experimentalOptions.put("profile.exit_type", "Normal");
			if (!experimentalOptions.isEmpty()) {
				options.setExperimentalOption("prefs", experimentalOptions);
			}
        }

		enableBidi(options);
        
		return options;
	}
	

	private void setLogging() {

    	// driver logging
    	if (webDriverConfig.getDebug().contains(DebugMode.DRIVER)) {
    		String edgeDriverLogPath = Paths.get(webDriverConfig.getOutputDirectory(), "edgedriver.log").toString();
        	System.setProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY, "true");
        	System.setProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY, edgeDriverLogPath);
        	logger.info("Edgedriver logs will be written to {}", edgeDriverLogPath);
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
        		logger.warn("Edge profile {} could not be set", webDriverConfig.getEdgeProfilePath());
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
        		logger.warn("Edge profile {} could not be set", webDriverConfig.getEdgeProfilePath());
        	}
        }
		
	} 
}

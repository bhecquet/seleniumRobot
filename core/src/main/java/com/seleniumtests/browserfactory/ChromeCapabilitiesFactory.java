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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.android.options.context.SupportsChromedriverExecutableOption;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.logging.DebugMode;

public class ChromeCapabilitiesFactory extends IDesktopCapabilityFactory {

	private static final String USER_DATA_DIR_OPTION = "--user-data-dir=";

	public ChromeCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	/**
	 * Create capabilities for mobile chrome
	 */
	public MutableCapabilities createMobileCapabilities(final DriverConfig webDriverConfig) {
		UiAutomator2Options capabilities = new UiAutomator2Options();
		ChromeOptions options = new ChromeOptions();
        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }

		List<String> chromeOptions = new ArrayList<>(List.of("--disable-translate",
				"--disable-web-security",
				"--disable-site-isolation-trials",
				"--disable-search-engine-choice-screen",
				"--disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades",
				"--remote-allow-origins=*")); // workaround for https://github.com/SeleniumHQ/selenium/issues/11750 on chrome >= 111

		if (webDriverConfig.getChromeOptions() != null) {
			for (String option: webDriverConfig.getChromeOptions().split(" ")) {
				if (option.startsWith("++")) {
					chromeOptions.remove(option.replace("++", "--"));
				} else {
					chromeOptions.add(option);
				}
			}
		}
		options.addArguments(chromeOptions);
        
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        
        // TEST_MOBILE
		capabilities.setNativeWebScreenshot(true);
        // TEST_MOBILE
        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
			capabilities.setChromedriverExecutable(System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));

        	// driver logging
        	setLogging();
        }
        
        return new MutableCapabilities(capabilities);
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
		Map<String, Object> experientalOptions = new HashMap<>();

		// to enable BiDi. this creates a tab on the browser
		// options.setCapability("webSocketUrl", true);

        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
		List<String> chromeOptions = new ArrayList<>(List.of("--disable-translate",
				"--disable-web-security",
				"--no-sandbox",
				"--disable-site-isolation-trials",
				// https://github.com/GoogleChrome/chrome-launcher/blob/main/docs/chrome-flags-for-tools.md
				"--disable-search-engine-choice-screen",
				// list of features: https://chromium.googlesource.com/chromium/src/+/refs/heads/main/chrome/common/chrome_features.cc
				"--disable-features=IsolateOrigins,site-per-process,PrivacySandboxSettings4,HttpsUpgrades",
				// workaround for https://github.com/SeleniumHQ/selenium/issues/11750 on chrome >= 111
				"--remote-allow-origins=*"));


		if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting chrome in headless mode");
			chromeOptions.add("--headless");
			chromeOptions.add("--window-size=1280,1024");
			chromeOptions.add("--disable-gpu");
        }

		if (webDriverConfig.getChromeOptions() != null) {
			for (String option: webDriverConfig.getChromeOptions().split(" ")) {
				if (option.startsWith("++")) {
					chromeOptions.remove(option.replace("++", "--"));
				} else {
					chromeOptions.add(option);
				}
			}
		}

		// configure options for file download,
		// only when chrome is started by selenium. Else, we get 'unrecognized chrome option: prefs '
		if (webDriverConfig.getAttachExistingDriverPort() == null) {
			if (webDriverConfig.getMode() == DriverMode.LOCAL) {
				// inspired by LocalNode.java
				Path downloadDir;
				try {
					downloadDir = Files.createDirectories(Paths.get(webDriverConfig.getDownloadOutputDirectory()));
					experientalOptions.putAll(
							Map.of(
									"download.prompt_for_download",
									false,
									"download.default_directory",
									downloadDir.toAbsolutePath().toString(),
									"savefile.default_directory",
									downloadDir.toAbsolutePath().toString()));
				} catch (IOException e) {
					logger.error("Error creating 'downloads' directory: " + e.getMessage());
				}
			} else {
				options.setEnableDownloads(true);
			}
		}

		options.addArguments(chromeOptions);

        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setLogging();
        }

        // performance logging
		Map<String, Object> perfLogPrefs = new HashMap<>();
		//perfLogPrefs.put("traceCategories", "browser,devtools.timeline,devtools");
		perfLogPrefs.put("enableNetwork", true);
		perfLogPrefs.put("enablePage", true);
		options.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);

		// For Enabling performance Logs for WebPageTest
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
		options.setCapability("goog:loggingPrefs", logPrefs);

        
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
            experientalOptions.put("profile.exit_type", "Normal");
			if (!experientalOptions.isEmpty()) {
				options.setExperimentalOption("prefs", experientalOptions);
			}
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

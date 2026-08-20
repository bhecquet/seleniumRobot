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

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.firefox.ProfilesIni;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.StringUtility;
import com.seleniumtests.util.logging.DebugMode;

public class FirefoxCapabilitiesFactory extends IDesktopCapabilityFactory {
	
    public FirefoxCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	public static final String ALL_ACCESS = "allAccess";
    
	@Override
	protected MutableCapabilities getDriverOptions() {
		FirefoxOptions options = new FirefoxOptions();
		
        if (webDriverConfig.isHeadlessBrowser()) {
        	logger.info("setting firefox in headless mode. Supported for firefox version >= 56");
	        options.addArguments("-headless");
	        options.addArguments("--window-size=1280,1024");
	        options.addArguments("--width=1280");
	        options.addArguments("--height=1024");
        }

        options.setLogLevel(FirefoxDriverLogLevel.ERROR);
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());
        
        if (webDriverConfig.getDebug().contains(DebugMode.DRIVER)) {
        	options.setLogLevel(FirefoxDriverLogLevel.TRACE);
        }

		// enable BiDi
		options.setCapability("webSocketUrl", true);
        
        // handle https://bugzilla.mozilla.org/show_bug.cgi?id=1429338#c4 and https://github.com/mozilla/geckodriver/issues/789
        //options.setCapability("moz:useNonSpecCompliantPointerOrigin", true);
        return options;
	}
	
	@Override
	protected String getDriverPath() {
		return webDriverConfig.getGeckoDriverPath();
	}
	
	@Override
	protected String getBrowserBinaryPath() {
		return webDriverConfig.getFirefoxBinPath();
	}
	
	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.FIREFOX;
	}
	
	@Override
	protected String getDriverExeProperty() {
		return GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY;
	}
	
	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		((FirefoxOptions)options).setBinary(selectedBrowserInfo.getPath());
        configProfile((FirefoxOptions)options, webDriverConfig);
	}
	

    protected void configProfile(FirefoxOptions options, final DriverConfig webDriverConfig) {

        if (webDriverConfig.getUserAgentOverride() != null) {
        	// ISSUE #705 - In order to give the maximum of data available to customize the User Agent,
        	// we need to pass the testName which is not in the context at this moment
        	String testName = "";
			try {
				testName = TestNGResultUtils.getVisualTestName(webDriverConfig.getTestContext().getTestNGResult());
			} catch (Exception e) {
				testName = TestNGResultUtils.getTestName(webDriverConfig.getTestContext().getTestNGResult());
			}
			webDriverConfig.getTestContext().setAttribute(SeleniumTestsContext.TEST_NAME, testName);
        	options.addPreference("general.useragent.override", StringUtility.interpolateString(webDriverConfig.getUserAgentOverride(), webDriverConfig.getTestContext()));
        }

        if (webDriverConfig.getNtlmAuthTrustedUris() != null) {
            options.addPreference("network.automatic-ntlm-auth.trusted-uris", webDriverConfig.getNtlmAuthTrustedUris());
        }

        if (webDriverConfig.getDownloadOutputDirectory() != null && webDriverConfig.getMode() == DriverMode.LOCAL) {
            options.addPreference("browser.download.dir", webDriverConfig.getDownloadOutputDirectory());
            options.addPreference("browser.download.folderList", 2);
            options.addPreference("browser.download.manager.showWhenStarting", false);
            options.addPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
        }

        // fix permission denied issues
        options.addPreference("capability.policy.default.Window.QueryInterface", ALL_ACCESS);
        options.addPreference("capability.policy.default.Window.frameElement.get", ALL_ACCESS);
        options.addPreference("capability.policy.default.HTMLDocument.compatMode.get", ALL_ACCESS);
        options.addPreference("capability.policy.default.Document.compatMode.get", ALL_ACCESS);
        options.addPreference("dom.max_chrome_script_run_time", 0);
        options.addPreference("dom.max_script_run_time", 0);
    }

    protected synchronized FirefoxProfile getFirefoxProfile() {

        if (webDriverConfig.getFirefoxProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath()) && (webDriverConfig.getFirefoxProfilePath().contains("/") || webDriverConfig.getFirefoxProfilePath().contains("\\"))) {
        		return new FirefoxProfile(new File(webDriverConfig.getFirefoxProfilePath()));
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath())) {
        		ProfilesIni init=new ProfilesIni();
        		return init.getProfile("default");
        	} else {
        		logger.warn("Firefox profile {}} could not be set", webDriverConfig.getFirefoxProfilePath());
        	}
        }
        return new FirefoxProfile();
    }

    /**
     * Creates a default profile that may be overriden on selenium grid side if we specify a path or "default"
     */
	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		if (webDriverConfig.getFirefoxProfilePath() != null) {
        	if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath()) || webDriverConfig.getFirefoxProfilePath().contains("/") || webDriverConfig.getFirefoxProfilePath().contains("\\")) {
        		options.setCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE, webDriverConfig.getFirefoxProfilePath());
        		return;
        	} else {
        		logger.warn("Firefox profile {} could not be set", webDriverConfig.getFirefoxProfilePath());
        	}
        }

        configProfile((FirefoxOptions)options, webDriverConfig);
		
	}

}

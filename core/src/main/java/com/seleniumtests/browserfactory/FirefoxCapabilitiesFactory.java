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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.firefox.ProfilesIni;

import com.seleniumtests.browserfactory.customprofile.FireFoxProfileMarker;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.logging.DebugMode;

public class FirefoxCapabilitiesFactory extends IDesktopCapabilityFactory {
	
    public FirefoxCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	public static final String ALL_ACCESS = "allAccess";
	private static boolean isProfileCreated = false;
    private static Object lockProfile = new Object();
    
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
        
        // handle https://bugzilla.mozilla.org/show_bug.cgi?id=1429338#c4 and https://github.com/mozilla/geckodriver/issues/789
        options.setCapability("moz:useNonSpecCompliantPointerOrigin", true);
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
		
        FirefoxProfile profile = getFirefoxProfile();
        configProfile(profile, webDriverConfig);
        ((FirefoxOptions)options).setProfile(profile);
        
        // extensions
        List<BrowserExtension> extensions = BrowserExtension.getExtensions(webDriverConfig.getTestContext().getConfiguration());
        if (!extensions.isEmpty()) {
        	for (BrowserExtension ext: extensions) {
        		profile.addExtension(ext.getExtensionPath());
        		for (Entry<String, String> entry: ext.getOptions().entrySet()) {
        			profile.setPreference(entry.getKey(), entry.getValue());
        		}
        	}
        	
        }
	}
	

    protected void configProfile(final FirefoxProfile profile, final DriverConfig webDriverConfig) {
        profile.setAcceptUntrustedCertificates(webDriverConfig.isSetAcceptUntrustedCertificates());
        profile.setAssumeUntrustedCertificateIssuer(webDriverConfig.isSetAssumeUntrustedCertificateIssuer());

        if (webDriverConfig.getUserAgentOverride() != null) {
            profile.setPreference("general.useragent.override", webDriverConfig.getUserAgentOverride());
        }

        if (webDriverConfig.getNtlmAuthTrustedUris() != null) {
            profile.setPreference("network.automatic-ntlm-auth.trusted-uris", webDriverConfig.getNtlmAuthTrustedUris());
        }

        if (webDriverConfig.getBrowserDownloadDir() != null && webDriverConfig.getMode() == DriverMode.LOCAL) {
            profile.setPreference("browser.download.dir", webDriverConfig.getBrowserDownloadDir());
            profile.setPreference("browser.download.folderList", 2);
            profile.setPreference("browser.download.manager.showWhenStarting", false);
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                "application/octet-stream,text/plain,application/pdf,application/zip,text/csv,text/html");
        }

        // fix permission denied issues
        profile.setPreference("capability.policy.default.Window.QueryInterface", ALL_ACCESS);
        profile.setPreference("capability.policy.default.Window.frameElement.get", ALL_ACCESS);
        profile.setPreference("capability.policy.default.HTMLDocument.compatMode.get", ALL_ACCESS);
        profile.setPreference("capability.policy.default.Document.compatMode.get", ALL_ACCESS);
        profile.setPreference("dom.max_chrome_script_run_time", 0);
        profile.setPreference("dom.max_script_run_time", 0);
    }

    /**
     * extractDefaultProfile to a folder.
     *
     * @param   profilePath  The folder to store the profile
     *
     * @throws  IOException	 when profile file is not found
     */
    protected void extractDefaultProfile(final String profilePath) throws IOException {
        synchronized (lockProfile) {
            try {
                if (!isProfileCreated) {
                    logger.info("start create profile");
                    FileUtils.deleteDirectory(new File(profilePath));
                    FileUtility.extractJar(profilePath, FireFoxProfileMarker.class);
                }
            } catch (Exception ex) {
            	logger.error(ex);
            }
            isProfileCreated = true;
        }

        
    }

    protected synchronized FirefoxProfile getFirefoxProfile() {

        if (webDriverConfig.getFirefoxProfilePath() != null) {
        	if (!BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath()) && (webDriverConfig.getFirefoxProfilePath().contains("/") || webDriverConfig.getFirefoxProfilePath().contains("\\"))) {
        		return new FirefoxProfile(new File(webDriverConfig.getFirefoxProfilePath()));
        	} else if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath())) {
        		ProfilesIni init=new ProfilesIni();
        		return init.getProfile("default");
        	} else {
        		logger.warn(String.format("Firefox profile %s could not be set", webDriverConfig.getFirefoxProfilePath()));
        	}
        }
        return new FirefoxProfile();
    }

    /**
     * Creates a default profile that may be overriden on selenium grid side if we specify a path or "default"
     */
	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		FirefoxProfile profile = new FirefoxProfile();
		if (webDriverConfig.getFirefoxProfilePath() != null) {
        	if (BrowserInfo.DEFAULT_BROWSER_PRODFILE.equals(webDriverConfig.getFirefoxProfilePath()) || webDriverConfig.getFirefoxProfilePath().contains("/") || webDriverConfig.getFirefoxProfilePath().contains("\\")) {
        		options.setCapability(SeleniumRobotCapabilityType.FIREFOX_PROFILE, webDriverConfig.getFirefoxProfilePath());
        		return;
        	} else {
        		logger.warn(String.format("Firefox profile %s could not be set", webDriverConfig.getFirefoxProfilePath()));
        	}
        } 		
		
        configProfile(profile, webDriverConfig);
        ((FirefoxOptions)options).setProfile(profile);
		
	}

}

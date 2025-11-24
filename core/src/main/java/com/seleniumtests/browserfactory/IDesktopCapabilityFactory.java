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

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;
import static org.openqa.selenium.remote.CapabilityType.PLATFORM_NAME;
import static org.openqa.selenium.remote.CapabilityType.PROXY;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.CustomSeleniumTestsException;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.osutility.OSUtility;

public abstract class IDesktopCapabilityFactory extends ICapabilitiesFactory {

	protected BrowserInfo selectedBrowserInfo;
	
	protected IDesktopCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	/**
	 * In local mode, select the right browser info, depending on type, version and binary
	 * If several version are available, the highest is selected. This means that if betaVersion option is selected, we will get the beta browser.
	 * 
	 * @param browserType		the browser to select (chrome, firefox, ...)
	 * @param binPath			the browser binary to start, if specifically set by user through option. Else, installed browser will be used
	 * @param driverPath		user defined driver. if null, default driver will be selected depending on browser and version
	 * @param version			user defined version if user needs to start a specific version of the browser and this version is installed
	 * @return
	 */
    private BrowserInfo prepareBinaryAndDriver(final BrowserType browserType, final String binPath, final String driverPath, final String version) {

    	// automatic list from OS + binary added as launch option (see SeleniumTestsContext.updateInstalledBrowsers())
    	List<BrowserInfo> browserInfos = OSUtility.getInstalledBrowsersWithVersion(webDriverConfig.getBetaBrowser()).get(browserType);

		if (version != null) {
    		selectedBrowserInfo = BrowserInfo.getInfoFromVersion(version, browserInfos);
    	} else if (binPath != null) {
    		selectedBrowserInfo = BrowserInfo.getInfoFromBinary(binPath, browserInfos);
    		logger.info("Using user defined browser binary from: {}", selectedBrowserInfo.getPath());
    	} else  {
			for (BrowserInfo browser : browserInfos) {
				if (webDriverConfig.getBetaBrowser().equals(browser.getBeta())) {
					selectedBrowserInfo = browser;
				}
			}
		}
		
		if (selectedBrowserInfo == null ) {
			throw new ConfigurationException(String.format("Browser %s %s is not available",
					webDriverConfig.getBrowserType(), Boolean.TRUE.equals(webDriverConfig.getBetaBrowser()) ? "beta" : ""));
		}

		// in case of legacy firefox driverFileName is null
    	String newDriverPath = new DriverExtractor().extractDriver(selectedBrowserInfo.getDriverFileName());
    	if (driverPath != null) {
    		newDriverPath = driverPath;
    		logger.info("using user defined driver from: {}", driverPath);
    	}
    	if (newDriverPath != null) {
    		System.setProperty(getDriverExeProperty(), newDriverPath);
    		
    		if (!OSUtility.isWindows() && !new File(newDriverPath).setExecutable(true)) {
                logger.error("Error setting executable on driver {}", newDriverPath);
            }
    	}
		
        return selectedBrowserInfo;
    }

	public BrowserInfo getSelectedBrowserInfo() {
		return selectedBrowserInfo;
	}

    public MutableCapabilities createCapabilities() {
    	MutableCapabilities options = getDriverOptions();
    	options = updateDefaultCapabilities(options);

        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
			prepareBinaryAndDriver(getBrowserType(),
					getBrowserBinaryPath(), 
					getDriverPath(),
					webDriverConfig.getBrowserVersion());
			
			updateOptionsWithSelectedBrowserInfo(options);
        } else if (webDriverConfig.getMode() == DriverMode.GRID) {
        	// add node tags
            if (!webDriverConfig.getNodeTags().isEmpty()) {
            	options.setCapability(SeleniumRobotCapabilityType.NODE_TAGS, webDriverConfig.getNodeTags());
            }
            options.setCapability(SeleniumRobotCapabilityType.BETA_BROWSER, webDriverConfig.getBetaBrowser());

            updateGridOptionsWithSelectedBrowserInfo(options);
        }

        if (webDriverConfig.getTestContext() != null && webDriverConfig.getTestContext().getTestNGResult() != null) {
        	String testName = TestNGResultUtils.getTestName(webDriverConfig.getTestContext().getTestNGResult());
            options.setCapability(SeleniumRobotCapabilityType.TEST_NAME, testName);
            options.setCapability(SeleniumRobotCapabilityType.STARTED_BY, webDriverConfig.getStartedBy());
        }
		// enable BiDi
		//options.setCapability("webSocketUrl", true);
     
        return options;
    }
    
    protected abstract MutableCapabilities getDriverOptions();
    
    protected abstract String getDriverPath();
    
    protected abstract BrowserType getBrowserType();
    
    protected abstract String getDriverExeProperty();
    
    protected abstract String getBrowserBinaryPath();
    
    protected abstract void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options);
    
    protected abstract void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options);
 
    private MutableCapabilities updateDefaultCapabilities(MutableCapabilities options) {

        // HTMLUnit does not provide AbstractDriverOptions
    	if (webDriverConfig.getBrowserType() == BrowserType.HTMLUNIT) {
    		if (webDriverConfig.getWebPlatform() != null) {
    			options.setCapability(PLATFORM_NAME, webDriverConfig.getWebPlatform().toString());
    		}
    		options.setCapability(ACCEPT_INSECURE_CERTS, webDriverConfig.isSetAcceptUntrustedCertificates());
    		
    	} else {
    		// ACCEPT_INSECURE_CERTS is not permitted for IE
	        if (webDriverConfig.getBrowserType() != BrowserType.INTERNET_EXPLORER) {
	        	((AbstractDriverOptions<?>)options).setAcceptInsecureCerts(webDriverConfig.isSetAcceptUntrustedCertificates());
	        } else {
	        	((AbstractDriverOptions<?>)options).setAcceptInsecureCerts(false);
	        }
	
	        if (webDriverConfig.getBrowserVersion() != null) {
	        	((AbstractDriverOptions<?>)options).setBrowserVersion(webDriverConfig.getBrowserVersion());
	        }
	
	        if (webDriverConfig.getWebPlatform() != null) {
	        	((AbstractDriverOptions<?>)options).setPlatformName(webDriverConfig.getWebPlatform().toString());
	        }
    	}

        configureProxyCap(options);
        
        return options;
    }  
    
    /**
     * Add proxy capability
     * @param capability
     */
    private void configureProxyCap(MutableCapabilities capability) {
    	Proxy proxy = webDriverConfig.getProxy();
		if (proxy != null) {
        	capability.setCapability(PROXY, Require.nonNull("Proxy", proxy));
        }
    }

	protected Path copyDefaultProfile(BrowserInfo browserInfo) {
		Path tempProfile = SeleniumTestsContextManager.getProfilesPath().resolve(browserInfo.getBrowser().name()).resolve(browserInfo.getBeta() ? "Beta" : "Release");

		if (tempProfile.toFile().exists()) {
			if (Instant.ofEpochMilli(tempProfile.toFile().lastModified()).plus(12, ChronoUnit.HOURS).isAfter(Instant.now())) {
				logger.info("{} has been modified in the last 12 hours, do not copy default profile", tempProfile);
				return tempProfile;
			} else {
				logger.info("{} profile folder is older than 12 hours recreate it", tempProfile);
				FileUtils.deleteQuietly(tempProfile.toFile());
			}
		}

		try {
			logger.info("Copying default profile to {}", tempProfile);

			Files.createDirectories(tempProfile);
			FileUtils.copyDirectory(new File(browserInfo.getDefaultProfilePath()), tempProfile.toFile());
		} catch (IOException e) {
			throw new CustomSeleniumTestsException(String.format("Cannot create profile directory: %s", tempProfile), e);
		}


		return tempProfile;
	}
}

/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.osutility.OSUtility;

import io.appium.java_client.remote.AndroidMobileCapabilityType;

public class ChromeCapabilitiesFactory extends ICapabilitiesFactory {

	@Override
    public DesiredCapabilities createCapabilities(final DriverConfig webDriverConfig) {

        DesiredCapabilities capability = DesiredCapabilities.chrome();
        capability = updateDefaultCapabilities(capability, webDriverConfig);
        capability.setBrowserName(DesiredCapabilities.chrome().getBrowserName());

        ChromeOptions options = new ChromeOptions();
        if (webDriverConfig.getUserAgentOverride() != null) {
            options.addArguments("--user-agent=" + webDriverConfig.getUserAgentOverride());
        }
        options.addArguments("--disable-translate");

        capability.setCapability(ChromeOptions.CAPABILITY, options);

        if (webDriverConfig.getChromeBinPath() != null) {
            capability.setCapability("chrome.binary", webDriverConfig.getChromeBinPath());
        }

        // Set ChromeDriver for local mode
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setChromeDriverLocal(webDriverConfig);
        }

        return capability;
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

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setChromeDriverLocal(webDriverConfig);
        	capabilities.setCapability(AndroidMobileCapabilityType.CHROMEDRIVER_EXECUTABLE, System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
        }
        
        return capabilities;
	}

    public void handleExtractResources() throws IOException {
    	BrowserInfo browserInfo = OSUtility.getInstalledBrowsersWithVersion().get(BrowserType.CHROME);
    	String driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(browserInfo.getDriverFileName()));
    	System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, driverPath);
        if (!OSUtility.isWindows()) {
            new File(driverPath).setExecutable(true);
        }
    }
    
    /**
     * Set ChromeDriver for local mode
     * @param webDriverConfig
     */
    public void setChromeDriverLocal(final DriverConfig webDriverConfig){
        String chromeDriverPath = webDriverConfig.getChromeDriverPath();
        if (chromeDriverPath == null) {
            try {
                if (System.getenv(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY) != null) {
                    logger.info("get Chrome driver from property:" 
                    			+ System.getenv(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
                    System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, System.getenv(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
                } else {
                    handleExtractResources();
                }
            } catch (IOException ex) {
            	logger.error(ex);
            }
        } else {
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverPath);
        }
    }

}

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

import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.osutility.OSUtility;

public class MarionetteCapabilitiesFactory extends FirefoxCapabilitiesFactory {
	
	@Override
	public DesiredCapabilities createCapabilities(final DriverConfig webDriverConfig) {
		DesiredCapabilities capabilities = super.createCapabilities(webDriverConfig);
		capabilities.setCapability("marionette", true);
		
		if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setGeckoDriverLocal(webDriverConfig);
        }
		
		return capabilities;
		
	}
	
	private void handleExtractResources() throws IOException {
		BrowserInfo browserInfo = OSUtility.getInstalledBrowsersWithVersion().get(BrowserType.FIREFOX);
    	String driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(browserInfo.getDriverFileName()));
    	System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, driverPath);
		
		if (!OSUtility.isWindows()) {
            new File(driverPath).setExecutable(true);
        }
	}
	
	/**
     * Set ChromeDriver for local mode
     * @param webDriverConfig
     */
    public void setGeckoDriverLocal(final DriverConfig webDriverConfig){
        String edgeDriverPath = webDriverConfig.getGeckoDriverPath();
        if (edgeDriverPath == null) {
            try {
                if (System.getenv(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY) != null) {
                    logger.info("get gecko driver from property:" + System.getenv(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY));
                    System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, System.getenv(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY));
                } else {
                    handleExtractResources();
                }
            } catch (IOException ex) {
            	logger.error(ex);
            }
        } else {
            System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, edgeDriverPath);
        }
    }
    
}

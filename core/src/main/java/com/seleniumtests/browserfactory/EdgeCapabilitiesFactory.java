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

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class EdgeCapabilitiesFactory extends ICapabilitiesFactory {

	private static final String EDGE_DRIVER_PROPERTY = "webdriver.edge.driver";
	
    /**
     * Create edge capabilities.
     */
    @Override
    public DesiredCapabilities createCapabilities(final DriverConfig webDriverConfig) {
        DesiredCapabilities capability = DesiredCapabilities.edge();
        
        if (!SystemUtils.IS_OS_WINDOWS_10 && webDriverConfig.getMode() == DriverMode.LOCAL) {
        	throw new ConfigurationException("Edge browser is only available on Windows 10");
        }

        if (webDriverConfig.isEnableJavascript()) {
            capability.setJavascriptEnabled(true);
        } else {
            capability.setJavascriptEnabled(false);
        }

        capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        if (webDriverConfig.getBrowserVersion() != null) {
            capability.setVersion(webDriverConfig.getBrowserVersion());
        }

        if (webDriverConfig.getWebPlatform() != null) {
            capability.setPlatform(webDriverConfig.getWebPlatform());
        }

        capability.setCapability(CapabilityType.PROXY, webDriverConfig.getProxy());
        
        // Set Edge for local mode
        if (webDriverConfig.getMode() == DriverMode.LOCAL) {
        	setEdgeDriverLocal(webDriverConfig);
        }

        return capability;
    }
    
    
    
    /**
     * Get Windows version as Edge driver is tied to it
     * @throws IOException
     */
    public void handleExtractResources() throws IOException {
    	BrowserInfo browserInfo = OSUtility.getInstalledBrowsersWithVersion().get(BrowserType.EDGE);
    	String driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(browserInfo.getDriverFileName()));
    	System.setProperty(EDGE_DRIVER_PROPERTY, driverPath);
    }
    
    /**
     * Set ChromeDriver for local mode
     * @param webDriverConfig
     */
    public void setEdgeDriverLocal(final DriverConfig webDriverConfig){
        String edgeDriverPath = webDriverConfig.getEdgeDriverPath();
        if (edgeDriverPath == null) {
            try {
                if (System.getenv(EDGE_DRIVER_PROPERTY) != null) {
                    logger.info("get edge driver from property:" + System.getenv(EDGE_DRIVER_PROPERTY));
                    System.setProperty(EDGE_DRIVER_PROPERTY, System.getenv(EDGE_DRIVER_PROPERTY));
                } else {
                    handleExtractResources();
                }
            } catch (IOException ex) {
            	logger.error(ex);
            }
        } else {
            System.setProperty(EDGE_DRIVER_PROPERTY, edgeDriverPath);
        }
    }

 
}

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

import org.openqa.selenium.Proxy;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverExtractor;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.osutility.OSUtility;

public class IECapabilitiesFactory extends ICapabilitiesFactory {
    
	private static final String WEBDRIVER_PROPERTY = "webdriver.ie.driver";

    public void handleExtractResources() throws IOException {
    	BrowserInfo browserInfo = OSUtility.getInstalledBrowsersWithVersion().get(BrowserType.INTERNET_EXPLORER);
    	String driverPath = FileUtility.decodePath(new DriverExtractor().extractDriver(browserInfo.getDriverFileName()));
    	System.setProperty(WEBDRIVER_PROPERTY, driverPath);
    	
//    	String driverPath;
//    	if (osUtil.getIEVersion() < 10) {
//    		driverPath = new DriverExtractor().extractDriver("IEDriverServer_x64");
//    	} else {
//    		driverPath = new DriverExtractor().extractDriver("IEDriverServer_Win32");
//    	}
//    	
//        File iEDriverServerFile = Paths.get(new File(driverPath).getParent(), "IEDriverServer.exe").toFile();
//
//        try {
//        	FileUtils.copyFile(new File(driverPath), iEDriverServerFile);
//        } catch (IOException e) {
//        	// do nothing, the file may already be in use
//        }

        
    }
    
    /**
     * Set IEDriver for Local Mode for local mode
     * @param webDriverConfig
     */
    public void setIEDriverLocal(final DriverConfig webDriverConfig){
    	
    	// an other driver has been configured through command line
    	if (webDriverConfig.getIeDriverPath() != null) {
            System.setProperty(WEBDRIVER_PROPERTY, webDriverConfig.getIeDriverPath());
        } else {
        	// an other driver has been configured in environment
            if (System.getenv(WEBDRIVER_PROPERTY) != null) {
                logger.info("Get IE Driver from property:" + System.getenv(WEBDRIVER_PROPERTY));
                System.setProperty(WEBDRIVER_PROPERTY, System.getenv(WEBDRIVER_PROPERTY));
            } else {
                try {
                    handleExtractResources();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public DesiredCapabilities createCapabilities(final DriverConfig cfg) {

        if (cfg.getMode() == DriverMode.LOCAL) {
        	setIEDriverLocal(cfg);
        }

        DesiredCapabilities capability = DesiredCapabilities.internetExplorer();

        capability.setBrowserName(DesiredCapabilities.internetExplorer().getBrowserName());

        if (cfg.isEnableJavascript()) {
            capability.setJavascriptEnabled(true);
        } else {
            capability.setJavascriptEnabled(false);
        }

        capability.setCapability(CapabilityType.TAKES_SCREENSHOT, true);
        capability.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        capability.setCapability("ignoreZoomSetting", true);
        
        capability.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
        capability.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        capability.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");

        if (cfg.getBrowserVersion() != null) {
            capability.setVersion(cfg.getBrowserVersion());
        }

        if (cfg.getWebPlatform() != null) {
            capability.setPlatform(cfg.getWebPlatform());
        }

        Proxy proxy = cfg.getProxy();
        capability.setCapability(CapabilityType.PROXY, proxy);

        return capability;
    }
}

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

import java.util.List;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.logging.DebugMode;
import com.seleniumtests.util.osutility.OSUtility;

public class IECapabilitiesFactory extends IDesktopCapabilityFactory {

    private static final String SE_IE_OPTIONS = "se:ieOptions";

	public IECapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
	protected MutableCapabilities getDriverOptions() {
		InternetExplorerOptions options = new InternetExplorerOptions();
		
		
		options.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
        options.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        options.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
        
//        options.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, true);
//        options.setCapability(InternetExplorerDriver.NATIVE_EVENTS, true);
        
        if (webDriverConfig.getDebug().contains(DebugMode.DRIVER)) {
        	options.setCapability(InternetExplorerDriver.LOG_LEVEL, "TRACE");
        }
        
        if (webDriverConfig.getAttachExistingDriverPort() != null) {
	        options.setCapability("attachExistingBrowser", true);
			((Map<String, Object>) options.getCapability(SE_IE_OPTIONS)).put("attachExistingBrowser", true);
        }
        
        if (Boolean.TRUE.equals(webDriverConfig.getIeMode())) {
        	List<BrowserInfo> edgeBrowserInfos = OSUtility.getInstalledBrowsersWithVersion(webDriverConfig.getBetaBrowser()).get(BrowserType.EDGE);
        	if (edgeBrowserInfos.isEmpty()) {
        		throw new ConfigurationException("Edge not available");
        	}
        	
        	// put in both location as Selenium3 does not handle edge chromium properly
        	((Map<String, Object>) options.getCapability(SE_IE_OPTIONS)).put("ie.edgechromium", true);
	        options.setCapability("ie.edgechromium", true); 
        	((Map<String, Object>) options.getCapability(SE_IE_OPTIONS)).put("ie.edgepath", edgeBrowserInfos.get(0).getPath());
	        options.setCapability("ie.edgepath", edgeBrowserInfos.get(0).getPath());
	        options.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, webDriverConfig.getInitialUrl());
        } else {
        	// initial URL is not set for Edge in IE mode, as about:blank is not a real URL and this prevent driver from finding the IE window
        	options.setCapability(InternetExplorerDriver.INITIAL_BROWSER_URL, "about:blank");
        }
		
        options.setPageLoadStrategy(webDriverConfig.getPageLoadStrategy());

        return options;
	}

	@Override
	protected String getDriverPath() {
		return webDriverConfig.getIeDriverPath();
	}

	@Override
	protected BrowserType getBrowserType() {
		return BrowserType.INTERNET_EXPLORER;
	}

	@Override
	protected String getDriverExeProperty() {
		return InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY;
	}

	@Override
	protected String getBrowserBinaryPath() {
		return null;
	}

	@Override
	protected void updateOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// nothing to do
	}

	@Override
	protected void updateGridOptionsWithSelectedBrowserInfo(MutableCapabilities options) {
		// TODO Auto-generated method stub
		
	}
}

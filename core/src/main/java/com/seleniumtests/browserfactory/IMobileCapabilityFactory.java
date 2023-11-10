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
import java.time.Duration;
import java.util.Optional;

import io.appium.java_client.remote.options.BaseOptions;
import io.appium.java_client.remote.options.SupportsAppOption;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.utils.TestNGResultUtils;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;

public abstract class IMobileCapabilityFactory extends ICapabilitiesFactory {
	

	protected IMobileCapabilityFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}
	
	protected abstract String getAutomationName();
	
	protected abstract MutableCapabilities getSystemSpecificCapabilities();
	
	protected abstract MutableCapabilities getBrowserSpecificCapabilities();
	  
	@Override
    public MutableCapabilities createCapabilities() {

    	String app = webDriverConfig.getApp().trim();
    	
    	BaseOptions options = new BaseOptions();
    	options.setAutomationName(getAutomationName());
    
    	if (app != null && !app.trim().isEmpty()) {
	    	options.setFullReset(webDriverConfig.isFullReset());
    	}
    	
    	// Set up version (device name is set on specific capability factories) else appium server would pick the only available emulator/device
        // Both of these are ignored for android for now
    	options.setPlatformName(webDriverConfig.getPlatform());
    	options.setPlatformVersion(webDriverConfig.getMobilePlatformVersion());
    	options.setNewCommandTimeout(Duration.ofSeconds(webDriverConfig.getNewCommandTimeout()));
    	
    	// in case app has not been specified for cloud provider
		Optional<String> applicationOption = ((SupportsAppOption)options).getApp();
        if (applicationOption.isPresent() && applicationOption.get() == null && app != null && !app.isEmpty()) {
        	
        	// in case of local file, give absolute path to file. For remote files (e.g: http://myapp.apk), it will be transmitted as is
        	if (new File(app).isFile()) {
        		app = new File(app).getAbsolutePath();
        	}
			((SupportsAppOption)options).setApp(app.replace("\\", "/"));
        }
        
        if (webDriverConfig.getTestContext() != null && webDriverConfig.getTestContext().getTestNGResult() != null) {
        	String testName = TestNGResultUtils.getTestName(webDriverConfig.getTestContext().getTestNGResult());
        	options.setCapability(SeleniumRobotCapabilityType.TEST_NAME, testName);
        	options.setCapability(SeleniumRobotCapabilityType.STARTED_BY, webDriverConfig.getStartedBy());
        }
    	
    	// do not configure application and browser as they are mutualy exclusive
        if (app == null || app.isEmpty() && webDriverConfig.getBrowserType() != BrowserType.NONE) {
        	options.setCapability(CapabilityType.BROWSER_NAME, webDriverConfig.getBrowserType().toString().toLowerCase());
        	options.merge(getBrowserSpecificCapabilities());
        } else {
        	options.setCapability(CapabilityType.BROWSER_NAME, (String)null);
        }
        
        // add node tags
        if (!webDriverConfig.getNodeTags().isEmpty() && webDriverConfig.getMode() == DriverMode.GRID) {
        	options.setCapability(SeleniumRobotCapabilityType.NODE_TAGS, webDriverConfig.getNodeTags());
        }
        
        // add OS specific capabilities
        options.merge(getSystemSpecificCapabilities());
        
        // add user configurations
        options.merge(webDriverConfig.getAppiumCapabilities());
        
        
        return options;
    }

}

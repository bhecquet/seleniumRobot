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

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * Class for launching test on browserstack
 * 
 * TODO: download video recording, get network logs; 
 * 
 * @author s047432
 *
 */
public class BrowserStackDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    public BrowserStackDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }


    /**
     * Add / update capabilities to be compatible with browserstack requirements
     * @return
     */
    private DesiredCapabilities createCapabilities(){
    	
		DesiredCapabilities capabilities = new DesiredCapabilities();
		
		if (SeleniumTestsContextManager.isMobileTest()) {
			capabilities.setCapability("browserName", webDriverConfig.getBrowserType() == BrowserType.SAFARI ? "iPhone": "android"); 
			capabilities.setCapability("device", webDriverConfig.getDeviceName()); // pour deviceName
			capabilities.setCapability("realMobile", "true");
			capabilities.setCapability("os_version", webDriverConfig.getMobilePlatformVersion());
			capabilities.setCapability("os", webDriverConfig.getPlatform());
		} 
           
		capabilities.setCapability("name", SeleniumTestsContextManager.getThreadContext().getTestMethodSignature());
        capabilities.setCapability("project", SeleniumTestsContextManager.getApplicationName());
        capabilities.setCapability("browserstack.selenium_version", SeleniumTestsContextManager.SELENIUM_VERSION);

    	return capabilities;
    }
    
    @Override
    protected WebDriver createNativeDriver() {
    	
    	MutableCapabilities capabilities = createCapabilities();
    	capabilities.merge(driverOptions);

    	try {
	        return new RemoteWebDriver(new URL(webDriverConfig.getHubUrl().get(0)), capabilities);
    	} catch (MalformedURLException e) {
    		throw new DriverExceptions("Error creating driver: " + e.getMessage());
    	}
    }

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		if (webDriverConfig.getTestType().family().equals(TestType.APP)) {
			throw new ConfigurationException("Mobile app testing is not supported on browserstack");
		}
		
		if("android".equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new AndroidCapabilitiesFactory(webDriverConfig);
            
        } else if ("ios".equalsIgnoreCase(webDriverConfig.getPlatform())){
        	return new IOsCapabilitiesFactory(webDriverConfig);
            
        } else {
        	return new BrowserStackCapabilitiesFactory(webDriverConfig);
        }
	}

}

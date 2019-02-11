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

import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverConfig;

public class BrowserStackCapabilitiesFactory extends ICapabilitiesFactory {
	
    public BrowserStackCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
    public DesiredCapabilities createCapabilities() {

        final DesiredCapabilities capabilities = new DesiredCapabilities();

        // platform must be the concatenation of 'os' and 'os_version' that browserstack undestands
        String platform = webDriverConfig.getPlatform();
        String platformVersion = null;
        String platformName = null;
        
        if (platform.toLowerCase().startsWith("windows")) {
        	platformVersion = platform.toLowerCase().replace("windows", "").trim();
        } else if (platform.toLowerCase().startsWith("os x")) {
        	platformVersion = platform.toLowerCase().replace("os x", "").trim();
        } else {
        	throw new ConfigurationException("Only Windows and Mac are supported desktop platforms ('Windows xxx' or 'OS X xxx'). See https://www.browserstack.com/automate/capabilities for details. 'platform' param is " + platform);
        }
        
        capabilities.setCapability("browser", webDriverConfig.getBrowser());
        capabilities.setCapability("browser_version", webDriverConfig.getBrowserVersion()); 
        capabilities.setCapability("os", platformName);
        capabilities.setCapability("os_version", platformVersion); 

        return capabilities;
    }
}

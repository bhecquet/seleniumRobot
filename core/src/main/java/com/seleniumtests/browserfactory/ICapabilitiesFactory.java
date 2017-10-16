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

import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.JsonObject;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public abstract class ICapabilitiesFactory {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(AbstractWebDriverFactory.class);

    abstract DesiredCapabilities createCapabilities(DriverConfig cfg);
    
    public DesiredCapabilities updateDefaultCapabilities(final DesiredCapabilities capability, final DriverConfig webDriverConfig) {

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

        Proxy proxy = webDriverConfig.getProxy();
        capability.setCapability(CapabilityType.PROXY, proxy);

        return capability;
    }
    
}

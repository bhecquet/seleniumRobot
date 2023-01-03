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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import com.seleniumtests.customexception.DriverExceptions;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.osutility.OSUtility;

public class IEDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {
	
    public IEDriverFactory(final DriverConfig webDriverConfig1) {
        super(webDriverConfig1);
    }
    
    @Override
    protected WebDriver createNativeDriver() {
    	InternetExplorerOptions options = (InternetExplorerOptions)driverOptions;
    	options.setCapability(SeleniumRobotCapabilityType.TEST_NAME, (String)null);
        return new InternetExplorerDriver(options);
    }

    @Override
    public WebDriver createWebDriver() {

        if (!OSUtility.isWindows()) {
            throw new DriverExceptions("IE is only supported on windows");
        }

        return super.createWebDriver();
    }

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		return new IECapabilitiesFactory(webDriverConfig);
	}

}

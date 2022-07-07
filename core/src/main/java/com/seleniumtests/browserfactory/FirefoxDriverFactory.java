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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.seleniumtests.driver.DriverConfig;

public class FirefoxDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    private static boolean marionetteMode = true;
    
    /**
     * @param  cfg  the configuration of the firefoxDriver
     */
    public FirefoxDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }   

    /**
     * create native driver instance, designed for unit testing.
     *
     * @return	the driver
     */
    @Override
    protected WebDriver createNativeDriver() {
    	setMarionetteMode(true);

		return new FirefoxDriver((FirefoxOptions)driverOptions);
    }
    
	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		return new FirefoxCapabilitiesFactory(webDriverConfig);
	}

	public static boolean isMarionetteMode() {
		return marionetteMode;
	}

	private static void setMarionetteMode(boolean marionetteMode) {
		FirefoxDriverFactory.marionetteMode = marionetteMode;
	}
}

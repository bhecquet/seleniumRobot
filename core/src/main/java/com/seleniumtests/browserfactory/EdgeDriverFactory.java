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

import com.seleniumtests.driver.DriverConfig;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class EdgeDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    /**
     * @param  cfg  the configuration of the firefoxDriver
     */
    public EdgeDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }

    /**
     * create native driver instance, designed for unit testing.
     *
     * @return	the driver
     */
    @Override
    protected WebDriver createNativeDriver() {
    	return new EdgeDriver((EdgeOptions)driverOptions);
    }

	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		return new EdgeCapabilitiesFactory(webDriverConfig);
	}
}

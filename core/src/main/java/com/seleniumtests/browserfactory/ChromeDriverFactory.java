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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.seleniumtests.driver.DriverConfig;

public class ChromeDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    public ChromeDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }

    @Override
    protected WebDriver createNativeDriver() {
        return new ChromeDriver(new ChromeCapabilitiesFactory().createCapabilities(webDriverConfig));
    }

}

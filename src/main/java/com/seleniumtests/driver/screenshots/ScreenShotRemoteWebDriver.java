/*
 * Copyright 2015 www.seleniumtests.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.driver.screenshots;

import java.net.URL;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ScreenShotRemoteWebDriver extends RemoteWebDriver implements TakesScreenshot {
    public ScreenShotRemoteWebDriver(final DesiredCapabilities capabilities) {
        super(capabilities);
    }

    public ScreenShotRemoteWebDriver(final URL url, final DesiredCapabilities capabilities) {
        super(url, capabilities);
    }

    @Override
    public <X> X getScreenshotAs(final OutputType<X> target)  {
        if ((Boolean) getCapabilities().getCapability(CapabilityType.TAKES_SCREENSHOT)) {
            String output = execute(DriverCommand.SCREENSHOT).getValue().toString();
            return target.convertFromBase64Png(output);
        }

        return null;
    }
}

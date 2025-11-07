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

import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.remote.options.BaseOptions;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import org.openqa.selenium.Platform;

import java.util.Optional;

public abstract class ICapabilitiesFactory {

	protected static final Logger logger = SeleniumRobotLogger.getLogger(ICapabilitiesFactory.class);

    /**
     * TODO: should be moved to grid connectors
     * returns the app in capabilities
     * @param caps
     * @return
     */
    protected Optional<String> getApp(Capabilities caps) {

        Platform platformName = new BaseOptions<>(caps).getPlatformName();
        if (platformName == null) {
            return Optional.empty();
        } else if (platformName.is(Platform.ANDROID)) {
            return new UiAutomator2Options(caps).getApp();
        } else if (platformName.is(Platform.IOS)) {
            return new XCUITestOptions(caps).getApp();
        } else return Optional.empty();
    }

    protected Capabilities setApp(Capabilities caps, String app) {
        if (new BaseOptions(caps).getPlatformName().is(Platform.ANDROID)) {
            return new UiAutomator2Options(caps).setApp(app);
        } else if (new BaseOptions(caps).getPlatformName().is(Platform.IOS)) {
            return new XCUITestOptions(caps).setApp(app);
        } else {
            return caps;
        }
    }

	protected DriverConfig webDriverConfig;
    
    protected ICapabilitiesFactory(DriverConfig webDriverConfig) {
    	this.webDriverConfig = webDriverConfig;
    }

    public abstract MutableCapabilities createCapabilities();
}

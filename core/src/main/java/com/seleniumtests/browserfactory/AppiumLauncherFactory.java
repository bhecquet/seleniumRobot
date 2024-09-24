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

import java.nio.file.Paths;

import com.seleniumtests.browserfactory.mobile.AppiumLauncher;
import com.seleniumtests.browserfactory.mobile.ExistingAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.GridAppiumLauncher;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverMode;

public class AppiumLauncherFactory {
	
	private AppiumLauncherFactory() {
		// private constructor
	}

	public static AppiumLauncher getInstance() {
		if (!(SeleniumTestsContextManager.isMobileTest() || SeleniumTestsContextManager.isDesktopAppTest())) {
			throw new ConfigurationException("AppiumLauncher can only be used in mobile / windows app testing");
		}
		
		if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
			if (SeleniumTestsContextManager.getThreadContext().getAppiumServerUrl() == null) {
				throw new ConfigurationException("'appiumServerUrl' parameter MUST be set");
			} else {
				return new ExistingAppiumLauncher(SeleniumTestsContextManager.getThreadContext().getAppiumServerUrl());
			}
		} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
			return new GridAppiumLauncher();
		} else {
			throw new ConfigurationException("AppiumLauncher can only be used in local and grid mode");
		}
	}
}

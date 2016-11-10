package com.seleniumtests.browserfactory;

import com.seleniumtests.browserfactory.mobile.AppiumLauncher;
import com.seleniumtests.browserfactory.mobile.GridAppiumLauncher;
import com.seleniumtests.browserfactory.mobile.LocalAppiumLauncher;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverMode;

public interface AppiumLauncherFactory {

	public default AppiumLauncher getInstance() {
		if (!SeleniumTestsContextManager.isMobileTest()) {
			throw new ConfigurationException("AppiumLauncher can only be used in mobile testing");
		}
		
		if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.LOCAL) {
			return new LocalAppiumLauncher();
		} else if (SeleniumTestsContextManager.getThreadContext().getRunMode() == DriverMode.GRID) {
			return new GridAppiumLauncher();
		} else {
			throw new ConfigurationException("AppiumLauncher can only be used in local and grid mode");
		}
	}
}

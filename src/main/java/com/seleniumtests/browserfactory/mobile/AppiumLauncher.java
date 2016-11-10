package com.seleniumtests.browserfactory.mobile;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.driver.DriverMode;

public interface AppiumLauncher {

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

	
	public void startAppium();
	
	public void stopAppium();
}

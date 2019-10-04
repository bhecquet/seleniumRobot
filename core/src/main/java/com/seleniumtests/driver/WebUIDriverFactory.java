package com.seleniumtests.driver;

import java.util.HashMap;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class WebUIDriverFactory {
	
	private WebUIDriverFactory() {
		// nothing
	}

	public static void getInstance(String name) {
		WebUIDriver uiDriver = new WebUIDriver(name);
		uiDriver.setConfig(new DriverConfig(SeleniumTestsContextManager.getThreadContext()));
		
		if (WebUIDriver.getUxDriverSession().get() == null) {
			WebUIDriver.getUxDriverSession().set(new HashMap<>());
        }
		WebUIDriver.getUxDriverSession().get().put(name, uiDriver);
        
		WebUIDriver.switchToDriver(name);
	}
}

package com.seleniumtests.driver;

import java.util.HashMap;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class WebUIDriverFactory {
	
	private WebUIDriverFactory() {
		// nothing
	}

	public static WebUIDriver getInstance(String name) {
		return new WebUIDriver(name);
	}
}

package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverInternetExplorer extends TestDriver {

	public TestDriverInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}
}

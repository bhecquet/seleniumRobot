package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverSafari extends TestDriver {

	public TestDriverSafari() throws Exception {
		super(BrowserType.SAFARI);
	}

}

package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverFirefox extends TestDriver {

	public TestDriverFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}

}

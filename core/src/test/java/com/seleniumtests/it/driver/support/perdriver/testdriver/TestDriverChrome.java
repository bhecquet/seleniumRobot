package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverChrome extends TestDriver {

	public TestDriverChrome() throws Exception {
		super(BrowserType.CHROME);
	}

}

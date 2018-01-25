package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverHtmlUnit extends TestDriver {

	public TestDriverHtmlUnit() throws Exception {
		super(BrowserType.HTMLUNIT);
	}

}

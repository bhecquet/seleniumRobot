package com.seleniumtests.it.driver.support.perdriver.testdriver;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverEdge extends TestDriver {

	public TestDriverEdge() throws Exception {
		super(BrowserType.EDGE);
	}

}

package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameSafari extends TestFrame {

	public TestFrameSafari() throws Exception {
		super(BrowserType.SAFARI);
	}
}

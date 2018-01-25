package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameFirefox extends TestFrame {

	public TestFrameFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}
}

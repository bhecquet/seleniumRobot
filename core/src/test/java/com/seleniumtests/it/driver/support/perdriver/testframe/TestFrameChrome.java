package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameChrome extends TestFrame {

	public TestFrameChrome() throws Exception {
		super(BrowserType.CHROME);
	}
}

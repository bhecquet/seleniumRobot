package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameInternetExplorer extends TestFrame {

	public TestFrameInternetExplorer() throws Exception {
		super(BrowserType.INTERNET_EXPLORER);
	}
}

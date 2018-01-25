package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameHtmlUnit extends TestFrame {

	public TestFrameHtmlUnit() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
}

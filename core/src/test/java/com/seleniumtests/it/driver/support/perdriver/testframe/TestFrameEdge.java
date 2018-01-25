package com.seleniumtests.it.driver.support.perdriver.testframe;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameEdge extends TestFrame {

	public TestFrameEdge() throws Exception {
		super(BrowserType.EDGE);
	}
}

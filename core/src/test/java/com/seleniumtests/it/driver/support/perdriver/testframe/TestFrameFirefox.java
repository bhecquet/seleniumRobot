package com.seleniumtests.it.driver.support.perdriver.testframe;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestFrame;

public class TestFrameFirefox extends TestFrame {

	public TestFrameFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}
	
	@Test(groups= {"nogroup"})
	public void test() {
	}
}

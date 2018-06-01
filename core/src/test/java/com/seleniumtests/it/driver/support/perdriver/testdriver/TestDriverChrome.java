package com.seleniumtests.it.driver.support.perdriver.testdriver;

import java.awt.AWTException;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverChrome extends TestDriver {

	public TestDriverChrome() throws Exception {
		super(BrowserType.CHROME);
	}
	
	@Test(groups= {"nogroup"})
	public void test() throws AWTException, InterruptedException {
		testUploadFileWithRobot();
	}

}

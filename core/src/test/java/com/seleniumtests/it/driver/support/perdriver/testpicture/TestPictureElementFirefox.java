package com.seleniumtests.it.driver.support.perdriver.testpicture;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestPictureElement;

public class TestPictureElementFirefox extends TestPictureElement {

	public TestPictureElementFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}
	
	@Test(groups={"nogroup"})
	public void test() {
		
	}
}

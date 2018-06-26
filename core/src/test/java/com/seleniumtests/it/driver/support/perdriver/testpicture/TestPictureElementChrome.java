package com.seleniumtests.it.driver.support.perdriver.testpicture;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestPictureElement;

public class TestPictureElementChrome extends TestPictureElement {

	public TestPictureElementChrome() throws Exception {
		super(BrowserType.CHROME);
	}

	@Test(groups={"nogroup"})
	public void test() {
		testActionDurationIsLogged();
	}
}

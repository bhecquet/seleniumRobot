package com.seleniumtests.it.driver.support.perdriver.testdriver;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverFirefox extends TestDriver {

	public TestDriverFirefox() throws Exception {
		super(BrowserType.FIREFOX);
	}

	@Test(groups={"it"})
	public void testClickActionCheckbox() {
		super.testClickActionCheckbox();
	}

}

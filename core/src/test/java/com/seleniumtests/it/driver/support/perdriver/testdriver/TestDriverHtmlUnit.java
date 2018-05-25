package com.seleniumtests.it.driver.support.perdriver.testdriver;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestDriverHtmlUnit extends TestDriver {

	public TestDriverHtmlUnit() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
	
	@Test(groups={"it"})
	public void testClickActionCheckbox() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testAutoScrolling() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testClickActionRadio() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testDoubleClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testUploadFileWithRobot() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"it"})
	public void testUploadFileWithRobotKeyboard() {
		// skip as htmlunit does not support it
	}

}

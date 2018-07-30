package com.seleniumtests.ut.uipage;

import org.testng.annotations.Test;

import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.TestDriver;

public class TestHtmlElement extends TestDriver {

	public TestHtmlElement() throws Exception {
		super(BrowserType.HTMLUNIT);
	}
	
	@Test(groups={"ut"})
	public void testClickActionCheckbox() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testAutoScrolling() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testClickActionRadio() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testDoubleClickActionDiv() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testUploadFileWithRobot() {
		// skip as htmlunit does not support it
	}
	
	@Test(groups={"ut"})
	public void testUploadFileWithRobotKeyboard() {
		// skip as htmlunit does not support it
	}

}

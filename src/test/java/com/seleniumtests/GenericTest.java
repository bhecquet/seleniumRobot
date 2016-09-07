package com.seleniumtests;

import org.testng.annotations.BeforeMethod;

import com.seleniumtests.core.SeleniumTestsContextManager;

public class GenericTest {

	@BeforeMethod(alwaysRun=true)  
	public void initTest() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
	}
}

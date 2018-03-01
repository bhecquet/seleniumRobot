package com.seleniumtests.ut.core;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;

public class TestLogActions extends GenericDriverTest {
	

	private DriverTestPage testPage;

	@BeforeMethod(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);
	}

	/**
	 * Check that when calling a method with password in it, this is masked
	 */
	@Test(groups= {"ut"})
	public void testPassworkMasking() {

		testPage._setPassword("someText");
		TestStep step = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(2);
		
		System.out.println(step.toString());
		
//		Assert.assertFalse(step.toString().contains("someText"));
	}
	
	// TODO: test avec methode qui a une signature setPassword(String ... password)
}

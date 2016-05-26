package com.seleniumtests.it.webelements;

import java.util.Map;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;

/**
 * Test PageObject
 * @author behe
 *
 */
public class TestPageObject extends GenericTest{
	
	private static DriverTestPage testPage;

	@BeforeClass()
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		
		try {
			driver.manage().window().maximize();
		} catch (Exception e) {}
	}
	
	@Test
	public void testPageParam() {
		Assert.assertEquals(testPage.param("variable1"), "value3");
	}
	
}

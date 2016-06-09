package com.seleniumtests.it.webelements;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.helper.WaitHelper;
import com.seleniumtests.it.driver.DriverTestPage;

/**
 * Test PageObject
 * @author behe
 *
 */
public class TestPageObject{
	
	private static DriverTestPage testPage;
	private WebDriver driver;

	@BeforeClass()
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@Test()
	public void testPageParam() {
		Assert.assertEquals(testPage.param("variable1"), "value3");
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		driver.close();
	}
	
}

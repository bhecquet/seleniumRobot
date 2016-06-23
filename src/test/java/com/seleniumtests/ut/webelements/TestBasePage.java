package com.seleniumtests.ut.webelements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.it.driver.TestDriver;

public class TestBasePage {
	
	private static WebDriver driver;
	private static DriverTestPage testPage;
	private static TestDriver testDriverIt;

	@BeforeClass(groups={"ut"})
	public static void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
		testDriverIt = new TestDriver(driver, testPage);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"ut"})
	public void testIsElementPresent() {
		Assert.assertFalse(testPage.isElementPresent(By.tagName("h12"), 4));
	}
}

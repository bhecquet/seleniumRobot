package com.seleniumtests.it.webelements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;


public class TestInterceptePage {
	public TestInterceptePage() throws Exception {
		super();
	}

	private WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx, xmlTest);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		driver.close();
	}
	
	
	@Test(groups={"it"})
	public void interceptBy() {
		Assert.assertEquals(testPage.findById("map:Text"), testPage.findById("text2"), "intercept by with map doesn't work");
	}

}

package com.seleniumtests.it.driver;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestDesktopDrivers extends GenericTest {
	
	
	@Test(groups={"it"})
	public void testFirefoxStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAttribute("browser", "*firefox");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertEquals("about:blank", driver.getCurrentUrl());
	}
	
	@Test(groups={"it"})
	public void testChromeStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAttribute("browser", "*chrome");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertEquals("data:,", driver.getCurrentUrl());
	}
	
	@Test(groups={"it"})
	public void testIEStartup(final ITestContext testNGCtx, final XmlTest xmlTest) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setAttribute("browser", "*iexplore");
		driver = WebUIDriver.getWebDriver(true);
		Assert.assertTrue(driver.getCurrentUrl().contains("http://localhost:"));
	}
}

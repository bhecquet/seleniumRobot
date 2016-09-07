package com.seleniumtests.it.driver;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestBrowserProxy extends GenericDriverTest {
	
	private static final String BROWSER = "chrome";

	public void initDriver(final ITestContext testNGCtx) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@Test(groups={"it"})
	public void testFirefoxProxyAuto(final ITestContext testNGCtx) {
		System.setProperty(SeleniumTestsContext.BROWSER, BROWSER);
		System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "autodetect");
		initDriver(testNGCtx);
		driver.get("http://www.google.fr");
		Assert.assertTrue(driver.findElement(By.tagName("body")).getText().toLowerCase().contains("gmail"), "Google home page has not been loaded");
	}
	
	/**
	 * Test disabled as too long. To activate if some change is done on proxy
	 * @param testNGCtx
	 */
	@Test(groups={"it"}, expectedExceptions=TimeoutException.class, enabled=false)
	public void testFirefoxProxyDirect(final ITestContext testNGCtx) {
		System.setProperty(SeleniumTestsContext.BROWSER, BROWSER);
		System.setProperty(SeleniumTestsContext.PAGE_LOAD_TIME_OUT, "5");
		System.setProperty(SeleniumTestsContext.WEB_SESSION_TIME_OUT, "5");
		System.setProperty(SeleniumTestsContext.WEB_PROXY_TYPE, "direct");
		initDriver(testNGCtx);
		driver.get("http://www.google.fr");
	}
}

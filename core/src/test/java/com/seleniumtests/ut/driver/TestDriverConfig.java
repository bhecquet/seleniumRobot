package com.seleniumtests.ut.driver;

import java.util.List;

import org.openqa.selenium.support.events.WebDriverListener;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.DriverConfig;

public class TestDriverConfig extends GenericTest {

	/**
	 * Test Neoload is active when proxy mode is set and path is given
	 */
	@Test(groups={"ut"})
	public void isNeoloadActive() {
		try {
			SeleniumTestsContextManager.getThreadContext().setNeoloadUserPath("path");
			System.setProperty("nl.selenium.proxy.mode", "Design");
			
			DriverConfig config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
			
			Assert.assertTrue(config.isNeoloadActive());
		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
	/**
	 * Test Neoload is not active when proxy mode is not set
	 */
	@Test(groups={"ut"})
	public void isNeoloadNotActiveIfNoProxyMode() {
		SeleniumTestsContextManager.getThreadContext().setNeoloadUserPath("path");
		
		DriverConfig config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
		
		Assert.assertFalse(config.isNeoloadActive());
	}
	
	/**
	 * Test Neoload is not active when path is not set
	 */
	@Test(groups={"ut"})
	public void isNeoloadNotActiveIfNoPath() {
		try {
			System.setProperty("nl.selenium.proxy.mode", "Design");
			
			DriverConfig config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
			
			Assert.assertFalse(config.isNeoloadActive());
		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	
	@Test(groups={"ut"})
	public void testGetWebDriverListeners() {
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.ut.driver.WebDriverListener1,com.seleniumtests.ut.driver.WebDriverListener2");
		
		DriverConfig config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
		
		List<WebDriverListener> wdListeners = config.getWebDriverListeners();
		Assert.assertEquals(wdListeners.size(), 2);
		Assert.assertTrue(wdListeners.get(0) instanceof WebDriverListener1);
		Assert.assertTrue(wdListeners.get(1) instanceof WebDriverListener2);
		
	}
	
	@Test(groups={"ut"})
	public void testGetWebDriverListenersNone() {

		DriverConfig config = new DriverConfig(SeleniumTestsContextManager.getThreadContext());
		
		List<WebDriverListener> wdListeners = config.getWebDriverListeners();
		Assert.assertEquals(wdListeners.size(), 0);
		
	}
	
	
}

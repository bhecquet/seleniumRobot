package com.seleniumtests.ut.driver;

import java.util.List;

import org.openqa.selenium.support.events.WebDriverListener;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.DriverConfig;

public class TestDriverConfig extends GenericTest {

	
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

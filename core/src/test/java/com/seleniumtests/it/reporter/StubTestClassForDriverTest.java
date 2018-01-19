package com.seleniumtests.it.reporter;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;

public class StubTestClassForDriverTest extends StubParentClass {
	
	@BeforeMethod(groups="stub")
	public void init() {
		System.setProperty("browser", "htmlunit");
		System.setProperty("overrideSeleniumNativeAction", "true");
	}
	
	@AfterMethod(groups="stub")
	public void reset() {
		System.clearProperty("browser");
		System.clearProperty("overrideSeleniumNativeAction");
	}

	@Test(groups="stub")
	public void testDriver() throws Exception {
		
		new DriverTestPage(true)
			._writeSomething()
			._reset();
	}
	
	/**
	 * check that with selenium override, logging is done
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActions() throws Exception {
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset();
	}
	
	/**
	 * check that without selenium override, logging is not done
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActionsWithoutOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset();
	}
}

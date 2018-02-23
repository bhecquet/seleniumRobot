package com.seleniumtests.it.stubclasses;

import java.lang.reflect.Method;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageNativeActions;

public class StubTestClassForDriverTest extends StubParentClass {
	
	@BeforeMethod(groups="stub")
	public void init(Method method) {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(true);
	}

	@Test(groups="stub")
	public void testDriver() throws Exception {

		new DriverTestPage(true)
			._writeSomething()
			._reset()
			._sendKeysComposite()
			._clickPicture();
	}
	
	/**
	 * check that with selenium override, logging is done
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActions() throws Exception {
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}
	
	/**
	 * check that without selenium override, logging is not done
	 * @throws Exception
	 */
	@Test(groups="stub")
	public void testDriverNativeActionsWithoutOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		SeleniumTestsContextManager.getThreadContext().setReplayTimeout(1);
		SeleniumTestsContextManager.getThreadContext().setImplicitWaitTimeout(1); 
		
		new DriverTestPageNativeActions(true)
		.sendKeys()
		.reset()
		.select();
	}
	
	@Test(groups="stub")
	public void testDriverWithHtmlElementWithoutOverride() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setOverrideSeleniumNativeAction(false);
		
		new DriverTestPage(true)
			._writeSomething()
			._reset();
//			._sendKeysComposite()
//			._clickPicture();
	}
}

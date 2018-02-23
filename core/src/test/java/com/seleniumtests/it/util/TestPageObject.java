package com.seleniumtests.it.util;

import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;

public class TestPageObject extends GenericDriverTest {

	
	@Test(groups={"it"})
	public void testResizeWindow() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*firefox");
		driver = WebUIDriver.getWebDriver(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}
	
	@Test(groups={"it"})
	public void testResizeWindowHeadless() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		DriverTestPage testPage = new DriverTestPage(true);
		new DriverTestPage(true).resizeTo(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}

}

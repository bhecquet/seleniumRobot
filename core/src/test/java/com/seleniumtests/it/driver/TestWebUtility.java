package com.seleniumtests.it.driver;

import org.openqa.selenium.Dimension;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.WebUtility;

public class TestWebUtility extends GenericDriverTest {

	@Test(groups={"it"})
	public void testResizeWindow() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		new WebUtility(driver).resizeWindow(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}
	
	@Test(groups={"it"})
	public void testResizeWindowHeadless() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		DriverTestPage testPage = new DriverTestPage(true);
		new WebUtility(driver).resizeWindow(600, 400);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, 600);
		Assert.assertEquals(viewPortSize.height, 400);
	}
	
	@Test(groups={"it"})
	public void testNoResizeWindowIfDimOmited() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("*chrome");
		driver = WebUIDriver.getWebDriver(true);
		Dimension viewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		new WebUtility(driver).resizeWindow(600, null);
		Dimension maxViewPortSize = ((CustomEventFiringWebDriver)driver).getViewPortDimensionWithoutScrollbar();
		Assert.assertEquals(viewPortSize.width, maxViewPortSize.width);
		Assert.assertEquals(viewPortSize.height, maxViewPortSize.height);
	}

}

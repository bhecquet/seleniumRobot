package com.seleniumtests.ut.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.Mock;
import org.openqa.selenium.WebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.neotys.selenium.proxies.NLWebDriver;
import com.neotys.selenium.proxies.NLWebDriverFactory;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.logger.TestLogging;

@PrepareForTest({NLWebDriverFactory.class})
public class TestWebUIDriver extends MockitoTest {
	
	@Mock
	private NLWebDriver neoloadDriver;
	

	/**
	 * When driver is created, no Neoload driver is instanciated if neoload parameters are not set
	 */
	@Test(groups={"ut"})
	public void testDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		Assert.assertNull(((CustomEventFiringWebDriver)driver).getNeoloadDriver());
	}
	
	/**
	 * Check that when user requests  for neoload, it's driver is added 
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithNeoload() {
		

		PowerMockito.mockStatic(NLWebDriverFactory.class);
		PowerMockito.when(NLWebDriverFactory.newNLWebDriver(any(WebDriver.class), anyString())).thenReturn(neoloadDriver);
		
		try {
			SeleniumTestsContextManager.getThreadContext().setNeoloadUserPath("path");
			System.setProperty("nl.selenium.proxy.mode", "Design");
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			
			WebDriver driver = WebUIDriver.getWebDriver(true);
			
			Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
			Assert.assertNotNull(((CustomEventFiringWebDriver)driver).getNeoloadDriver());
		} finally {
			System.clearProperty("nl.selenium.proxy.mode");
		}
	}
	

	/**
	 * destroys the driver if one has been created
	 */
	@AfterMethod(groups={"ut", "it"}, alwaysRun=true)
	public void destroyDriver() {
		if (WebUIDriver.getWebDriver(false) != null) {
			WebUIDriver.cleanUp();
		}

		TestLogging.reset();
	}
}

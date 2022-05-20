package com.seleniumtests.ut.browserfactory;

import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.mockito.Mock;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.HtmlUnitDriverFactory;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;

public class TestAbstractWebDriverFactory extends MockitoTest {


	@Mock
	private SeleniumTestsContext context;

	@Mock
	private RemoteWebDriver driver;

	@Mock
	private DriverConfig config;
	
	@Test(groups={"ut"})
	public void testUserDefinedsCapabilities() throws Exception {
		
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("foo", "bar");
		caps.setCapability(CapabilityType.TAKES_SCREENSHOT, false);
		
		when(config.isEnableJavascript()).thenReturn(true);
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		when(config.getCapabilites()).thenReturn(caps);
		
		SeleniumTestsContextManager.setThreadContext(context);
		when(context.getTestType()).thenReturn(TestType.WEB);
		
		// connect to grid
		PowerMockito.whenNew(RemoteWebDriver.class).withAnyArguments().thenReturn(driver);
		HtmlUnitDriverFactory driverFactory = new HtmlUnitDriverFactory(config);
		
		Assert.assertFalse(driverFactory.getDriverOptions().is(CapabilityType.TAKES_SCREENSHOT));
		Assert.assertEquals(caps.getCapability("foo"), "bar");
	}

}
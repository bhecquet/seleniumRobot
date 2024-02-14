package com.seleniumtests.ut.browserfactory;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.HtmlUnitDriverFactory;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;

public class TestAbstractWebDriverFactory extends MockitoTest {

	@Mock
	private SeleniumTestsContext context;

	@Mock
	private DriverConfig config;
	
	@Test(groups={"ut"})
	public void testUserDefinedsCapabilities() throws Exception {
		
		MutableCapabilities caps = new MutableCapabilities();
		caps.setCapability("foo", "bar");
		
		when(config.getNodeTags()).thenReturn(new ArrayList<>());
		when(config.getCapabilites()).thenReturn(caps);
		when(config.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
		when(config.getWebPlatform()).thenReturn(Platform.WINDOWS);
		
		SeleniumTestsContextManager.setThreadContext(context);
		when(context.getTestType()).thenReturn(TestType.WEB);
		
		// connect to grid
		try (MockedConstruction mockedWebDriver = mockConstruction(RemoteWebDriver.class)) {

			HtmlUnitDriverFactory driverFactory = new HtmlUnitDriverFactory(config);

			Assert.assertEquals(caps.getCapability("foo"), "bar");
		}
	}

}
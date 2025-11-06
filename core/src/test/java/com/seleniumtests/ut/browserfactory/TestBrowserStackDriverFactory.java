package com.seleniumtests.ut.browserfactory;


import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserstackDriverFactory;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.DebugMode;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestBrowserStackDriverFactory extends MockitoTest {

	@Mock
	private DriverConfig config;
	
	@Mock
	private SeleniumGridConnector gridConnector1;

	@Mock
	private SeleniumTestsContext context;
	
	@Mock
	private Options options;
	
	@Mock
	private Timeouts timeouts;

	private Capabilities caps;

	@BeforeMethod(groups= {"ut"})
	public void init() throws Exception {
		when(config.getTestContext()).thenReturn(context);
		Mockito.when(config.getDebug()).thenReturn(List.of(DebugMode.NONE));

		when(config.getBrowserType()).thenReturn(BrowserType.HTMLUNIT);
		when(config.getPlatform()).thenReturn("windows");
		
		// configure driver
		Map<String, String> capsMap = new HashMap<>();
		capsMap.put(CapabilityType.BROWSER_NAME, "htmlunit");
		capsMap.put(CapabilityType.BROWSER_VERSION, "70.0.1.2.3");
		caps = new MutableCapabilities(capsMap);

		when(options.timeouts()).thenReturn(timeouts);
		
		when(gridConnector1.getHubUrl()).thenReturn(new URI("http://localhost:1111/wd/hub").toURL());
		when(gridConnector1.uploadMobileApp(any(Capabilities.class))).thenAnswer((invocation -> invocation.getArgument(0)));
		when(context.getTestStepManager()).thenReturn(new TestStepManager());
		when(context.getWebDriverGridTimeout()).thenReturn(SeleniumTestsContext.DEFAULT_WEB_DRIVER_GRID_TIMEOUT);
		SeleniumTestsContextManager.setThreadContext(context);
	}
	
	/**
	 * Check we create a driver from grid
	 */
	@Test(groups={"ut"})
	public void testDriverCreation() {

		when(context.getTestType()).thenReturn(TestType.WEB);

		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		
		// connect to grid
		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new BrowserstackDriverFactory(config);

			// check browserstack options are present
			Assert.assertNotNull(driverFactory.getDriverOptions().asMap().get("bstack:options"));
			Assert.assertEquals(driverFactory.getDriverOptions().asMap().get(SeleniumRobotCapabilityType.TEST_ID), "no-test");
			Assert.assertEquals(driverFactory.getDriverOptions().asMap().get(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());

			WebDriver newDriver = driverFactory.createWebDriver();

			// check that with a single driver creation, retry timeout is 30 mins
			Assert.assertEquals(driverFactory.getInstanceRetryTimeout(), SeleniumTestsContext.DEFAULT_WEB_DRIVER_GRID_TIMEOUT);

			// issue #280: also check that BrowserInfo is not null
			Assert.assertNotNull(newDriver);
			Assert.assertEquals(newDriver, mockedRemoteWebDriver.constructed().get(0));
			Assert.assertNotNull(driverFactory.getSelectedBrowserInfo());
			Assert.assertEquals(driverFactory.getSelectedBrowserInfo().getBrowser(), BrowserType.HTMLUNIT);
			Assert.assertEquals(driverFactory.getSelectedBrowserInfo().getVersion(), "70.0.1.2.3");

			// check SeleniumRobot input capabilities has been added to driver after creation
			Assert.assertEquals(((RemoteWebDriver)newDriver).getCapabilities().getCapability(SeleniumRobotCapabilityType.SESSION_CREATION_TRY), 0);
			Assert.assertEquals(((RemoteWebDriver)newDriver).getCapabilities().getCapability(SeleniumRobotCapabilityType.GLOBAL_SESSION_ID), SeleniumTestsContext.getContextId().toString());
			Assert.assertEquals(((RemoteWebDriver)newDriver).getCapabilities().getCapability(SeleniumRobotCapabilityType.TEST_ID), "no-test");
		}
	}
}

















package com.seleniumtests.ut.browserfactory;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seleniumtests.core.TestStepManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.util.helper.WaitHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.SeleniumGridDriverFactory;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridNodeNotAvailable;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.util.logging.DebugMode;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class TestSeleniumGridDriverFactory extends MockitoTest {

	@Mock
	private DriverConfig config;

	@Mock
	private Proxy proxyConfig;
	
	@Mock
	private SeleniumGridConnector gridConnector1;
	
	@Mock
	private SeleniumGridConnector gridConnector2;

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
		when(gridConnector2.getHubUrl()).thenReturn(new URI("http://localhost:2222/wd/hub").toURL());
		when(gridConnector1.uploadMobileApp(any())).thenReturn((MutableCapabilities) caps);
		when(gridConnector2.uploadMobileApp(any())).thenReturn((MutableCapabilities) caps);
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
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
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
		}
	}
	
	@Test(groups={"ut"})
	public void testMobileDriverCreation()  {

		when(config.getPlatform()).thenReturn("android");
		when(config.getApp()).thenReturn("myApp.apk");
		when(context.getTestType()).thenReturn(TestType.APPIUM_APP_ANDROID);
		
		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getAppiumCapabilities()).thenReturn(new BaseOptions<>());
		
		// connect to grid
		try (MockedConstruction<AndroidDriver> mockedAndroidDriver = mockConstruction(AndroidDriver.class, (androidDriver, context1) -> {
			when(androidDriver.manage()).thenReturn(options);
			when(androidDriver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
			WebDriver newDriver = driverFactory.createWebDriver();

			// verify app is uploaded
			verify(gridConnector1).uploadMobileApp(any());

			// verify driver is an android driver
			Assert.assertTrue(newDriver instanceof AndroidDriver);
		}
	}
	
	@Test(groups={"ut"})
	public void testIosMobileDriverCreation() {
		

		when(config.getPlatform()).thenReturn("ios");
		when(config.getApp()).thenReturn("myApp.ipa");
		when(context.getTestType()).thenReturn(TestType.APPIUM_APP_IOS);
		
		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getAppiumCapabilities()).thenReturn(new MutableCapabilities());
		
		// connect to grid
		try (MockedConstruction<IOSDriver> mockedIOSDriver = mockConstruction(IOSDriver.class, (iosDriver, context1) -> {
			when(iosDriver.manage()).thenReturn(options);
			when(iosDriver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
			WebDriver newDriver = driverFactory.createWebDriver();

			// verify app is uploaded
			verify(gridConnector1).uploadMobileApp(any());

			// verify driver is an android driver
			Assert.assertTrue(newDriver instanceof IOSDriver);
		}
	}

	@Test(groups={"ut"})
	public void testWindowsDriverCreation() {

		when(config.getPlatform()).thenReturn("windows");
		when(config.getApp()).thenReturn("notepad.exe");
		when(context.getTestType()).thenReturn(TestType.APPIUM_APP_WINDOWS);

		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getAppiumCapabilities()).thenReturn(new BaseOptions<>());

		// connect to grid
		try (MockedConstruction<AppiumDriver> mockedDriver = mockConstruction(AppiumDriver.class, (windowsDriver, context1) -> {
			when(windowsDriver.manage()).thenReturn(options);
			when(windowsDriver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
			WebDriver newDriver = driverFactory.createWebDriver();

			// verify driver is an android driver
			Assert.assertTrue(newDriver instanceof AppiumDriver);
		}
	}

	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Platform linux is not supported for application tests")
	public void testLinuxDriverCreation() {

		when(config.getPlatform()).thenReturn("linux");
		when(config.getApp()).thenReturn("app");
		when(context.getTestType()).thenReturn(TestType.APPIUM_APP_WINDOWS);

		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getAppiumCapabilities()).thenReturn(new BaseOptions<>());

		// connect to grid
		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (windowsDriver, context1) -> {
			when(windowsDriver.manage()).thenReturn(options);
			when(windowsDriver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
			WebDriver newDriver = driverFactory.createWebDriver();

			// verify driver is an android driver
			Assert.assertTrue(newDriver instanceof RemoteWebDriver);
		}
	}

	/**
	 * When getSessionInformationFromGrid fails, we should quit the driver in case it has been created
	 */
	@Test(groups={"ut"}, expectedExceptions = SeleniumGridNodeNotAvailable.class)
	public void testDriverCreationWithSessionInformationFailing() {

		when(context.getTestType()).thenReturn(TestType.WEB);
		when(context.getWebDriverGridTimeout()).thenReturn(20);

		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		doThrow(new SessionNotCreatedException("some error")).when(gridConnector1).getSessionInformationFromGrid(any(RemoteWebDriver.class), anyLong());

		// connect to grid
		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config);
			try {
				driverFactory.createWebDriver();
			} finally {
				// check driver has been quit (1 time for each retry)
				verify(mockedRemoteWebDriver.constructed().get(0)).quit();
				verify(mockedRemoteWebDriver.constructed().get(1)).quit();
				verify(mockedRemoteWebDriver.constructed().get(2)).quit();
			}
		}
	}
	
	/**
	 * If grid is not active, driver is not created and exception is raised
	 */
	@Test(groups={"ut"}, expectedExceptions=SeleniumGridNodeNotAvailable.class)
	public void testDriverNotCreatedIfGridNotActive() {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {
			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			when(context.getWebDriverGridTimeout()).thenReturn(2);

			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(false);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));

			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config, false);
			driverFactory.createWebDriver();
			
		}
		
	}
	
	/**
	 * If exception is raised during driver creation, exception is raised
	 */
	@Test(groups={"ut"}, expectedExceptions=SeleniumGridNodeNotAvailable.class)
	public void testDriverNotCreatedIfError() {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {
			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			when(context.getWebDriverGridTimeout()).thenReturn(1);

			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
			
			// connect to grid
			SeleniumGridDriverFactory factory = spy(new SeleniumGridDriverFactory(config));
			when(factory.getDriverInstance(any(URL.class), any(MutableCapabilities.class))).thenThrow(new WebDriverException(""));
			factory.createWebDriver();
		}
	}

	/**
	 * Check timeout set for driver creation is used
	 */
	@Test(groups={"ut"}, expectedExceptions=SeleniumGridNodeNotAvailable.class)
	public void testDriverNotCreatedIfErrorWithTimeout() {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {
			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			when(context.getWebDriverGridTimeout()).thenReturn(5);

			when(context.getTestType()).thenReturn(TestType.WEB);

			when(gridConnector1.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));

			// connect to grid
			Instant start = Instant.now();
			SeleniumGridDriverFactory factory = spy(new SeleniumGridDriverFactory(config));
			when(factory.getDriverInstance(any(URL.class), any(MutableCapabilities.class))).thenThrow(new WebDriverException(""));
			factory.createWebDriver();
			Assert.assertTrue(Duration.between(start, Instant.now()).toMillis() > 5000);
		}
	}
	
	/**
	 * Check we use the first grid connector if it's available
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectors() {
		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {
			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {

			when(context.getTestType()).thenReturn(TestType.WEB);

			when(gridConnector1.isGridActive()).thenReturn(true);
			when(gridConnector2.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1, gridConnector2));

			SeleniumGridDriverFactory factory = new SeleniumGridDriverFactory(config, false);
			WebDriver newDriver = factory.createWebDriver();
			Assert.assertNotNull(newDriver);

			// check the second grid connector has never been called
			verify(gridConnector1).isGridActive();
			verify(gridConnector2, never()).isGridActive();

			Assert.assertEquals(factory.getActiveGridConnector(), gridConnector1);
		}
	}

	@Test(groups={"ut"})
	public void testShuffleGridConnectors() {

		when(context.getTestType()).thenReturn(TestType.WEB);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1, gridConnector2));

		// check that at least once, order is changed
		boolean ok = false;
		for (int i = 0; i < 10; i++) {
			try {
				SeleniumGridDriverFactory factory = new SeleniumGridDriverFactory(config, true);
				Assert.assertEquals(factory.getGridConnectors().get(0), gridConnector2);
				ok = true;
				break;
			} catch (AssertionError e) {
				WaitHelper.waitForMilliSeconds(100);
			}
		}
		Assert.assertTrue(ok);
	}
	
	/**
	 * Check we use the first grid connector if it's available
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectorsOneUnavailable() {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			when(context.getTestType()).thenReturn(TestType.WEB);

			when(gridConnector1.isGridActive()).thenReturn(false);
			when(gridConnector2.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1, gridConnector2));

			SeleniumGridDriverFactory factory = new SeleniumGridDriverFactory(config, false);
			WebDriver newDriver = factory.createWebDriver();
			Assert.assertNotNull(newDriver);

			verify(gridConnector1).isGridActive();
			verify(gridConnector2).isGridActive();
			Assert.assertEquals(factory.getActiveGridConnector(), gridConnector2);
		}
	}
	
	/**
	 * Check we use the connector which is not in error
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithSeveralGridConnectorsOneInError() throws URISyntaxException, MalformedURLException {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {
			when(context.getWebDriverGridTimeout()).thenReturn(1);

			when(context.getTestType()).thenReturn(TestType.WEB);
			
			when(gridConnector1.isGridActive()).thenReturn(true);
			when(gridConnector2.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1, gridConnector2));
			
			// first driver will create an exception
			SeleniumGridDriverFactory factory = spy(new SeleniumGridDriverFactory(config, false));

			// first driver creation will fail, the second one (on second grid) will succeed
			when(factory.getDriverInstance(eq(new URI("http://localhost:1111/wd/hub").toURL()), any(MutableCapabilities.class))).thenThrow(new WebDriverException(""));
			RemoteWebDriver drv = new RemoteWebDriver(new URI("http://localhost:2222/wd/hub").toURL(), caps);
			when(factory.getDriverInstance(eq(new URI("http://localhost:2222/wd/hub").toURL()), any(MutableCapabilities.class))).thenReturn(drv);
			WebDriver newDriver = factory.createWebDriver();
			Assert.assertNotNull(newDriver);

			// both grid connectors have been called
			verify(factory, times(2)).getDriverInstance(any(URL.class), any(MutableCapabilities.class));

			Assert.assertEquals(factory.getActiveGridConnector(), gridConnector2);
			
		}
	}
	
	

	/**
	 * Check we can force driver to be created on the same node using capabilities if DriverConfig.getRunOnSameNode() specifies the node name
	 * For this to work a previous driver must have been created for one of the configured grid connector
	 */
	@Test(groups={"ut"})
	public void testDriverCreationOnSameNode() {
		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {

			when(context.getTestType()).thenReturn(TestType.WEB);

			when(gridConnector1.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
			when(config.getRunOnSameNode()).thenReturn("http://localhost:5556/");
			when(config.getSeleniumGridConnector()).thenReturn(gridConnector1); // simulate a previously created driver

			// connect to grid
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config, false);
			WebDriver newDriver = driverFactory.createWebDriver();

			// check that with a multiple drivers creation, retry timeout is 1 min 30 for the second drivers because runOnSameNode is set
			Assert.assertEquals(driverFactory.getInstanceRetryTimeout(), 90);

			Assert.assertNotNull(newDriver);
			Assert.assertEquals(newDriver, mockedRemoteWebDriver.constructed().get(0));
		}
	}
	
	/**
	 * Check capability is correctly set when requested to run on same node
	 */
	@Test(groups={"ut"})
	public void testCapabilitiesCreationOnSameNode() {
		

		when(context.getTestType()).thenReturn(TestType.WEB);
		
		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getRunOnSameNode()).thenReturn("http://localhost:5556/");
		when(config.getSeleniumGridConnector()).thenReturn(gridConnector1); // simulate a previously created driver

		caps = new SeleniumGridDriverFactory(config).createSpecificGridCapabilities(config);
		Assert.assertEquals(caps.getCapability(SeleniumRobotCapabilityType.ATTACH_SESSION_ON_NODE), "http://localhost:5556/");
	}
	
	/**
	 * Check we can force driver to be created on the same node using capabilities if DriverConfig.getRunOnSameNode() specifies the node name
	 * For this to work a previous driver must have been created for one of the configured grid connector
	 */
	@Test(groups={"ut"})
	public void testDriverCreationOnSameNodeMultipleHubs() {

		try (MockedConstruction<RemoteWebDriver> mockedRemoteWebDriver = mockConstruction(RemoteWebDriver.class, (driver, context1) -> {

			when(driver.manage()).thenReturn(options);
			when(driver.getCapabilities()).thenReturn(caps);
		})) {

			when(context.getTestType()).thenReturn(TestType.WEB);

			when(gridConnector1.isGridActive()).thenReturn(true);
			when(gridConnector2.isGridActive()).thenReturn(true);
			when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1, gridConnector2));
			when(config.getRunOnSameNode()).thenReturn("http://localhost:5556/");
			when(config.getSeleniumGridConnector()).thenReturn(gridConnector2); // simulate a previously created driver

			// connect to grid
			SeleniumGridDriverFactory driverFactory = new SeleniumGridDriverFactory(config, false);
			WebDriver newDriver = driverFactory.createWebDriver();
			Assert.assertNotNull(newDriver);
			Assert.assertEquals(newDriver, mockedRemoteWebDriver.constructed().get(0));

			// Check selected grid connector is the requested one
			Assert.assertEquals(driverFactory.getActiveGridConnector(), gridConnector2);
		}
	}
	
	/**
	 * Check that it's not possible to create a driver on same node is no previous driver has been created before (gridConnector is null in context)
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDriverDoNotCreateDriverOnSameNodeWithoutPreviousOne() {

		when(context.getWebDriverGridTimeout()).thenReturn(1);

		when(context.getTestType()).thenReturn(TestType.WEB);

		when(gridConnector1.isGridActive()).thenReturn(true);
		when(context.getSeleniumGridConnectors()).thenReturn(List.of(gridConnector1));
		when(config.getRunOnSameNode()).thenReturn("http://localhost:5556/");
		when(config.getSeleniumGridConnector()).thenReturn(null);			// simulate the case where no previous driver was created

		// connect to grid
		new SeleniumGridDriverFactory(config).createWebDriver();

	}
	
}

















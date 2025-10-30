package com.seleniumtests.ut.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.seleniumtests.browserfactory.*;
import com.seleniumtests.customexception.RetryableDriverException;
import com.seleniumtests.driver.*;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.StatisticsStorage;
import com.seleniumtests.core.StatisticsStorage.DriverUsage;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.it.stubclasses.StubTestPage;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.video.VideoRecorder;

public class TestWebUIDriver extends MockitoTest {

	@Mock
	private RemoteWebDriver drv1;
	
	@Mock
	private RemoteWebDriver drv2;
	
	@Mock
	private VideoRecorder videoRecorder;

	@Mock
	private CustomEventFiringWebDriver eventDriver1;
	
	@Mock
	private CustomEventFiringWebDriver eventDriver2;
	
	@Mock
	private SeleniumGridConnector gridConnector;

	@Mock
	private WebDriver.Options options;

	@Mock
	private Logs logs;

	@Mock
	private WebDriver.Timeouts timeouts;

	@Mock
	private WebDriver.TargetLocator targetLocator;


	@BeforeMethod(groups={"ut"})
	private void init() {
		// add capabilities to allow augmenting driver
		when(drv1.getCapabilities()).thenReturn(new DesiredCapabilities());
		when(drv1.manage()).thenReturn(options);
		when(drv1.switchTo()).thenReturn(targetLocator);
		when(options.timeouts()).thenReturn(timeouts);
		when(options.logs()).thenReturn(logs);
		when(drv2.getCapabilities()).thenReturn(new DesiredCapabilities());
		StatisticsStorage.reset();
	}

	@Test(groups={"ut"})
	public void testDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		
		Capabilities caps = ((CustomEventFiringWebDriver)driver).getInternalCapabilities();
		Assert.assertNotNull(caps.getCapability(SeleniumRobotCapabilityType.START_TIME));
		Assert.assertNotNull(caps.getCapability(SeleniumRobotCapabilityType.STARTUP_DURATION));
	}

	@Test(groups={"ut"})
	public void testAppiumDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setApp("notepad");
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);
		SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4321/");

		try (MockedConstruction<AppiumDriverFactory> mockedAppiumDriverFactory = mockConstruction(AppiumDriverFactory.class, (appiumDriverFactory, context) -> {

			WebDriver driver = mock(RemoteWebDriver.class);
			when(appiumDriverFactory.createWebDriver()).thenReturn(driver);
			when(((HasCapabilities)driver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("app", "notepad", "automationName", "FlaUI", "platformName", "windows")));
		})) {

			WebDriver driver = WebUIDriver.getWebDriver(true);

			Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
			Assert.assertEquals(mockedAppiumDriverFactory.constructed().size(), 1);

			Capabilities caps = ((CustomEventFiringWebDriver) driver).getInternalCapabilities();
			Assert.assertNotNull(caps.getCapability(SeleniumRobotCapabilityType.START_TIME));
			Assert.assertNotNull(caps.getCapability(SeleniumRobotCapabilityType.STARTUP_DURATION));
		}
	}

	@Test(groups={"ut"})
	public void testFirefoxDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");

		try (MockedConstruction<FirefoxDriverFactory> mockedFirefoxDriverFactory = mockConstruction(FirefoxDriverFactory.class, (firefoxDriverFactory, context) -> {

			WebDriver driver = mock(FirefoxDriver.class);
			when(firefoxDriverFactory.createWebDriver()).thenReturn(driver);
			when(((HasCapabilities)driver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "firefox", "browserVersion", "100.0")));
		});
			 MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.WINDOWS);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.FIREFOX, List.of(new BrowserInfo(BrowserType.FIREFOX, "100"))));


			WebDriver driver = WebUIDriver.getWebDriver(true);
			Assert.assertTrue(((CustomEventFiringWebDriver)driver).getOriginalDriver() instanceof FirefoxDriver);
			Assert.assertEquals(mockedFirefoxDriverFactory.constructed().size(), 1);
		}
	}

	@Test(groups={"ut"})
	public void testChromeDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");

		try (MockedConstruction<ChromeDriverFactory> mockedChromeDriverFactory = mockConstruction(ChromeDriverFactory.class, (chromeDriverFactory, context) -> {

			WebDriver driver = mock(ChromeDriver.class);
			when(chromeDriverFactory.createWebDriver()).thenReturn(driver);
			when(((HasCapabilities)driver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "chrome", "browserVersion", "100.0")));
		});
			 MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.WINDOWS);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.CHROME, List.of(new BrowserInfo(BrowserType.CHROME, "100"))));


			WebDriver driver = WebUIDriver.getWebDriver(true);
			Assert.assertTrue(((CustomEventFiringWebDriver)driver).getOriginalDriver() instanceof ChromeDriver);
			Assert.assertEquals(mockedChromeDriverFactory.constructed().size(), 1);
		}
	}

	@Test(groups={"ut"})
	public void testEdgeDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("edge");

		try (MockedConstruction<EdgeDriverFactory> mockedEdgeDriverFactory = mockConstruction(EdgeDriverFactory.class, (edgeDriverFactory, context) -> {

			WebDriver driver = mock(EdgeDriver.class);
			when(edgeDriverFactory.createWebDriver()).thenReturn(driver);
			when(((HasCapabilities)driver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "edge", "browserVersion", "100.0")));
		});
			MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.WINDOWS);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.EDGE, List.of(new BrowserInfo(BrowserType.EDGE, "100"))));

			WebDriver driver = WebUIDriver.getWebDriver(true);
			Assert.assertTrue(((CustomEventFiringWebDriver)driver).getOriginalDriver() instanceof EdgeDriver);
			Assert.assertEquals(mockedEdgeDriverFactory.constructed().size(), 1);
		}
	}

	@Test(groups={"ut"})
	public void testSafariDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("safari");

		try (MockedConstruction<SafariDriverFactory> mockedSafariDriverFactory = mockConstruction(SafariDriverFactory.class, (safariDriverFactory, context) -> {

			WebDriver driver = mock(SafariDriver.class);
			when(safariDriverFactory.createWebDriver()).thenReturn(driver);
			when(((HasCapabilities)driver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "safari", "browserVersion", "100.0")));
		});
			MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.MAC);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.SAFARI, List.of(new BrowserInfo(BrowserType.SAFARI, "100"))));

			WebDriver driver = WebUIDriver.getWebDriver(true);
			Assert.assertTrue(((CustomEventFiringWebDriver)driver).getOriginalDriver() instanceof SafariDriver);
			Assert.assertEquals(mockedSafariDriverFactory.constructed().size(), 1);
		}
	}

	
	/**
	 * No list for the selected driver
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser HTMLUNIT is not available.*")
	public void testDriverCreationBrowserNotAvailable() {

		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, CALLS_REAL_METHODS)) {
			Map<BrowserType, List<BrowserInfo>> browserInfos = new EnumMap<>(BrowserType.class);
			browserInfos.put(BrowserType.CHROME, List.of(new BrowserInfo(BrowserType.CHROME, "96.0", "", false, false)));

			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(anyBoolean())).thenReturn(browserInfos);

			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			WebUIDriver.getWebDriver(true);
		}
	}
	
	/**
	 * Empty list for the selected driver
	 */
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser HTMLUNIT is not available.*")
	public void testDriverCreationBrowserNotAvailable2() {

		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, CALLS_REAL_METHODS)) {
			Map<BrowserType, List<BrowserInfo>> browserInfos = new EnumMap<>(BrowserType.class);
			browserInfos.put(BrowserType.HTMLUNIT, new ArrayList<>());
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(anyBoolean())).thenReturn(browserInfos);

			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			WebUIDriver.getWebDriver(true);
		}
	}
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser CHROME beta is not available.*")
	public void testDriverCreationBrowserBetaNotAvailable() {

		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, CALLS_REAL_METHODS)) {
			Map<BrowserType, List<BrowserInfo>> browserInfos = new EnumMap<>(BrowserType.class);
			browserInfos.put(BrowserType.CHROME, List.of(new BrowserInfo(BrowserType.CHROME, "96.0", "", false, false)));
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(anyBoolean())).thenReturn(browserInfos);

			SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
			SeleniumTestsContextManager.getThreadContext().setBetaBrowser(true);
			WebUIDriver.getWebDriver(true);
		}
	}
	
	
	@Test(groups={"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Browser CHROME {2}is not available.*")
	public void testDriverCreationBrowserNonBetaNotAvailable() {

		try (MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class, CALLS_REAL_METHODS)) {
			Map<BrowserType, List<BrowserInfo>> browserInfos = new EnumMap<>(BrowserType.class);
			browserInfos.put(BrowserType.CHROME, List.of(new BrowserInfo(BrowserType.CHROME, "96.0", "", false, true)));
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(anyBoolean())).thenReturn(browserInfos);

			SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
			SeleniumTestsContextManager.getThreadContext().setBetaBrowser(false);
			WebUIDriver.getWebDriver(true);
		}
	}
	
	/**
	 * Check that usage duration is added on driver quit
	 */
	@Test(groups={"ut"})
	public void testCapsOnDriverQuit() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		CustomEventFiringWebDriver driver = (CustomEventFiringWebDriver)WebUIDriver.getWebDriver(true);
		driver.quit();
		
		List<DriverUsage> driverUsages = StatisticsStorage.getDriverUsage();
		Assert.assertEquals(driverUsages.size(), 1);
		Assert.assertEquals(driverUsages.get(0).getBrowserName(), "htmlunit");
		Assert.assertTrue(driverUsages.get(0).getDuration() > 0.0);
		Assert.assertNull(driverUsages.get(0).getGridHub());
		Assert.assertNull(driverUsages.get(0).getGridNode());
		Assert.assertTrue(driverUsages.get(0).getStartTime() > 0);
		Assert.assertEquals(driverUsages.get(0).getTestName(), "testCapsOnDriverQuit");
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithListener() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverListener("com.seleniumtests.ut.driver.WebDriverListener1");
		WebDriverListener1.setCalled(false);
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		driver.manage().window().getSize();
		
		// check 'afterGetSize()' has been called
		Assert.assertTrue(WebDriverListener1.isCalled());
		

	}
	
	@Test(groups={"ut"})
	public void testConstructor() {
		WebUIDriverFactory.getInstance("foo").initInstance();
		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 1);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "foo");
	}
	
	/**
	 * A new WebUIDriver is the new default one
	 */
	@Test(groups={"ut"})
	public void testMultipleConstructor() {
		WebUIDriverFactory.getInstance("foo").initInstance();
		WebUIDriverFactory.getInstance("bar").initInstance();
		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 2);

		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "bar");
	}

	/**
	 * issue #280: check that selectedBrowserInfo is available in grid mode
	 */
	@Test(groups={"ut"})
	public void testDriverCreationInGrid() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		
		
		// set connector to simulate the driver creation on grid
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnectors(List.of(gridConnector));
		when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");

		try (MockedConstruction<SeleniumGridDriverFactory> mockedGridDriverFactory = mockConstruction(SeleniumGridDriverFactory.class, (gridDriverFactory, context) -> {
			when(gridDriverFactory.createWebDriver()).thenReturn(new HtmlUnitDriver());
			when(gridDriverFactory.getSelectedBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.HTMLUNIT, "1.1"));
		})) {

			CustomEventFiringWebDriver ceDriver = (CustomEventFiringWebDriver) WebUIDriver.getWebDriver(true);
			Assert.assertNotNull(ceDriver.getBrowserInfo());
			Assert.assertEquals(ceDriver.getBrowserInfo().getVersion(), "1.1");

			verify(mockedGridDriverFactory.constructed().get(0)).createWebDriver();
		}
	}

	/**
	 * #619: Check that when driver creation fails, cleanUp operations are normal
	 * In this test, driver creation completely fails
	 */
	@Test(groups={"ut"})
	public void testDriverCreationInGridFails() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");

		// set connector to simulate the driver creation on grid
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnectors(List.of(gridConnector));
		when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");

		try (MockedConstruction<SeleniumGridDriverFactory> mockedGridDriverFactory = mockConstruction(SeleniumGridDriverFactory.class, (gridDriverFactory, context) -> {
			doAnswer(invocation -> {
				throw new IOException("KO");
			}).when(gridDriverFactory).createWebDriver();
			when(gridDriverFactory.getSelectedBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.HTMLUNIT, "1.1"));
		})) {
			
			Assert.assertThrows(SkipException.class, () -> WebUIDriver.getWebDriver(true));

			// check we try to create the driver only once, because on the first try, it fails completely
			verify(mockedGridDriverFactory.constructed().get(0)).createWebDriver();

			// check cleanUp is correct
			WebUIDriver.cleanUp();
		}
	}

	/**
	 * #619: test the case where driver is created, but when CustomEventFiringWebDriver augments the driver, it step fails on CDP connection
	 * "remotely closed"
	 */
	@Test(groups={"ut"})
	public void testDriverCreationInGridSuccessAndAugmentingFails() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		List<WebDriver> drivers = new ArrayList<>();

		// set connector to simulate the driver creation on grid
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnectors(List.of(gridConnector));
		when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");

		try (MockedConstruction<SeleniumGridDriverFactory> mockedGridDriverFactory = mockConstruction(SeleniumGridDriverFactory.class, (gridDriverFactory, context) -> {

			when(gridDriverFactory.createWebDriver()).thenAnswer(
					(Answer<WebDriver>) invocation -> {
						WebDriver realDriver = spy(new HtmlUnitDriver());
						when(realDriver.switchTo()).thenReturn(targetLocator);
						drivers.add(realDriver);
						return realDriver;
					}
			);

			when(gridDriverFactory.getSelectedBrowserInfo()).thenReturn(new BrowserInfo(BrowserType.HTMLUNIT, "1.1"));
			});
			MockedConstruction<Augmenter> mockedAugmenter = mockConstruction(Augmenter.class, (augmenter, context) -> {
				doAnswer(invocation -> {
					throw new IOException("Remotely closed"); // simulate the case where CDP connection cannot be established
				}).when(augmenter).augment(any());
			});
		) {


			try {
				WebUIDriver.getWebDriver(true);
			} catch (RetryableDriverException e) {
				// it's expected
			}
			// check we tried to create driver 2 times, because driver was created on grid, but failed to be augmented
			verify( mockedAugmenter.constructed().get(0)).augment(any(WebDriver.class));
			verify( mockedAugmenter.constructed().get(1)).augment(any(WebDriver.class));

			// check cleanUp is correct and driver is closed
			WebUIDriver.logFinalDriversState(Reporter.getCurrentTestResult());
			WebUIDriver.cleanUp();
			verify(drivers.get(0)).quit();
			verify(drivers.get(1)).quit();
		}
	}

	/**
	 * Test that 2 drivers can be created specifying different name
	 */
	@Test(groups={"ut"})
	public void testNewDriverCreationInGrid() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");
		
		WebUIDriver uiDriver1 = spy(WebUIDriver.getWebUIDriver(true, "main")); // create it so that we can control it via mock
		WebUIDriver.getUxDriverSession().get().put("main", uiDriver1);
		doReturn(drv1).when(uiDriver1).createWebDriver();
		WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);

		// set connector to simulate the driver creation on grid
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(gridConnector);
		when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");
		
		WebUIDriver uiDriver2 = spy(WebUIDriver.getWebUIDriver(true, "other"));
		WebUIDriver.getUxDriverSession().get().put("other", uiDriver2);
		doReturn(drv2).when(uiDriver2).createWebDriver();
		WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);
		Assert.assertNull(uiDriver1.getConfig().getRunOnSameNode());
		Assert.assertNotNull(uiDriver2.getConfig().getRunOnSameNode());
		
		// second driver is the active one
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "other");

	}
	
	/**
	 * Check the case where second driver fails to start, we should remove all it's references (name) from WebUiDriver
	 * and switch back to previous driver
	 * In that case, we should not raise a SkipException as the test has already started
	 */
	@Test(groups={"ut"})
	public void testNewDriverCreationInGridFails() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		SeleniumTestsContextManager.getThreadContext().setWebDriverGrid("http://localhost:4444/wd/hub");
		SeleniumTestsContextManager.getThreadContext().setRunMode("grid");


		try (MockedConstruction<WebUIDriver> mockedWebUIDriver = mockConstruction(WebUIDriver.class,
				withSettings().defaultAnswer(CALLS_REAL_METHODS),
				(uiDriver, context) -> {
			if (context.arguments().get(0).equals("main")) {
				doReturn(drv1).when(uiDriver).createWebDriver();
			} else {
				doThrow(new WebDriverException("error")).when(uiDriver).createWebDriver();
			}
			uiDriver.setName(context.arguments().get(0).toString()); // mandatory to set the name on the instance, as it's what constructor does
		})) {
			WebUIDriver.getUxDriverSession().remove();

			WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);

			// set connector to simulate the driver creation on grid
			SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(gridConnector);
			when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");

			Assert.assertThrows(WebDriverException.class, () -> WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null));

			Assert.assertNull((mockedWebUIDriver.constructed().get(0)).getConfig().getRunOnSameNode());
			Assert.assertNotNull((mockedWebUIDriver.constructed().get(1)).getConfig().getRunOnSameNode());

			Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "main");
		}

	}
	
	/**
	 * Check that with grid execution, we set the name of the previous driver execution node on this new driver
	 */
	@Test(groups={"ut"})
	public void testNewDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);
		
		Assert.assertNotEquals(driver1, driver2);
	}

	/**
	 * #619: when driver augmenting fails, we get a RemoteWebDriver instead of a CustomEventFiringWebDriver
	 * Check getWebDriver won't fail
	 */
	@Test(groups={"ut"})
	public void testNewDriverCreationWithExistingFailed() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");

		WebUIDriver uiDriver1 = spy(WebUIDriver.getWebUIDriver(true, "main"));
		RemoteWebDriver realDriver = mock(RemoteWebDriver.class);
		uiDriver1.setDriver(realDriver);
		WebUIDriver.getUxDriverSession().get().put("main", uiDriver1);

		WebDriver driver2 = WebUIDriver.getWebDriver(true);
		Assert.assertNotEquals(driver2, realDriver);
	}

	/**
	 * If BrowserType is not given, the type is taken from context parameter
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithDefaultType() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, null, "main", null);
		
		Assert.assertEquals( ((CustomEventFiringWebDriver)driver1).getCapabilities().getBrowserName(), "htmlunit");
	}
	
	/**
	 * When the same name is specified, the driver is not recreated
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWhenAlreadyExisting() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		
		Assert.assertEquals(driver1, driver2);
	}
	

	/**
	 * issue #304: Check that if we close all driver windows, recreating the same driver do not raise exception
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWhenAlreadyExistingThenClosed() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
        driver1.close();
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);

		Assert.assertNotEquals(driver1, driver2);
	}
	
	/**
	 * issue #304: Check error is raised if we try to switch to a closed driver
	 */
	@Test(groups={"ut"}, expectedExceptions = ScenarioException.class)
	public void testSwitchToClosedDriver() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "second", null);
		Assert.assertEquals(WebUIDriver.getWebDriver(false), driver2);
		driver1.close();
		WebUIDriver.switchToDriver("main");
	}
	
	/**
	 * Exception raised when driver name is not given
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testCreationWithoutName() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, null, null);	
	}
	
	@Test(groups={"ut"})
	public void testGetDriverWithoutBeingCreated() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		Assert.assertNull(WebUIDriver.getWebDriver(false));	
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithVideo() {
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");

		try (MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class)) {
			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.startVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(File.class),
					eq("videoCapture.avi"))).thenReturn(videoRecorder);

			WebDriver driver = WebUIDriver.getWebDriver(true);

			Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
			Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());
			Assert.assertEquals(WebUIDriver.getVideoRecorder().get(), videoRecorder);
		}
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithVideoInError() {
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");

		try (MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class)) {
			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.startVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(File.class),
					eq("videoCapture.avi"))).thenThrow(new ScenarioException("error"));

			WebDriver driver = WebUIDriver.getWebDriver(true);

			Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
			Assert.assertNull(WebUIDriver.getVideoRecorder().get());
		}
		
	}
	
	/**
	 * No error should be raised when cleaning non existing drivers
	 */
	@Test(groups={"ut"})
	public void testCleanUpNoDriverCreated() {
		WebUIDriver.cleanUp();
	}
	
	/**
	 * All drivers should be deleted
	 */
	@Test(groups={"ut"})
	public void testCleanUpMultipleBrowsers() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver1 = WebUIDriverFactory.getInstance("foo");
		WebUIDriver uiDriver2 = WebUIDriverFactory.getInstance("bar");
		uiDriver1.initInstance();
		uiDriver2.initInstance();
		uiDriver1.createRemoteWebDriver();
		uiDriver2.createRemoteWebDriver();

		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 2);
		WebUIDriver.cleanUp();

		Assert.assertNull(uiDriver1.getDriver());
		Assert.assertNull(uiDriver2.getDriver());
		Assert.assertNull(WebUIDriver.getUxDriverSession().get());
	}
	
	/**
	 * Check driver is quit
	 */
	@Test(groups={"ut"})
	public void testCleanUp() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = spy(WebUIDriver.getWebDriver(true));
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		uiDriver.setDriver(driver); // force the mocked driver
		Assert.assertNotNull(uiDriver.getDriver());
		
		WebUIDriver.cleanUp();
		verify(driver).quit();
		Assert.assertNull(uiDriver.getDriver());	
	}
	
	/**
	 * If driver has crashed, "driverEXited" is set to true, check no error is raised
	 */
	@Test(groups={"ut"})
	public void testCleanUpDriverExited() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = spy(WebUIDriver.getWebDriver(true));
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		uiDriver.setDriver(driver); // force the mocked driver
		((CustomEventFiringWebDriver)driver).setDriverExited();
		Assert.assertNotNull(uiDriver.getDriver());
		
		WebUIDriver.cleanUp();
		verify(driver, never()).quit();
		Assert.assertNull(uiDriver.getDriver());	
	}
	
	
	/**
	 * Check there are no errors if driver is null
	 */
	@Test(groups={"ut"})
	public void testCleanUpDriverNull() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = WebUIDriverFactory.getInstance("foo");
		uiDriver.initInstance();
		uiDriver.setDriver(null);
		
		WebUIDriver.cleanUp();	
	}
	
	/**
	 * Check that test does not stop even if 'quit()' raises an error
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithError() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = spy(WebUIDriver.getWebDriver(true));
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		uiDriver.setDriver(driver); // force the mocked driver
		Assert.assertNotNull(uiDriver.getDriver());

		doThrow(new WebDriverException("error")).when(driver).quit();
		
		WebUIDriver.cleanUp();
		verify(driver).quit();
		Assert.assertNull(uiDriver.getDriver());	
		
	}
	
	/**
	 * video is closed when driver is cleaned
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithVideoCapture() {

		try (MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class)) {
			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.startVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(File.class),
					eq("videoCapture.avi"))).thenReturn(videoRecorder);
			SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			WebUIDriver uiDriver = WebUIDriverFactory.getInstance("foo");
			uiDriver.initInstance();
			uiDriver.createRemoteWebDriver();
			VideoRecorder vRecorder = WebUIDriver.getVideoRecorder().get();

			Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());

			WebUIDriver.cleanUp();
			mockedCustomEventFiringWebDriver.verify(() -> CustomEventFiringWebDriver.stopVideoCapture(eq(DriverMode.LOCAL), eq(null), eq(vRecorder)));

			Assert.assertNull(WebUIDriver.getVideoRecorder().get());
		}
	}
	
	/**
	 * No error is raised when error happens closing video
	 * videoRecorder is also removed
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithVideoCaptureWithError() {

		try (MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class)) {
			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.startVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(File.class),
					eq("videoCapture.avi"))).thenReturn(videoRecorder);

			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.stopVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(VideoRecorder.class))).thenThrow(new NullPointerException("error"));

			SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			WebUIDriver uiDriver = WebUIDriverFactory.getInstance("foo");
			uiDriver.initInstance();
			uiDriver.createRemoteWebDriver();
			VideoRecorder vRecorder = WebUIDriver.getVideoRecorder().get();

			Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());

			WebUIDriver.cleanUp();
			mockedCustomEventFiringWebDriver.verify(() -> CustomEventFiringWebDriver.stopVideoCapture(eq(DriverMode.LOCAL), eq(null), eq(vRecorder)));

			Assert.assertNull(WebUIDriver.getVideoRecorder().get());
		}
	}

	@Test(groups={"ut"})
	public void testCleanUpWithVideoCaptureWithError2() {

		try (MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class)) {
			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.startVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(File.class),
					eq("videoCapture.avi"))).thenReturn(videoRecorder);

			mockedCustomEventFiringWebDriver.when(() -> CustomEventFiringWebDriver.stopVideoCapture(
					eq(DriverMode.LOCAL),
					eq(null),
					any(VideoRecorder.class))).thenThrow(new IOException("error"));

			SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
			SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
			WebUIDriver uiDriver = WebUIDriverFactory.getInstance("foo");
			uiDriver.initInstance();
			uiDriver.createRemoteWebDriver();
			VideoRecorder vRecorder = WebUIDriver.getVideoRecorder().get();

			Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());

			WebUIDriver.cleanUp();
			mockedCustomEventFiringWebDriver.verify(() -> CustomEventFiringWebDriver.stopVideoCapture(eq(DriverMode.LOCAL), eq(null), eq(vRecorder)));

			Assert.assertNull(WebUIDriver.getVideoRecorder().get());
		}
	}


	/**
	 * Check driver is quit
	 */
	@Test(groups={"ut"})
	public void testLogFinalDriverState() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		WebDriver mockedDriver = mock(FirefoxDriver.class);

		try (MockedConstruction<FirefoxDriverFactory> mockedFirefoxDriverFactory = mockConstruction(FirefoxDriverFactory.class, (firefoxDriverFactory, context) -> {
			when(firefoxDriverFactory.createWebDriver()).thenReturn(mockedDriver);
			when(((HasCapabilities)mockedDriver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "firefox", "browserVersion", "100.0")));
			when(mockedDriver.switchTo()).thenReturn(targetLocator);
		});
			 MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.WINDOWS);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.FIREFOX, List.of(new BrowserInfo(BrowserType.FIREFOX, "100"))));

			WebUIDriver.getWebDriver(true);
			WebUIDriver.logFinalDriversState(Reporter.getCurrentTestResult());
			verify(mockedDriver, times(2)).switchTo();
		}
	}

	/**
	 * In case browser / driver has died, do not log final states
	 */
	@Test(groups={"ut"})
	public void testLogFinalDriverStateDriverExited() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		WebDriver mockedDriver = mock(FirefoxDriver.class);

		try (MockedConstruction<FirefoxDriverFactory> mockedFirefoxDriverFactory = mockConstruction(FirefoxDriverFactory.class, (firefoxDriverFactory, context) -> {
			when(firefoxDriverFactory.createWebDriver()).thenReturn(mockedDriver);
			when(((HasCapabilities)mockedDriver).getCapabilities()).thenReturn(new MutableCapabilities(Map.of("browserName", "firefox", "browserVersion", "100.0")));
			when(mockedDriver.switchTo()).thenReturn(targetLocator);
		});
			 MockedStatic<OSUtility> mockedOsUtility = mockStatic(OSUtility.class)) {

			mockedOsUtility.when(OSUtility::getCurrentPlatorm).thenReturn(Platform.WINDOWS);
			mockedOsUtility.when(() -> OSUtility.getInstalledBrowsersWithVersion(false)).thenReturn(Map.of(BrowserType.FIREFOX, List.of(new BrowserInfo(BrowserType.FIREFOX, "100"))));

			WebDriver driver = WebUIDriver.getWebDriver(true);
			((CustomEventFiringWebDriver)driver).setDriverExited();
			WebUIDriver.logFinalDriversState(Reporter.getCurrentTestResult());
			verify(mockedDriver, never()).switchTo();
		}
	}

	@Test(groups={"ut"})
	public void testDriverSwitching() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);

		// last created driver has the focus
		Assert.assertEquals(WebUIDriver.getWebDriver(false), driver2);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "other");
		
		WebUIDriver.switchToDriver("main");
		Assert.assertEquals(WebUIDriver.getWebDriver(false), driver1);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "main");
	}
	
	/**
	 * Error should be raised if we try to switch to a closed driver
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDriverSwitchingWhenClosed() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "other", null);
		
		driver2.quit();
		WebUIDriver.switchToDriver("other");
	}
	
	/**
	 * Error should be raised if no driver exists
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testDriverSwitchingWithoutDriver() {
		WebUIDriver.switchToDriver("main");
	}
	
	/**
	 * A new WebUIDriver is the new default one
	 */
	@Test(groups={"ut"})
	public void testGetCurrentWebUiDriver() {
		WebUIDriver.getWebUIDriver(true, "foo");
		WebUIDriver uiDriver2 = WebUIDriver.getWebUIDriver(true, "bar");
		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 2);

		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "bar");
		Assert.assertEquals(WebUIDriver.getWebUIDriver(false), uiDriver2);
	}
	
	/**
	 * Null is returned when no WebUIDriver exist
	 */
	@Test(groups={"ut"})
	public void testGetNoWebUiDriver() {
		Assert.assertNull(WebUIDriver.getWebUIDriver(false));
	}
	
	/**
	 * Null is returned when no WebUIDriver exist for this name
	 */
	@Test(groups={"ut"})
	public void testGetNoWebUiDriver2() {
		WebUIDriver.getWebUIDriver(true, "bar");
		Assert.assertNull(WebUIDriver.getWebUIDriver(false, "foo"));
	}
	

	/**
	 * issue #297: check we get the current driver when creating a new page, instead of getting the default one
	 */
	@Test(groups={"ut"})
	public void testGetCurrentWebUiDriverFromPage() throws Exception {

		SeleniumTestsContextManager.getThreadContext().setBrowser("none");
		// this call is normally done on test start
		WebUIDriver.resetCurrentWebUiDriverName();

		try (MockedStatic<WebUIDriver> mockedWebUiDriver = mockStatic(WebUIDriver.class, Mockito.CALLS_REAL_METHODS)) {

			mockedWebUiDriver.when(() -> WebUIDriver.getWebDriver(eq(true), any(BrowserType.class), eq("main"), eq(null))).thenAnswer(
                    (Answer<?>) invocation -> {
                        WebUIDriver.setCurrentWebUiDriverName("main"); // as we mock driver creation, current driver name is never set
                        return eventDriver1;
                    }
            );
			mockedWebUiDriver.when(() -> WebUIDriver.getWebDriver(eq(true), any(BrowserType.class), eq("app"), any())).thenAnswer(
                    (Answer<?>) invocation -> {
                        WebUIDriver.setCurrentWebUiDriverName("app"); // as we mock driver creation, current driver name is never set
return eventDriver2;
                    }
            );

			// create a first page. This creates the default driver
			StubTestPage page1 = new StubTestPage();

			// create a new page linked to a new driver. This creates a new driver names "app"
			StubTestPage page2 = new StubTestPage(BrowserType.NONE);

			// create an other page which sould use the last created driver
			StubTestPage page3 = new StubTestPage();

			Assert.assertEquals(page1.getDriver(), eventDriver1);
			Assert.assertEquals(page2.getDriver(), eventDriver2);
			Assert.assertEquals(page3.getDriver(), eventDriver2); // last page uses the driver created in the previous page
		}
	}
	
	/**
	 * issue #303: check that if we recreate an existing driver, we get the same driver as before and we switch to it
	 */
	@Test(groups={"ut"})
	public void testGetCurrentWebUiDriverFromPage2() {

		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "main");
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "other");
		
		WebUIDriver.switchToDriver("main"); 
		Assert.assertEquals(WebUIDriver.getWebDriver(false), driver1);

		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "main");
		WebDriver driver3 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "other");
		Assert.assertEquals(driver2, driver3);
		
	}

	/**
	 * destroys the driver if one has been created
	 */
	@AfterMethod(groups={"ut", "it"}, alwaysRun=true)
	public void destroyDriver() {
		WebUIDriver.cleanUp();

		GenericTest.resetTestNGREsultAndLogger();
	}
}

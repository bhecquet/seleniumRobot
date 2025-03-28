/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.seleniumtests.browserfactory.mobile.*;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.browserfactory.AppiumLauncherFactory;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.reporter.ReporterTest;
import com.seleniumtests.uipage.htmlelements.TextFieldElement;
import com.seleniumtests.util.helper.WaitHelper;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSCommand;
import com.seleniumtests.util.osutility.OSUtility;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import kong.unirest.Unirest;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockConstruction;

//@PrepareForTest({AdbWrapper.class, AndroidDriver.class, MobileDeviceSelector.class, AppiumDriverFactory.class, AppiumLauncherFactory.class, Unirest.class, InstrumentsWrapper.class})
public class TestWebUiDriver extends ReporterTest {
	
	private static final Logger logger = SeleniumRobotLogger.getLogger(TestWebUiDriver.class);
	
	@Mock
	private AndroidDriver androidDriver;
	
	@Mock
	private Options driverOptions;
	
	@Mock
	private Timeouts timeouts;

	
	/**
	 * Test we can connect to a remote appium server (no local devices)
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testLocalAndroidDriverWithRemoteAppiumServer() throws Exception {

		List<MobileDevice> deviceList = new ArrayList<>();

		try (MockedConstruction mockedAdbWrapper = mockConstruction(AdbWrapper.class, (adbWrapper, context) -> {
			when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		});
			 MockedConstruction mockedAndroidDriver = mockConstruction(AndroidDriver.class, (androidDriver, context) -> {
				 when(androidDriver.manage()).thenReturn(driverOptions);
				 when(androidDriver.getContext()).thenReturn("NATIVE_APP");
				 when(androidDriver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "", Platform.ANY));
			 });
			 MockedStatic mockedAppiumLauncher = mockStatic(AppiumLauncherFactory.class);
		) {
			when(driverOptions.timeouts()).thenReturn(timeouts);

			SeleniumTestsContextManager.getThreadContext().setRunMode("local");
			SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4321/wd/hub/");
			SeleniumTestsContextManager.getThreadContext().setDeviceId("emulator-5556");
			SeleniumTestsContextManager.getThreadContext().setPlatform("android");
			SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("5.0");
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);

			createServerMock("GET", "/wd/hub/sessions", 200, "{}");

			ExistingAppiumLauncher appiumLauncher;

			appiumLauncher = spy(new ExistingAppiumLauncher("http://localhost:4321/wd/hub/"));
			mockedAppiumLauncher.when(() -> AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);

			WebUIDriver.getWebDriver(true);

			// 1 driver has been created
			Assert.assertEquals(mockedAndroidDriver.constructed().size(), 1);

			WebUIDriver.cleanUp();
			verify(appiumLauncher).stopAppium();
		}
	}
	
	@Test(groups={"it"})
	public void testLocaliOSDriverWithRemoteAppiumServer() throws Exception {

		List<MobileDevice> deviceList = new ArrayList<>();

		try (MockedConstruction mockedInstrument = mockConstruction(InstrumentsWrapper.class, (instrumentsWrapper, context) -> {
				when(instrumentsWrapper.parseIosDevices()).thenReturn(deviceList);
			});
			 MockedConstruction mockedIOSDriver = mockConstruction(IOSDriver.class, (iosDriver, context) -> {
				 when(iosDriver.manage()).thenReturn(driverOptions);
				 when(iosDriver.getContext()).thenReturn("NATIVE_APP");
				 when(iosDriver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "", Platform.ANY));
			 });
			 MockedStatic mockedAppiumLauncher = mockStatic(AppiumLauncherFactory.class);
		) {

			when(driverOptions.timeouts()).thenReturn(timeouts);

			SeleniumTestsContextManager.getThreadContext().setRunMode("local");
			SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4321/wd/hub/");
			SeleniumTestsContextManager.getThreadContext().setDeviceId("123456");
			SeleniumTestsContextManager.getThreadContext().setPlatform("ios");
			SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("13.0");
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_IOS);

			createServerMock("GET", "/wd/hub/sessions", 200, "{}");

			ExistingAppiumLauncher appiumLauncher;

			appiumLauncher = spy(new ExistingAppiumLauncher("http://localhost:4321/wd/hub/"));
			mockedAppiumLauncher.when(() -> AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);

			WebUIDriver.getWebDriver(true);

			Assert.assertEquals(mockedIOSDriver.constructed().size(), 1);
			WebUIDriver.cleanUp();
			verify(appiumLauncher).stopAppium();
		}
	}


	/**
	 * Check a Windows application can be tested
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testLocalWindowsDriverWithRemoteAppiumServer() throws Exception {

		try (
			 MockedConstruction mockedWindowsDriver = mockConstruction(AppiumDriver.class, (windowsDriver, context) -> {
				 when(windowsDriver.manage()).thenReturn(driverOptions);
				 when(windowsDriver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "", Platform.WINDOWS));
			 });
			 MockedStatic mockedAppiumLauncher = mockStatic(AppiumLauncherFactory.class);
		) {

			when(driverOptions.timeouts()).thenReturn(timeouts);

			SeleniumTestsContextManager.getThreadContext().setRunMode("local");
			SeleniumTestsContextManager.getThreadContext().setApp("myApp");
			SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4321/wd/hub/");
			SeleniumTestsContextManager.getThreadContext().setPlatform("windows");
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_WINDOWS);

			createServerMock("GET", "/wd/hub/sessions", 200, "{}");

			ExistingAppiumLauncher appiumLauncher;

			appiumLauncher = spy(new ExistingAppiumLauncher("http://localhost:4321/wd/hub/"));
			mockedAppiumLauncher.when(() -> AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);

			WebUIDriver.getWebDriver(true);

			Assert.assertEquals(mockedWindowsDriver.constructed().size(), 1);
			WebUIDriver.cleanUp();
			verify(appiumLauncher).stopAppium();
		}
	}

	/**
	 * Test we get an error when using remote appium and no deviceId is provided
	 * @throws Exception
	 */
	@Test(groups={"it"}, expectedExceptions = ConfigurationException.class)
	public void testLocalAndroidDriverWithRemoteAppiumServerAndNoDeviceId() throws Exception {

		List<MobileDevice> deviceList = new ArrayList<>();
		try (MockedConstruction mockedAdbWrapper = mockConstruction(AdbWrapper.class, (adbWrapper, context) -> {
			when(adbWrapper.getDeviceList()).thenReturn(deviceList);
		});
			 MockedConstruction mockedAndroidDriver = mockConstruction(AndroidDriver.class, (androidDriver, context) -> {
				 when(androidDriver.manage()).thenReturn(driverOptions);
				 when(androidDriver.getCapabilities()).thenReturn(new DesiredCapabilities("chrome", "", Platform.ANY));
			 });
			 MockedStatic mockedAppiumLauncher = mockStatic(AppiumLauncherFactory.class);
		) {

			when(driverOptions.timeouts()).thenReturn(timeouts);
			
			SeleniumTestsContextManager.getThreadContext().setRunMode("local");
			SeleniumTestsContextManager.getThreadContext().setAppiumServerUrl("http://localhost:4321/wd/hub/");
			SeleniumTestsContextManager.getThreadContext().setPlatform("android");
			SeleniumTestsContextManager.getThreadContext().setMobilePlatformVersion("5.0");
			SeleniumTestsContextManager.getThreadContext().setTestType(TestType.APPIUM_APP_ANDROID);
			
			createServerMock("GET", "/wd/hub/sessions", 200, "{}");

			ExistingAppiumLauncher appiumLauncher;

			appiumLauncher = spy(new ExistingAppiumLauncher("http://localhost:4321/wd/hub/"));
			mockedAppiumLauncher.when(() -> AppiumLauncherFactory.getInstance()).thenReturn(appiumLauncher);
			
			WebUIDriver.getWebDriver(true);
		} finally {
			WebUIDriver.cleanUp();
		}

	}

	/**
	 * Check that HAR capture file is present
	 * Check it contains one page per TestStep
	 *
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testHarCaptureExists() throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverExposedViaWebServer"});

		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverExposedViaWebServer", "main-networkCapture.har").toFile().exists());

		JSONObject json = new JSONObject(FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverExposedViaWebServer", "main-networkCapture.har").toFile(), StandardCharsets.UTF_8));
		JSONArray pages = json.getJSONObject("log").getJSONArray("pages");

		// 7 steps in HTML
		Assert.assertTrue(pages.length() >= 6, "content is: " + json.toString());
		List<String> pageNames = new ArrayList<>();
		for (Object page: pages.toList()) {
			pageNames.add(((Map<String, Object>)page).get("title").toString().trim());
		}

		Assert.assertTrue(pageNames.contains("_writeSomething"));
		Assert.assertTrue(pageNames.contains("_reset"));
		Assert.assertTrue(pageNames.contains("_sendKeysComposite"));
		Assert.assertTrue(pageNames.contains("_clickPicture"));

		// Check "driver-log-performance.txt file is empty (HAR has been produced)
		Assert.assertEquals(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverExposedViaWebServer", "driver-log-performance.txt").toFile().length(), 0);
	}

	@Test(groups={"it"})
	public void testHarCaptureExistsAndPerformanceLogsPresent() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.DEBUG, "driver");
			executeSubTest(1, new String[]{"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[]{"testDriverExposedViaWebServer"});

			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverExposedViaWebServer", "main-networkCapture.har").toFile().exists());

			// Check "driver-log-performance.txt file is empty (HAR has been produced)
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverExposedViaWebServer", "driver-log-performance.txt").toFile().length() > 0);
		} finally {
			System.clearProperty(SeleniumTestsContext.DEBUG);
		}
	}
	
	/**
	 * Check that browser logs are written to file (only available for chrome)
	 * 
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testBrowserLogsExists() throws Exception {

		try {
			System.setProperty(SeleniumTestsContext.DEBUG, "driver");
			executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriver"});
			
			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "chromedriver.log").toFile().exists());
//			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-browser.txt").toFile().exists());
//			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-client.txt").toFile().exists());
//			Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriver", "driver-log-driver.txt").toFile().exists());
		} finally {
			System.clearProperty(SeleniumTestsContext.DEBUG);
		}
	}
	

	@Test(groups={"it"})
	public void testLocalLaunchWithMultipleThreads() throws Exception {
		
		ReporterTest.executeSubTest(2, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverManualSteps", "testDriver"});
		
		String logs = readSeleniumRobotLogFile();
		
		// check drivers are created one after the other
		int firstDriverCreation = logs.indexOf("driver creation took"); // written when driver is created
		int firstDriverInit = logs.indexOf("driver mode:"); // written before creating driver
		int secondDriverCreation = logs.lastIndexOf("driver creation took");
		int secondDriverInit = logs.lastIndexOf("driver mode:"); 
		
		Assert.assertTrue(secondDriverInit > firstDriverCreation);
		Assert.assertTrue(secondDriverInit > firstDriverInit);
		Assert.assertTrue(secondDriverCreation > firstDriverCreation);
	}

	/**
	 * Check that HAR capture file is present in result with manual steps
	 *
	 * @throws Exception
	 */
	@Test(groups={"it"})
	public void testReportContainsHarCaptureWithManualSteps() throws Exception {

		executeSubTest(1, new String[] {"com.seleniumtests.it.stubclasses.StubTestClassForDriverTest"}, ParallelMode.METHODS, new String[] {"testDriverManualSteps"});

		Assert.assertTrue(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverManualSteps", "main-networkCapture.har").toFile().exists());
		JSONObject json = new JSONObject(FileUtils.readFileToString(Paths.get(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), "testDriverManualSteps", "main-networkCapture.har").toFile(), StandardCharsets.UTF_8));
		JSONArray pages = json.getJSONObject("log").getJSONArray("pages");
		Assert.assertEquals(pages.length(), 6);
		Assert.assertEquals(pages.getJSONObject(2).getString("title").trim(), "Write");
		Assert.assertEquals(pages.getJSONObject(3).getString("title").trim(), "Reset");
	}
	
	@Test(groups={"it"})
	public void testMultipleBrowserCreation() {

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// creates the first driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
		driver1.get("chrome://settings/");
		
		// creates the second driver
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.FIREFOX, "second", null);
		driver2.get("about:config");
		
		// last created driver has the focus
		Assert.assertEquals(WebUIDriver.getWebDriver(false), driver2);
		
		// created browser is of the requested type
		Assert.assertEquals(((CustomEventFiringWebDriver)driver1).getCapabilities().getBrowserName(), "chrome");
		Assert.assertEquals(((CustomEventFiringWebDriver)driver2).getCapabilities().getBrowserName(), "firefox");
	}
	
	/**
	 * Test we can switch between browsers
	 */
	@Test(groups={"it"})
	public void testMultiBrowserSwitching() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// creates the first driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
		driver1.get("chrome://settings/");
		
		// creates the second driver
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.FIREFOX, "second", null);
		driver2.get("about:config");
		
		WebUIDriver.switchToDriver("main");
		Assert.assertEquals(WebUIDriver.getWebDriver(false).getCurrentUrl(), "chrome://settings/");
		WebUIDriver.switchToDriver("second");
		Assert.assertEquals(WebUIDriver.getWebDriver(false).getCurrentUrl(), "about:config");
	}
	
	/**
	 * Check error is raised if we try to switch to a closed browser
	 */
	@Test(groups={"it"}, expectedExceptions=ScenarioException.class)
	public void testMultiBrowserCloseOneBrowser() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		// creates the first driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", null);
		driver1.get("chrome://settings/");
		
		// creates the second driver
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.FIREFOX, "second", null);
		driver2.get("about:config");
		
		driver1.quit();
		WebUIDriver.switchToDriver("main");
		
	}
	

	/**
	 * Check we can attach Selenium to a chrome process if it's launched with '--remote-debugging-port' option
	 */
	@Test(groups={"it"})
	public void testAttachExternalChromeBrowser() {

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		String path = browsers.get(BrowserType.CHROME).get(0).getPath();
		int port = GenericDriverTest.findFreePort();
		
		// create chrome browser with the right option
		new BrowserLauncher(BrowserType.CHROME, port, path, 0, null).run();
			
		// creates the driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", port);
		driver1.get("chrome://settings/");
		Assert.assertTrue(new TextFieldElement("search", By.id("search")).isElementPresent(3));
	}
	
	/**
	 * Be sure that we can still attach driver even if it takes a long time to start
	 * For chrome, max wait time is 1 minute
	 */
	@Test(groups={"it"})
	public void testAttachExternalChromeBrowserStartingLate() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		String path = browsers.get(BrowserType.CHROME).get(0).getPath();
		int port = GenericDriverTest.findFreePort();
		
		logger.info("will start browser in 15 secs");
		new BrowserLauncher(BrowserType.CHROME, port, path, 30, null).run();

		logger.info("Waiting for driver");
		
		// creates the driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", port);
		driver1.get("chrome://settings/");
		Assert.assertTrue(new TextFieldElement("search", By.id("search")).isElementPresent(3));
	}
	
	/**
	 * Disabled as it's just here to debug some cases
	 */
	@Test(groups={"it"}, enabled=false)
	public void testAttachExternalChromeBrowserNotPresent() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);

		// creates the driver
		WebUIDriver.getWebDriver(true, BrowserType.CHROME, "main", 12345);
	}
	
	/**
	 * Check we can attach Selenium to a internet explorer process if it's through API
	 * @throws Exception 
	 */
	@Test(groups={"it"})
	public void testAttachExternalIEBrowser() throws Exception {
		
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		exposeWebServer();
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("default");
		SeleniumTestsContextManager.getThreadContext().setEdgeIeMode(true);
		SeleniumTestsContextManager.getThreadContext().setInitialUrl(serverUrl);

		// creates the first driver so that an internet explorer process is created the right way
		new BrowserLauncher(BrowserType.INTERNET_EXPLORER, 0, null, 0, serverUrl).run();
		
		// attach a second driver to the previous browser
		WebDriver driverAttached = WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "second", 0);
		Assert.assertEquals(driverAttached.getCurrentUrl(), serverUrl); // check we are connected to the first started browser
		driverAttached.get("about:blank"); // check we can control the browser
	}

	/**
	 * Be sure that we can still attach driver even if it takes a long time to start
	 * For IE, max wait time is 2 minutes
	 * @throws Exception 
	 */
	@Test(groups={"it"})
	public void testAttachExternalInternetExplorerStartingLate() throws Exception {

		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}

		exposeWebServer();
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("default");
		SeleniumTestsContextManager.getThreadContext().setEdgeIeMode(true);
		SeleniumTestsContextManager.getThreadContext().setInitialUrl(serverUrl);
		
		logger.info("will start browser in 30 secs");
		new BrowserLauncher(BrowserType.INTERNET_EXPLORER, 0, null, 30, serverUrl).run();
		logger.info("Waiting for driver");
		
		// creates the driver
		WebDriver driverAttached = WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "second", 0);
		Assert.assertEquals(driverAttached.getCurrentUrl(), serverUrl); // check we are connected to the first started browser
		driverAttached.get("about:blank"); // check we can control the browser
	}

	/**
	 * Disabled as it's just here to debug some cases
	 */
	@Test(groups={"it"}, enabled=false)
	public void testAttachExternalIErowserNotPresent() {
		
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);

		// creates the driver
		WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "main", 0);
	}
	

	@Test(groups={"it"})
	public void testAttachExternalEdgeBrowser() {
		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}

		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		
		Map<BrowserType, List<BrowserInfo>> browsers = OSUtility.getInstalledBrowsersWithVersion();
		String path = browsers.get(BrowserType.EDGE).get(0).getPath();
		int port = GenericDriverTest.findFreePort();
		
		// create chrome browser with the right option
		new BrowserLauncher(BrowserType.EDGE, port, path, 0, null).run();
			
		// creates the driver
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.EDGE, "main", port);
		driver1.get("edge://settings/");
		Assert.assertTrue(new TextFieldElement("search", By.id("search_input")).isElementPresent(3));
	}
	
	@Test(groups={"it"})
	public void testAttachExternalEdgeIEBrowser() throws Exception {

		if (!SystemUtils.IS_OS_WINDOWS) {
			throw new SkipException("This test can only be done on Windows");
		}
		exposeWebServer();
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		SeleniumTestsContextManager.getThreadContext().setBrowser("iexplore");
		SeleniumTestsContextManager.getThreadContext().setEdgeUserProfilePath("default");
		SeleniumTestsContextManager.getThreadContext().setEdgeIeMode(true);
		SeleniumTestsContextManager.getThreadContext().setInitialUrl(serverUrl);

		// creates the first driver so that an internet explorer process is created the right way
		new BrowserLauncher(BrowserType.INTERNET_EXPLORER, 0, null, 0, serverUrl).run();
		
		// attach a second driver to the previous browser
		WebDriver driverAttached = WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "second", 0);
		Assert.assertEquals(driverAttached.getCurrentUrl(), serverUrl); // check we are connected to the first started browser
		driverAttached.get("about:blank"); // check we can control the browser
	}
	
	@AfterMethod(groups={"it"}, alwaysRun = true)
	public void closeBrowser() {
		try {
			WebUIDriver.cleanUp();
		} catch (WebDriverException e) {
			
		}
	}
	
	private class BrowserLauncher extends Thread {
		
		private String path;
		private int port;
		private int delay;
		private BrowserType browserType;
		private String startupUrl;
		
		public BrowserLauncher(BrowserType browserType, int port, String path, int delay, String startupUrl) {
			this.port = port;
			this.path = path;
			this.delay = delay;
			this.browserType = browserType;
			this.startupUrl = startupUrl;
		}
		
		public void run() {
			
			WaitHelper.waitForSeconds(delay);

			if (BrowserType.CHROME == browserType || BrowserType.EDGE == browserType) {
				// create chrome browser with the right option
				OSCommand.executeCommand(new String[] {path, "--remote-debugging-port=" + port, "--remote-allow-origins=*", "about:blank"});
			} else if (BrowserType.INTERNET_EXPLORER == browserType) {
				WebDriver driver = WebUIDriver.getWebDriver(true, BrowserType.INTERNET_EXPLORER, "main", null);
				if (startupUrl != null) {
					driver.get(startupUrl);
				}
			}
		 }
	}
}

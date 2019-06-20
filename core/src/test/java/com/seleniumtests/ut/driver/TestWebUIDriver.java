package com.seleniumtests.ut.driver;

import com.neotys.selenium.proxies.NLWebDriver;
import com.neotys.selenium.proxies.NLWebDriverFactory;
import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.connectors.selenium.SeleniumGridConnectorFactory;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.StatisticsStorage;
import com.seleniumtests.core.StatisticsStorage.DriverUsage;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.VideoRecorder;
import com.seleniumtests.reporter.logger.TestLogging;
import net.lightbody.bmp.BrowserMobProxy;
import org.mockito.Mock;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@PrepareForTest({NLWebDriverFactory.class, CustomEventFiringWebDriver.class, SeleniumGridConnectorFactory.class})
@PowerMockIgnore("javax.net.ssl.*")
public class TestWebUIDriver extends MockitoTest {
	
	@Mock
	private NLWebDriver neoloadDriver;
	
	@Mock
	private WebDriver drv1;
	
	@Mock
	private WebDriver drv2;
	
	@Mock
	private VideoRecorder videoRecorder;
	
	@Mock
	private CustomEventFiringWebDriver eventDriver;
	
	@Mock
	private SeleniumGridConnector gridConnector;
	
	@Mock
	private BrowserMobProxy mobProxy;

	/**
	 * When driver is created, no Neoload driver is instanciated if neoload parameters are not set
	 */
	@Test(groups={"ut"})
	public void testDriverCreation() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		Assert.assertNull(((CustomEventFiringWebDriver)driver).getNeoloadDriver());
		
		Capabilities caps = ((CustomEventFiringWebDriver)driver).getInternalCapabilities();
		Assert.assertNotNull(caps.getCapability(DriverUsage.START_TIME));
		Assert.assertNotNull(caps.getCapability(DriverUsage.STARTUP_DURATION));
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
	
	@Test(groups={"ut"})
	public void testConstructor() {
		WebUIDriver uiDriver = new WebUIDriver("foo");
		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 1);
		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "foo");
	}
	
	/**
	 * A new WebUIDriver is the new default one
	 */
	@Test(groups={"ut"})
	public void testMultipleConstructor() {
		WebUIDriver uiDriver1 = new WebUIDriver("foo");
		WebUIDriver uiDriver2 = new WebUIDriver("bar");
		Assert.assertEquals(WebUIDriver.getUxDriverSession().get().size(), 2);

		Assert.assertEquals(WebUIDriver.getCurrentWebUiDriverName(), "bar");
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithBrowserMob() {
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(false);
		Assert.assertNotNull(uiDriver.getConfig().getBrowserMobProxy());
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
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);

		// set connector to simulate the driver creation on grid
		SeleniumTestsContextManager.getThreadContext().setSeleniumGridConnector(gridConnector);
		when(gridConnector.getNodeUrl()).thenReturn("http://localhost:5555/");
		
		WebUIDriver uiDriver2 = spy(WebUIDriver.getWebUIDriver(true, "other"));
		WebUIDriver.getUxDriverSession().get().put("other", uiDriver2);
		doReturn(drv2).when(uiDriver2).createWebDriver();
		WebDriver driver2 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "other", null);
		Assert.assertNull(uiDriver1.getConfig().getRunOnSameNode());
		Assert.assertNotNull(uiDriver2.getConfig().getRunOnSameNode());

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
	 * If BrowserType is not given, the type is taken from context parameter
	 */
	@Test(groups={"ut"})
	public void testDriverCreationWithDefaultType() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, null, "main", null);
		
		Assert.assertTrue(((CustomEventFiringWebDriver)driver1).getWebDriver() instanceof HtmlUnitDriver);
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
	 * Exception raised when driver name is not given
	 */
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testCreationWithoutName() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, null, null);	
	}
	
	@Test(groups={"ut"})
	public void testGetDriverWithoutBeingCreated() {
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		Assert.assertNull(WebUIDriver.getWebDriver(false));	
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithVideo() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		
		PowerMockito.mockStatic(CustomEventFiringWebDriver.class);
		PowerMockito.doReturn(videoRecorder).when(CustomEventFiringWebDriver.class, "startVideoCapture", 
					eq(DriverMode.LOCAL), 
					eq(null), 
					any(File.class), 
					eq("videoCapture.avi"));
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());
		
	}
	
	@Test(groups={"ut"})
	public void testDriverCreationWithVideoInError() throws Exception {
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		
		PowerMockito.mockStatic(CustomEventFiringWebDriver.class);
		PowerMockito.doThrow(new ScenarioException("error")).when(CustomEventFiringWebDriver.class, "startVideoCapture", 
				eq(DriverMode.LOCAL), 
				eq(null), 
				any(File.class), 
				eq("videoCapture.avi"));
		
		WebDriver driver = WebUIDriver.getWebDriver(true);
		
		Assert.assertTrue(driver instanceof CustomEventFiringWebDriver);
		Assert.assertNull(WebUIDriver.getVideoRecorder().get());
		
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
		WebUIDriver uiDriver1 = new WebUIDriver("foo");
		WebUIDriver uiDriver2 = new WebUIDriver("bar");
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
		
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.setDriver(eventDriver);

		WebUIDriver.cleanUp();
		verify(eventDriver).quit();
		Assert.assertNull(uiDriver.getDriver());	
	}
	
	
	/**
	 * Check there are no errors if driver is null
	 */
	@Test(groups={"ut"})
	public void testCleanUpDriverNull() {
		
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.setDriver(null);
		
		WebUIDriver.cleanUp();	
	}
	
	/**
	 * Check that test does not stop even if 'quit()' raises an error
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithError() {
		
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.setDriver(eventDriver);
		doThrow(new WebDriverException("error")).when(eventDriver).quit();
		
		WebUIDriver.cleanUp();
		verify(eventDriver).quit();
		Assert.assertNull(uiDriver.getDriver());	
	}
	
	/**
	 * BrowserMob proxy is closed when driver is cleaned
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithBrowserMob() {
		
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.createRemoteWebDriver();
		Assert.assertNotNull(uiDriver.getConfig().getBrowserMobProxy());
		
		WebUIDriver.cleanUp();
		
		Assert.assertNull(uiDriver.getConfig().getBrowserMobProxy());
	}
	
	/**
	 * Check no error is raised even if ending browsermob proxy raises an error
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithBrowserMobInError() {
		
		SeleniumTestsContextManager.getThreadContext().setCaptureNetwork(true);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.createRemoteWebDriver();
		Assert.assertNotNull(uiDriver.getConfig().getBrowserMobProxy());
		
		// raise an error ending
		uiDriver.getConfig().setBrowserMobProxy(mobProxy);
		doThrow(new NullPointerException("error")).when(mobProxy).endHar();
		
		WebUIDriver.cleanUp();
		
		Assert.assertNull(uiDriver.getConfig().getBrowserMobProxy());
	}
	
	/**
	 * video is closed when driver is cleaned
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithVideoCapture() throws Exception {
		
		PowerMockito.mockStatic(CustomEventFiringWebDriver.class);
		PowerMockito.doReturn(videoRecorder).when(CustomEventFiringWebDriver.class, "startVideoCapture", 
					eq(DriverMode.LOCAL), 
					eq(null), 
					any(File.class), 
					eq("videoCapture.avi"));

		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.createRemoteWebDriver();
		VideoRecorder vRecorder = WebUIDriver.getVideoRecorder().get();
		
		Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());
		
		WebUIDriver.cleanUp();
		PowerMockito.verifyStatic(CustomEventFiringWebDriver.class, times(1));
		CustomEventFiringWebDriver.stopVideoCapture(eq(DriverMode.LOCAL), eq(null), eq(vRecorder));
		
		Assert.assertNull(WebUIDriver.getVideoRecorder().get());
	}
	
	/**
	 * No error is raised when error happens closing video
	 * videoRecorder is also removed
	 * @throws Exception 
	 */
	@Test(groups={"ut"})
	public void testCleanUpWithVideoCaptureWithError() throws Exception {
		
		PowerMockito.mockStatic(CustomEventFiringWebDriver.class);
		PowerMockito.doReturn(videoRecorder).when(CustomEventFiringWebDriver.class, "startVideoCapture", 
				eq(DriverMode.LOCAL), 
				eq(null), 
				any(File.class), 
				eq("videoCapture.avi"));
		PowerMockito.doThrow(new NullPointerException("error")).when(CustomEventFiringWebDriver.class, "stopVideoCapture", 
				eq(DriverMode.LOCAL), 
				eq(null), 
				any(VideoRecorder.class));
		
		SeleniumTestsContextManager.getThreadContext().setVideoCapture("true");
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		WebUIDriver uiDriver = new WebUIDriver("foo");
		uiDriver.createRemoteWebDriver();
		VideoRecorder vRecorder = WebUIDriver.getVideoRecorder().get();
		
		Assert.assertNotNull(WebUIDriver.getVideoRecorder().get());
		
		WebUIDriver.cleanUp();
		PowerMockito.verifyStatic(CustomEventFiringWebDriver.class, times(1));
		CustomEventFiringWebDriver.stopVideoCapture(eq(DriverMode.LOCAL), eq(null), eq(vRecorder));
		
		Assert.assertNull(WebUIDriver.getVideoRecorder().get());
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
		WebDriver driver1 = WebUIDriver.getWebDriver(true, BrowserType.HTMLUNIT, "main", null);
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
		WebUIDriver uiDriver1 = WebUIDriver.getWebUIDriver(true, "foo");
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
		WebUIDriver uiDriver = WebUIDriver.getWebUIDriver(true, "bar");
		Assert.assertNull(WebUIDriver.getWebUIDriver(false, "foo"));
	}

	/**
	 * destroys the driver if one has been created
	 */
	@AfterMethod(groups={"ut", "it"}, alwaysRun=true)
	public void destroyDriver() {
		WebUIDriver.cleanUp();

		TestLogging.reset();
	}
}

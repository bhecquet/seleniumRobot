/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.ut.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.awt.AWTError;
import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.seleniumtests.browserfactory.chrome.ChromiumUtils;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.WebSessionEndedException;
import com.seleniumtests.driver.*;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openqa.selenium.*;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.htmlunit.options.HtmlUnitDriverOptions;
import org.openqa.selenium.remote.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.browserfactory.SeleniumRobotCapabilityType;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;
import com.seleniumtests.util.video.VideoRecorder;

public class TestCustomEventFiringWebDriver extends MockitoTest {


	@Mock
	private RemoteWebDriver driver;

	@Mock
	private AndroidDriver mobileDriver;

	@Mock
	private Options options;
	
	@Mock
	private Window window;
	
	@Mock
	private OSUtility osUtility;
	
	@Mock
	private BrowserInfo browserInfo;

	@Mock 
	private SeleniumGridConnector gridConnector;
	
	@Mock
	private TargetLocator target;

	@Mock
	private Capabilities capabilities;
	
	@Mock
	private PointerInfo pointerInfo;

	@Mock
	private TargetLocator targetLocator;
	
	private CustomEventFiringWebDriver eventDriver;
	private CustomEventFiringWebDriver attachedEventDriver;

	private MockedStatic<OSUtilityFactory> mockedOsUtilityFactory;
	private MockedStatic<CustomEventFiringWebDriver> mockedCustomEventFiringWebDriver;
	private MockedStatic<MouseInfo> mockedMouse;


	@BeforeMethod(groups={"ut"})
	private void init() {

		when(driver.getCapabilities()).thenReturn(new DesiredCapabilities()); // add capabilities to allow augmenting driver
		
		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, null));
		attachedEventDriver = spy(new CustomEventFiringWebDriver(driver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, null, 12345, new ArrayList<>()));
		when(driver.manage()).thenReturn(options);
		when(driver.getCapabilities()).thenReturn(capabilities);
		when(driver.switchTo()).thenReturn(target);
		when(driver.getSessionId()).thenReturn(new SessionId("1234"));
		when(driver.getPageSource()).thenReturn("<html></html>");
		when(options.window()).thenReturn(window);
		when(window.getSize()).thenReturn(new Dimension(100, 100));
		
		mockedOsUtilityFactory = mockStatic(OSUtilityFactory.class);
		mockedOsUtilityFactory.when(OSUtilityFactory::getInstance).thenReturn(osUtility);

		mockedCustomEventFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class, CALLS_REAL_METHODS);
		mockedCustomEventFiringWebDriver.when(CustomEventFiringWebDriver::getScreensRectangle).thenReturn(new Rectangle(1900, 1000));

		mockedMouse = mockStatic(MouseInfo.class);
		mockedMouse.when(MouseInfo::getPointerInfo).thenReturn(pointerInfo);
		when(pointerInfo.getLocation()).thenReturn(new java.awt.Point(2, 3));
		CustomEventFiringWebDriver.resetVideoRecorder();

		when(mobileDriver.manage()).thenReturn(options);
		when(mobileDriver.getCapabilities()).thenReturn(capabilities);
		when(mobileDriver.switchTo()).thenReturn(target);
		when(mobileDriver.getPageSource()).thenReturn("<html></html>");
		when(mobileDriver.getCapabilities()).thenReturn(new DesiredCapabilities());
		when(mobileDriver.getContext()).thenReturn("WEB");
	}

	@AfterMethod(groups = "ut", alwaysRun = true)
	private void closeMocks() {
		mockedOsUtilityFactory.close();
		mockedCustomEventFiringWebDriver.close();
		mockedMouse.close();
	}

	@Test(groups = {"ut"})
	public void testConstructor() {

		WebDriver realDriver = new HtmlUnitDriver();
		try (MockedConstruction<Augmenter> mockedAugmenter = mockConstruction(Augmenter.class, (augmenter, context) ->
				doAnswer(invocation -> realDriver)
						.when(augmenter)
						.augment(any()))
		) {

			new CustomEventFiringWebDriver(realDriver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, null);
			verify(mockedAugmenter.constructed().get(0)).augment(realDriver);
		}
	}

	/**
	 * Check port for BiDi is updated (for testcontainers support)
	 */
	@Test(groups = {"ut"})
	public void testUpdateBidiPort() {
		MutableCapabilities caps = new HtmlUnitDriverOptions();
		caps.setCapability("se:cdp", "wss://localhost:12345/abcde/bla");
		caps.setCapability(SeleniumRobotCapabilityType.GRID_HUB, "https://localhost:4444/wd/hub");
		HtmlUnitDriver realDriver = spy(new HtmlUnitDriver());
		when(realDriver.getCapabilities()).thenReturn(caps);

		try (MockedConstruction<Augmenter> mockedAugmenter = mockConstruction(Augmenter.class, (augmenter, context) ->
				doAnswer(invocation -> realDriver)
						.when(augmenter)
						.augment(any()))
		) {

			CustomEventFiringWebDriver customEventFiringWebDriver = new CustomEventFiringWebDriver(realDriver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, null);
			Assert.assertEquals(customEventFiringWebDriver.getCapabilities().getCapability("se:cdp"), "wss://localhost:4444/abcde/bla");

		}
	}

	/**
	 * Check port for BiDi is updated (for testcontainers support) even if grid URL has no port defined (default port used)
	 */
	@Test(groups = {"ut"})
	public void testUpdateBidiPortNoPortInUrl() {
		MutableCapabilities caps = new HtmlUnitDriverOptions();
		caps.setCapability("se:cdp", "wss://localhost:444/abcde/bla");
		caps.setCapability(SeleniumRobotCapabilityType.GRID_HUB, "https://localhost/wd/hub");
		HtmlUnitDriver realDriver = spy(new HtmlUnitDriver());
		when(realDriver.getCapabilities()).thenReturn(caps);

		try (MockedConstruction<Augmenter> mockedAugmenter = mockConstruction(Augmenter.class, (augmenter, context) ->
				doAnswer(invocation -> realDriver)
						.when(augmenter)
						.augment(any()))
		) {

			CustomEventFiringWebDriver customEventFiringWebDriver = new CustomEventFiringWebDriver(realDriver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, null);
			Assert.assertEquals(customEventFiringWebDriver.getCapabilities().getCapability("se:cdp"), "wss://localhost:443/abcde/bla");

		}
	}

	@Test(groups = {"ut"})
	public void testGetSessionId() {
		Assert.assertEquals(eventDriver.getSessionId(), "1234");
	}
	
	/**
	 * Test the case where driver is not a RemoteWebDriver (htmlunit)
	 * We shoud get a generated id
	 */
	@Test(groups = {"ut"})
	public void testGetSessionIdWithIncompatibleDriver() {
		when(driver.getSessionId()).thenThrow(ClassCastException.class);
		Assert.assertNotEquals(eventDriver.getSessionId(), "1234");
	}

	/**
	 * Check that if a TimeoutException is returned when getting window handles, we consider driver/browser as died
	 */
	@Test(groups = {"ut"})
	public void testGetWindowHandlesInTimeout() {
		when(driver.getWindowHandles()).thenThrow(new TimeoutException());
		Set<String> handles = eventDriver.getWindowHandles();
		Assert.assertTrue(handles.isEmpty());
		Assert.assertTrue(eventDriver.isDriverExited());
	}

	/**
	 * If driver has already exited, do not try to reach it
	 */
	@Test(groups = {"ut"})
	public void testGetWindowHandlesDriverExited() {
		eventDriver.setDriverExited();
		Set<String> handles = eventDriver.getWindowHandles();
		Assert.assertTrue(handles.isEmpty());
		verify(driver, never()).getWindowHandles();
	}
	/**
	 * Check that if a UnreachableBrowserException is returned when getting window handles, we consider driver/browser as died
	 */
	@Test(groups = {"ut"})
	public void testGetWindowHandlesBrowserUnreachable() {
		when(driver.getWindowHandles()).thenThrow(new UnreachableBrowserException("not here"));
		Set<String> handles = eventDriver.getWindowHandles();
		Assert.assertTrue(handles.isEmpty());
		Assert.assertTrue(eventDriver.isDriverExited());
	}
	/**
	 * Check that if a UnreachableBrowserException is returned when getting window handles, we consider driver/browser as died
	 */
	@Test(groups = {"ut"})
	public void testGetWindowHandlesWebSessionEndedException() {
		when(driver.getWindowHandles()).thenThrow(new WebSessionEndedException());
		Set<String> handles = eventDriver.getWindowHandles();
		Assert.assertTrue(handles.isEmpty());
		Assert.assertTrue(eventDriver.isDriverExited());
	}

	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedWithBrowserPresent() {
		when(driver.getWindowHandles()).thenReturn(new TreeSet<>(List.of("12345")));
		Assert.assertFalse(eventDriver.isBrowserOrAppClosed());
	}
	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedWithoutBrowserWindow() {
		when(driver.getWindowHandles()).thenReturn(new TreeSet<>());
		Assert.assertTrue(eventDriver.isBrowserOrAppClosed());
	}
	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedWithoutBrowser() {
		when(driver.getWindowHandles()).thenReturn(new TreeSet<>(List.of("12345")));
		when(eventDriver.getSessionId()).thenThrow(NoSuchSessionException.class);
		Assert.assertTrue(eventDriver.isBrowserOrAppClosed());
	}
	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedWithoutBrowser2() {
		when(driver.getWindowHandles()).thenReturn(new TreeSet<>(List.of("12345")));
		when(eventDriver.getCapabilities()).thenThrow(UnsupportedCommandException.class);
		Assert.assertTrue(eventDriver.isBrowserOrAppClosed());
	}
	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedMobileApp() {
		when(mobileDriver.getWindowHandles()).thenReturn(new TreeSet<>()); // check we don't call getWindowHandles
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_ANDROID, DriverMode.LOCAL, null));

		Assert.assertFalse(eventDriver.isBrowserOrAppClosed());
	}
	@Test(groups = {"ut"})
	public void testIsBrowserOrAppClosedMobileAppWithWebContext() {
		when(mobileDriver.getWindowHandles()).thenReturn(new TreeSet<>()); // check we don't call getWindowHandles
		when(mobileDriver.getContext()).thenReturn("WEBVIEW");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_ANDROID, DriverMode.LOCAL, null));

		Assert.assertFalse(eventDriver.isBrowserOrAppClosed());
	}
	
	@Test(groups = {"ut"})
	public void testGetPageSource() {
		Assert.assertEquals(eventDriver.getPageSource(), "<html></html>");
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testFindElement() {
		eventDriver.findElement(By.id("el"));
		verify(driver).findElement(By.id("el"));
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testFindElements() {
		eventDriver.findElements(By.id("el"));
		verify(driver).findElements(By.id("el"));
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testGet() {
		eventDriver.get("http://foo");
		verify(driver).get("http://foo");
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testSwitchTo() {
		eventDriver.switchTo().defaultContent();
		verify(driver).switchTo();
	}
	@Test(groups = {"ut"})
	public void testSwitchToTimeout() {
		when(driver.switchTo()).thenReturn(targetLocator);
		when(targetLocator.defaultContent()).thenThrow(new TimeoutException());
		Assert.assertThrows(WebSessionEndedException.class, () -> {
			TargetLocator targetLocator = eventDriver.switchTo();
			targetLocator.defaultContent();
		});
		Assert.assertTrue(eventDriver.isDriverExited());
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testManage() {
		eventDriver.manage();
		verify(driver).manage();
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testGetScreenshotAs() {
		eventDriver.getScreenshotAs(OutputType.BASE64);
		verify(driver).getScreenshotAs(OutputType.BASE64);
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testExecuteScript() {
		eventDriver.executeScript("my script");
		verify(driver).executeScript("my script");
	}
	
	/**
	 * Check command is forwarded to underlying driver
	 */
	@Test(groups = {"ut"})
	public void testAsyncExecuteScript() {
		eventDriver.executeAsyncScript("my script");
		verify(driver).executeAsyncScript("my script");
	}
	
	/**
	 * Test the case where driver cannot return page source (case for mobile browsers or old edge versions for example)
	 */
	@Test(groups = {"ut"})
	public void testGetPageSourceWithIncompatibleDriver() {
		doThrow(new WebDriverException("some error")).when(driver).getPageSource();
		Assert.assertNull(eventDriver.getPageSource());
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimension() {
		Dimension dim = eventDriver.getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testContentDimension() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(List.of(120L, 80L));
		Dimension dim = eventDriver.getContentDimension();
		
		// no need to switch to default content if size is correctly returned
		verify(driver, never()).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithZoomFactor() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(List.of(120.5, 80.67));
		Dimension dim = eventDriver.getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #238: sometimes, when trying to capture screenshot, if browser context do not correspond to driver context 
	 * (we have switched to an iframe but never went back whereas displayed page is different), getContentDimension returns (0,0) which prevent screenshot from 
	 * being stored
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionNotGet() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(List.of(100, 0)).thenReturn(List.of(120, 80));
		Dimension dim = eventDriver.getContentDimension();
		
		verify(driver).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	@Test(groups = {"ut"})
	public void testContentDimensionNotGet2() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(List.of(0, 100)).thenReturn(List.of(120, 80));
		Dimension dim = eventDriver.getContentDimension();
		
		verify(driver).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionNonWebTest() {
		when(mobileDriver.executeScript(anyString())).thenReturn(List.of(120L, 80L));
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APP, DriverMode.LOCAL, null));

		Dimension dim = eventDriver.getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimensionWithoutScrollbar() {
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbar() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(120L).thenReturn(80L);
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		// no need to switch to default content if size is correctly returned
		verify(driver, never()).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #238: check that if at least 1 dimension is the max one, switch to default content to retrieve it
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarNotGet() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(100000L).thenReturn(120L).thenReturn(80L); // retry when getting width
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		verify(driver).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarNotGet2() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(120L).thenReturn(100000L).thenReturn(80L); // retry when getting height
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		verify(driver).switchTo();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #233: Test the case where JS_GET_VIEWPORT_SIZE_WIDTH and JS_GET_VIEWPORT_SIZE_HEIGHT returns 100000x100000 because dimension is not found
	 * In this case, return the browser size
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarNotReturned() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(100000L);
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 10000);
		Assert.assertEquals(dim.width, 2000);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarWithZoomFactor() {
		when(driver.executeScript(anyString(), eq(true))).thenReturn(120.5).thenReturn(80.67);
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}

	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithoutScrollbarNonWebTest() {
		when(mobileDriver.executeScript(anyString(), eq(true))).thenReturn(120L).thenReturn(80L);
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APP, DriverMode.LOCAL, null));
		Dimension dim = eventDriver.getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForScrollPosition() {
		Point point = eventDriver.getScrollPosition();
		
		// check we get the default position: (0,0)
		Assert.assertEquals(point.x, 0);
		Assert.assertEquals(point.y, 0);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testScrollPosition() {
		when(driver.executeScript(anyString())).thenReturn(List.of(120L, 80L));
		Point point = eventDriver.getScrollPosition();
		
		// check we get the scroll position
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testScrollPositionWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(List.of(120.5, 80.67));
		Point point = eventDriver.getScrollPosition();
		
		// check we get the window dimension
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"}, expectedExceptions=WebDriverException.class)
	public void testScrollPositionNonWebTest() {
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APP, DriverMode.LOCAL, null));
		when(driver.executeScript(anyString())).thenReturn(List.of(120L, 80L));
		eventDriver.getScrollPosition();

	}
	
	@Test(groups = {"ut"})
	public void testClose() {

		eventDriver.close();
		verify(driver).close();
	}
	
	/**
	 * With Internet Explorer, closing driver may lead to a NullPointerException
	 * Check its intercepted
	 */
	@Test(groups = {"ut"})
	public void testCloseWithNPE() {
		doThrow(new NullPointerException("no handles")).when(driver).close();
		eventDriver.close();
	}
	
	@Test(groups = {"ut"})
	public void testCloseWithAlerts() {
		doThrow(new UnhandledAlertException("alert present")).doNothing().when(driver).close();
		eventDriver.close();
		verify(driver).switchTo();
		verify(target).alert(); // check we switch to alert
	}
	
	/**
	 * Check driver is quit and all pids are killed
	 */
	@Test(groups = {"ut"})
	public void testQuit() {
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(List.of(1000L));
		
		eventDriver.quit();
		verify(osUtility).killProcess("1000", true);
	}
	
	/**
	 * issue #401: When we attach a chrome browser, check we get it's process PID
	 */
	@Test(groups = {"ut"})
	public void testQuitWithAttachedChromeBrowser() {
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(List.of(1000L));
		when(osUtility.getProcessIdByListeningPort(12345)).thenReturn(1001);
		
		attachedEventDriver.quit();
		verify(osUtility).killProcess("1000", true);
		verify(osUtility).killProcess("1001", true);
	}
	
	/**
	 * Check that even if error is raised when driver is quit, killing process is done
	 */
	@Test(groups = {"ut"})
	public void testQuitInErrorLocal() {
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(List.of(1000L));
		when(capabilities.getCapability(SeleniumRobotCapabilityType.GRID_NODE_URL)).thenReturn(null);
		doThrow(new WebDriverException("some error")).when(driver).quit();
		
		try {
			eventDriver.quit();
		} catch (WebDriverException e) {
			// ignore
		}
		verify(osUtility).killProcess("1000", true);
		
		// this test is "local", so no node is available, we won't try to stop session on grid node
		verify(gridConnector, never()).stopSession("1234");
	}
	
	/**
	 * Check that even if error is raised when driver is quit, session stop is done at node level
	 */
	@Test(groups = {"ut"})
	public void testQuitInErrorOnGrid() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, browserInfo, TestType.WEB, DriverMode.LOCAL, gridConnector));
		
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(List.of(1000L));
		when(capabilities.getCapability(SeleniumRobotCapabilityType.GRID_NODE_URL)).thenReturn("http://grid-node:5555");
		doThrow(new WebDriverException("some error")).when(driver).quit();
		
		try {
			eventDriver.quit();
		} catch (WebDriverException e) {
			//
		}
		verify(osUtility).killProcess("1000", true);
		
		// this test is "local", so no node is available, we won't try to stop session on grid node
		verify(gridConnector).stopSession("1234");
	}

	@Test(groups = {"ut"})
	public void testIsWebTestDesktop() {
		eventDriver.setTestType(TestType.WEB);
		Assert.assertTrue(eventDriver.isWebTest());
	}
	@Test(groups = {"ut"})
	public void testIsWebTestWindowsApp() {
		eventDriver.setTestType(TestType.APPIUM_APP_WINDOWS);
		Assert.assertFalse(eventDriver.isWebTest());
	}

	@Test(groups = {"ut"})
	public void testIsWebTestMobileWeb() {
		when(eventDriver.getContext()).thenReturn("WEBVIEW");
		eventDriver.setTestType(TestType.APPIUM_WEB_ANDROID);
		Assert.assertTrue(eventDriver.isWebTest());
	}
	@Test(groups = {"ut"})
	public void testIsWebTestMobileApp() {
		when(eventDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver.setTestType(TestType.APPIUM_APP_IOS);
		Assert.assertFalse(eventDriver.isWebTest());
	}
	
	/**
	 * Test mouse coordinates in local mode
	 */
	@Test(groups = {"ut"})
	public void testMouseCoordinatesOnDesktop() {
		CustomEventFiringWebDriver.getMouseCoordinates(DriverMode.LOCAL, gridConnector);
		
		verify(pointerInfo).getLocation();
		verify(gridConnector, never()).getMouseCoordinates();
	}
	
	/**
	 * Test mouse coordinates in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testMouseCoordinatesOnDesktopWithoutDesktop() {
		mockedMouse.when(MouseInfo::getPointerInfo).thenThrow(new HeadlessException());
		CustomEventFiringWebDriver.getMouseCoordinates(DriverMode.LOCAL, gridConnector);
	}

	/**
	 * Test mouse coordinates in grid mode
	 */
	@Test(groups = {"ut"})
	public void testMouseCoordinatesOnDesktopWithGrid() {
		CustomEventFiringWebDriver.getMouseCoordinates(DriverMode.GRID, gridConnector);
		

		verify(pointerInfo, never()).getLocation();
		verify(gridConnector).getMouseCoordinates();
	}
	
	/**
	 * Test left click in local mode
	 */
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktop() {

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
			Robot robot = mockedRobot.constructed().get(0);

			verify(robot).mousePress(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).leftClic(anyBoolean(), anyInt(), anyInt());

			// video not requested
			Assert.assertEquals(mockedVideoRecorder.constructed().size(), 0);
		}
	}

	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopWithVideoCapture() throws IOException {

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			 MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = mockedRobot.constructed().get(0);
			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(robot).mousePress(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).leftClic(anyBoolean(), anyInt(), anyInt());

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopWithVideoCaptureAndError() throws IOException {

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> doThrow(AWTError.class).when(mock).mousePress(InputEvent.BUTTON1_DOWN_MASK));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);

			try {
				CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
			} catch (AWTError e) {
				//
			}

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopMainScreen() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.leftClicOnDesktopAt(true, 0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = (Robot) mockedRobot.constructed().get(0);
			verify(robot).mousePress(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).leftClic(anyBoolean(), anyInt(), anyInt());
		}
	}
	
	/**
	 * Test left click in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickOnDesktopWithoutDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstructionWithAnswer(Robot.class, invocation -> {
			throw new AWTException("");
		})) {
			CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
		}
	}
	
	/**
	 * Test left click with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickWithDeviceProviders() {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * Test left click in grid mode
	 */
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopWithGrid() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.GRID, gridConnector);

			// in GRID mode, Robot is never called
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).leftClic(false, 0, 0);
		}
	}
	
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopWithGridMainScreen() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.leftClicOnDesktopAt(true, 0, 0, DriverMode.GRID, gridConnector);

			// in GRID mode, Robot is never called
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).leftClic(true, 0, 0);
		}
	}
	
	/**
	 * Test double click in local mode
	 */
	@Test(groups = {"ut"})
	public void testDoubleClickOnDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			 MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot =  mockedRobot.constructed().get(0);
			verify(robot, times(2)).mousePress(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot, times(2)).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).doubleClick(anyBoolean(), anyInt(), anyInt());

			// video recorder not called as no video is requested
			Assert.assertEquals(mockedVideoRecorder.constructed().size(), 0);
		}
	}

	@Test(groups = {"ut"})
	public void testDoubleClickOnDesktopWithVideoCapture() throws IOException {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			 MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = (Robot) mockedRobot.constructed().get(0);
			VideoRecorder videoRecorder = (VideoRecorder) mockedVideoRecorder.constructed().get(0);
			verify(robot, times(2)).mousePress(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot, times(2)).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).doubleClick(anyBoolean(), anyInt(), anyInt());

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	@Test(groups = {"ut"})
	public void testDoubleClickOnDesktopWithVideoCaptureAndError() throws IOException {

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> doThrow(AWTError.class).when(mock).mousePress(InputEvent.BUTTON1_DOWN_MASK));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);

			try {
				CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
			} catch (AWTError e) {
				//
			}

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	/**
	 * Test double click in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testDoubleClickOnDesktopWithoutDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstructionWithAnswer(Robot.class, invocation -> {
			throw new AWTException("");
		})) {
			CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
		}
	}
	
	/**
	 * Test double click with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testDoubleClickWithDeviceProviders() {
		CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * Test double click in grid mode
	 */
	@Test(groups = {"ut"})
	public void testDoubleClickOnDesktopWithGrid() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.doubleClickOnDesktopAt(0, 0, DriverMode.GRID, gridConnector);

			// in GRID mode, Robot is never called
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).doubleClick(false, 0, 0);
		}
	}
	
	@Test(groups = {"ut"})
	public void testDoubleClickOnDesktopWithGridMainScreen() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.doubleClickOnDesktopAt(true, 0, 0, DriverMode.GRID, gridConnector);

			// in GRID mode, Robot is never called
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).doubleClick(true, 0, 0);
		}
	}
	
	/**
	 * Test right clic in local mode, no video recording
	 */
	@Test(groups = {"ut"})
	public void testRightClickOnDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			 MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = mockedRobot.constructed().get(0);
			verify(robot).mousePress(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).rightClic(anyBoolean(), anyInt(), anyInt());

			// video recorder not called as no video is requested
			Assert.assertEquals(mockedVideoRecorder.constructed().size(), 0);
		}
	}

	@Test(groups = {"ut"})
	public void testRightClickOnDesktopWithVideoCapture() throws IOException {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class);
			 MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = mockedRobot.constructed().get(0);
			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(robot).mousePress(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).rightClic(anyBoolean(), anyInt(), anyInt());

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	@Test(groups = {"ut"})
	public void testRightClickOnDesktopWithVideoCaptureAndError() throws IOException {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> doThrow(AWTError.class).when(mock).mousePress(InputEvent.BUTTON1_DOWN_MASK));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			try {
				CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
			} catch (AWTError e) {
				//
			}

			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	@Test(groups = {"ut"})
	public void testRightClickOnDesktopMainScreen() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.rightClicOnDesktopAt(true, 0, 0, DriverMode.LOCAL, gridConnector);

			Robot robot = mockedRobot.constructed().get(0);
			verify(robot).mousePress(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			verify(robot).mouseMove(0, 0);
			verify(gridConnector, never()).rightClic(anyBoolean(), anyInt(), anyInt());
		}
	}
	
	/**
	 * Test right clic in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickOnDesktopWithoutDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstructionWithAnswer(Robot.class, invocation -> {
			throw new AWTException("");
		})) {
			CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
		}
	}

	/**
	 * Test right click with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickWithDeviceProviders() {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * Test right clic in grid mode
	 */
	@Test(groups = {"ut"})
	public void testRightClickOnDesktopWithGrid() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.GRID, gridConnector);

			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).rightClic(false, 0, 0);
		}
	}
	
	@Test(groups = {"ut"})
	public void testRightClickOnDesktopWithGridMainScreen() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.rightClicOnDesktopAt(true, 0, 0, DriverMode.GRID, gridConnector);

			// check Robot is never created in GRID mode
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).rightClic(true, 0, 0);
		}
	}
	
	/**
	 * write to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testWriteToDesktop() {
		try (MockedConstruction<Keyboard> mockedKeyboard = mockConstruction(Keyboard.class)) {

			CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.LOCAL, gridConnector);

			Keyboard keyboard = mockedKeyboard.constructed().get(0);
			verify(keyboard).typeKeys("text");
			verify(gridConnector, never()).writeText(anyString());
		}
	}

	/**
	 * Write text in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteToDesktopWithoutDesktop() {
		try (MockedConstruction<Keyboard> mockedKeyboard = mockConstructionWithAnswer(Keyboard.class, invocation -> {
			throw new AWTException("");
		})) {
			CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.LOCAL, gridConnector);
		}
	}

	/**
	 * write textk with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteTextWithDeviceProviders() {
		CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * write to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testWriteToDesktopWithGrid() {
		try (MockedConstruction<Keyboard> mockedKeyboard = mockConstruction(Keyboard.class)) {
			CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.GRID, gridConnector);

			// check that in grid mode, Keyboard is never called
			Assert.assertEquals(mockedKeyboard.constructed().size(), 0);
			verify(gridConnector).writeText("text");
		}
	}
	
	/**
	 * write to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testDisplayRunningStep() {
		VideoRecorder videoRecorder = mock(VideoRecorder.class);
		long duration = CustomEventFiringWebDriver.displayStepOnScreen("text", DriverMode.LOCAL, gridConnector, videoRecorder);

		Assert.assertEquals(duration, 0);
		verify(videoRecorder).displayRunningStep("text");
		verify(gridConnector, never()).displayRunningStep(anyString());
	}
	
	/**
	 * write text with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testDisplayRunningStepWithDeviceProviders(){
		VideoRecorder videoRecorder = new VideoRecorder(new File("video"), "foo.avi");
		CustomEventFiringWebDriver.displayStepOnScreen("text", DriverMode.SAUCELABS, gridConnector, videoRecorder);
	}
	
	/**
	 * write to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testDisplayRunningStepWithGrid() {
		VideoRecorder videoRecorder = mock(VideoRecorder.class);
		when(gridConnector.displayRunningStep(anyString())).thenReturn(10L);
		long duration = CustomEventFiringWebDriver.displayStepOnScreen("text", DriverMode.GRID, gridConnector, videoRecorder);

		Assert.assertEquals(duration, 10);
		verify(videoRecorder, never()).displayRunningStep("text");
		verify(gridConnector).displayRunningStep(anyString());
	}

	/**
	 * write to desktop in browserstack mode
	 */
	@Test(groups = {"ut"})
	public void testDisplayRunningStepWithBrowserstack() {
		VideoRecorder videoRecorder = mock(VideoRecorder.class);
		long duration = CustomEventFiringWebDriver.displayStepOnScreen("text", DriverMode.BROWSERSTACK, gridConnector, videoRecorder);

		Assert.assertEquals(duration, 0);
		verify(videoRecorder, never()).displayRunningStep("text");
		verify(gridConnector, never()).displayRunningStep(anyString());
	}
	
	/**
	 * send keys to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testSendKeysToDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.sendKeysToDesktop(List.of(10, 20), DriverMode.LOCAL, gridConnector);

			Robot robot = mockedRobot.constructed().get(0);
			verify(robot).keyPress(10);
			verify(robot).keyPress(20);
			verify(robot).keyRelease(10);
			verify(robot).keyRelease(20);
			verify(gridConnector, never()).sendKeysWithKeyboard(any(List.class));
		}
	}

	/**
	 * Write text in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysToDesktopWithoutDesktop(){
		try (MockedConstruction<Robot> mockedRobot = mockConstructionWithAnswer(Robot.class, invocation -> {
			throw new AWTException("");
		})) {
			CustomEventFiringWebDriver.sendKeysToDesktop(List.of(10, 20), DriverMode.LOCAL, gridConnector);
		}
	}

	/**
	 * send keys with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysWithDeviceProviders(){
		CustomEventFiringWebDriver.sendKeysToDesktop(List.of(10, 20), DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * send keys to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testSendKeysToDesktopWithGrid() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class)) {
			CustomEventFiringWebDriver.sendKeysToDesktop(List.of(10, 20), DriverMode.GRID, gridConnector);

			// check we do not create robot as we are not LOCAL
			Assert.assertEquals(mockedRobot.constructed().size(), 0);
			verify(gridConnector).sendKeysWithKeyboard(any(List.class));
		}
	}
	
	/**
	 * capture picture in local mode, without video capture
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktop() throws IOException {
		File imageFile = File.createTempFile("image-", ".png");
		imageFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), imageFile);
		BufferedImage bi = ImageIO.read(imageFile);

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(bi));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {

			String b64img = CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
			Assert.assertTrue(b64img.startsWith("iVBORw0KGgoAAAAN"));
			verify(gridConnector, never()).captureDesktopToBuffer();

			Assert.assertEquals(mockedVideoRecorder.constructed().size(), 0);
		}
	}
	
	/**
	 * Check that step display is disabled / enabled when video capture has started
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktopWithVideoCapture() throws IOException {

		File imageFile = File.createTempFile("image-", ".png");
		imageFile.deleteOnExit();
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), imageFile);
		BufferedImage bi = ImageIO.read(imageFile);

		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> when(mock.createScreenCapture(any(Rectangle.class))).thenReturn(bi));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {

			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			String b64img = CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
			Assert.assertTrue(b64img.startsWith("iVBORw0KGgoAAAAN"));
			verify(gridConnector, never()).captureDesktopToBuffer();

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	/**
	 * Check that step display is disabled / enabled when video capture has started even if error occurs
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktopWithVideoCaptureAndError() throws IOException {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> when(mock.createScreenCapture(any(Rectangle.class))).thenThrow(ScenarioException.class));
             MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)
		) {
			File imageFile = File.createTempFile("image-", ".png");
			imageFile.deleteOnExit();
			FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), imageFile);

			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			try {
				CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
			} catch (ScenarioException e) {
				//
			}
			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(videoRecorder).disableStepDisplay();
			verify(videoRecorder).enableStepDisplay();
		}
	}
	
	/**
	 * capture picture in local headless mode, ScenarioException should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithoutDesktop() {
		try (MockedConstruction<Robot> mockedRobot = mockConstruction(Robot.class, (mock, context) -> when(mock.createScreenCapture(any(Rectangle.class))).thenThrow(AWTError.class))) {

			CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
		}
	}
	
	/**
	 * capture desktop with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithDeviceProviders(){
		CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.SAUCELABS, gridConnector);
	}
	

	/**
	 * capture picture in grid mode
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktopWithGrid() {

		CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.GRID, gridConnector);
		verify(gridConnector).captureDesktopToBuffer();
	}

	/**
	 * start video capture to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testStartVideoCaptureToDesktop() throws IOException {
		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(videoRecorder).start();
			verify(gridConnector, never()).startVideoCapture();
			Assert.assertFalse(CustomEventFiringWebDriver.getVideoRecorders().isEmpty());
		}
    }

	/**
	 * start video capture in headless mode: an error should be raised because there is no session
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureToDesktopWithoutDesktop() throws IOException {

		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstructionWithAnswer(VideoRecorder.class, invocation -> {
			throw new HeadlessException();
		})) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");
		}
	}

	/**
	 * start video capture with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureWithDeviceProviders() throws IOException {
		File videoFolder = File.createTempFile("video", ".avi").getParentFile();
		CustomEventFiringWebDriver.startVideoCapture(DriverMode.SAUCELABS, gridConnector, videoFolder, "video.avi");
	}
	
	/**
	 * start video capture to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testStartVideoCaptureToDesktopWithGrid() throws IOException {
		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.GRID, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(videoRecorder, never()).start();
			verify(gridConnector).startVideoCapture();
			Assert.assertTrue(CustomEventFiringWebDriver.getVideoRecorders().isEmpty());
		}
	}
	/**
	 * start video capture to desktop in browserstack mode
	 */
	@Test(groups = {"ut"})
	public void testStartVideoCaptureToDesktopWithBrowserStack() throws IOException {
		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.BROWSERSTACK, gridConnector, videoFolder, "video.avi");

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			verify(videoRecorder, never()).start();
			verify(gridConnector).startVideoCapture();
			Assert.assertTrue(CustomEventFiringWebDriver.getVideoRecorders().isEmpty());
		}
	}
	
	/**
	 * stop video capture to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktop() throws IOException {
		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			VideoRecorder videoRecorder = new VideoRecorder(new File("video"), "foo.avi");
			CustomEventFiringWebDriver.stopVideoCapture(DriverMode.LOCAL, gridConnector, videoRecorder);

			verify(videoRecorder).stop();
			verify(gridConnector, never()).stopVideoCapture(anyString());
		}
	}
	
	/**
	 * stop video capture to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktop2() throws IOException {
		try (MockedConstruction<VideoRecorder> mockedVideoRecorder = mockConstruction(VideoRecorder.class)) {
			File videoFolder = File.createTempFile("video", ".avi").getParentFile();
			CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");
			Assert.assertFalse(CustomEventFiringWebDriver.getVideoRecorders().isEmpty());

			VideoRecorder videoRecorder = mockedVideoRecorder.constructed().get(0);
			CustomEventFiringWebDriver.stopVideoCapture(DriverMode.LOCAL, gridConnector, videoRecorder);

			verify(videoRecorder).stop();
			verify(gridConnector, never()).stopVideoCapture(anyString());

			Assert.assertTrue(CustomEventFiringWebDriver.getVideoRecorders().isEmpty());
		}
	}
	
	/**
	 * stop video capture whereas it has not been started, ScenarioException should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureIfNotStarted() throws IOException {
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.LOCAL, gridConnector, null);
	}

	/**
	 * stop video capture with device providers: this is not supported, so exception should be raised
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureWithDeviceProviders() throws IOException {
		VideoRecorder videoRecorder = new VideoRecorder(new File("video"), "foo.avi");
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.SAUCELABS, gridConnector, videoRecorder);
	}
	
	/**
	 * stop video capture to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktopWithGrid() throws IOException {

		VideoRecorder videoRecorder = mock(VideoRecorder.class);
		File videoFile = File.createTempFile("video", ".avi");
		when(videoRecorder.getFolderPath()).thenReturn(videoFile.getParentFile());
		when(videoRecorder.getFileName()).thenReturn(videoFile.getName());
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.GRID, gridConnector, videoRecorder);
		
		verify(videoRecorder, never()).start();
		verify(gridConnector).stopVideoCapture(anyString());
	}

	/**
	 * stop video capture to desktop in browserstack mode
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktopWithBrowserstack() throws IOException {

		VideoRecorder videoRecorder = mock(VideoRecorder.class);
		File videoFile = File.createTempFile("video", ".avi");
		when(videoRecorder.getFolderPath()).thenReturn(videoFile.getParentFile());
		when(videoRecorder.getFileName()).thenReturn(videoFile.getName());
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.BROWSERSTACK, gridConnector, videoRecorder);

		verify(videoRecorder, never()).start();
		verify(gridConnector).stopVideoCapture(anyString());
	}
	
	/**
	 * Check we never get null, we update if not initialized
	 */
	@Test(groups = {"ut"})
	public void testGetCurrentHandlesUpdated() {
		when(driver.getWindowHandles()).thenReturn(new TreeSet<>(List.of("12345", "67890")));
		Assert.assertEquals(eventDriver.getCurrentHandles(), new TreeSet<>(List.of("12345", "67890")));
		verify(eventDriver, times(1)).updateWindowsHandles();
	}
	@Test(groups = {"ut"})
	public void testGetCurrentHandlesNotUpdated() {
		eventDriver.setCurrentHandles(new TreeSet<>(List.of("12345", "67890")));
		Assert.assertEquals(eventDriver.getCurrentHandles(), new TreeSet<>(List.of("12345", "67890")));
		verify(eventDriver, never()).updateWindowsHandles();
	}

	@Test(groups = {"ut"})
	public void testHideKeyboardMobile() {
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_IOS, DriverMode.LOCAL, null));
		eventDriver.hideKeyboard();
		verify(((HidesKeyboard)mobileDriver), never()).hideKeyboard();
	}

	@Test(groups = {"ut"})
	public void testHideKeyboardWebMobile() {
		when(mobileDriver.getContext()).thenReturn("WEBVIEW");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_WEB_ANDROID, DriverMode.LOCAL, null));
		eventDriver.hideKeyboard();
		verify(((HidesKeyboard)mobileDriver)).hideKeyboard();
	}

	@Test(groups = {"ut"})
	public void testHideKeyboardDesktop() {
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.WEB, DriverMode.LOCAL, null));
		eventDriver.hideKeyboard();
		verify(((HidesKeyboard)mobileDriver), never()).hideKeyboard();
	}

	@Test(groups = {"ut"})
	public void testContextMobile() {
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_IOS, DriverMode.LOCAL, null));
		WebDriver wd = eventDriver.context("foo");
		Assert.assertEquals(wd, eventDriver);
		verify(((SupportsContextSwitching)mobileDriver)).context("foo");
	}

	@Test(groups = {"ut"})
	public void testContextDesktop() {
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.WEB, DriverMode.LOCAL, null));
		WebDriver wd = eventDriver.context("foo");
		Assert.assertEquals(wd, eventDriver);
		verify(((SupportsContextSwitching)mobileDriver), never()).context("foo");
	}

	@Test(groups = {"ut"})
	public void testContextHandlesMobile() {
		when(mobileDriver.getContextHandles()).thenReturn(new TreeSet<>(List.of("NATIVE_APP", "WEBVIEW")));
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_IOS, DriverMode.LOCAL, null));
		Set<String> handles = eventDriver.getContextHandles();
		verify(((SupportsContextSwitching)mobileDriver)).getContextHandles();
		Assert.assertEquals(handles.size(), 2);
		Assert.assertTrue(handles.contains("NATIVE_APP"));
		Assert.assertTrue(handles.contains("WEBVIEW"));
	}

	@Test(groups = {"ut"})
	public void testContextHandlesDesktop() {
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.WEB, DriverMode.LOCAL, null));
		eventDriver.context("foo");
		Set<String> handles = eventDriver.getContextHandles();
		verify(((SupportsContextSwitching)mobileDriver), never()).getContextHandles();
		Assert.assertEquals(handles.size(), 1);
		Assert.assertTrue(handles.contains("WEB"));
	}


	@Test(groups = {"ut"})
	public void testGetContextMobile() {
		when(mobileDriver.getContext()).thenReturn("NATIVE_APP");
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_IOS, DriverMode.LOCAL, null));
		String context = eventDriver.getContext();
		Assert.assertEquals(context, "NATIVE_APP");
		verify(((SupportsContextSwitching)mobileDriver), times(2)).getContext(); // one for isWebTest(), one for getContext()
	}

	@Test(groups = {"ut"})
	public void testGetContextDesktop() {
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.WEB, DriverMode.LOCAL, null));
		String context = eventDriver.getContext();
		Assert.assertEquals(context, "WEB");
		verify(((SupportsContextSwitching)mobileDriver), never()).getContext();
	}

	@Test(groups = {"ut"})
	public void testGetContextWindowsApp() {
		eventDriver = spy(new CustomEventFiringWebDriver(mobileDriver, null, null, TestType.APPIUM_APP_WINDOWS, DriverMode.LOCAL, null));
		String context = eventDriver.getContext();
		Assert.assertEquals(context, "APP");
		verify(((SupportsContextSwitching)mobileDriver), never()).getContext();
	}

	@Test(groups = {"ut"})
	public void testGetCurrentUrl() {
		when(driver.getCurrentUrl()).thenReturn("http://google.com");
		Assert.assertEquals(eventDriver.getCurrentUrl(), "http://google.com");
	}

	/**
	 * Check we handle alert
	 */
	@Test(groups = {"ut"})
	public void testGetCurrentUrlWithAlert() {
		when(driver.getCurrentUrl()).thenThrow(new UnhandledAlertException("Alert preset")).thenReturn("http://google.com");
		Assert.assertEquals(eventDriver.getCurrentUrl(), "http://google.com");
		verify(eventDriver).switchTo();
	}

	/**
	 * If alert cannot be dismissed, error is raised
	 */
	@Test(groups = {"ut"})
	public void testGetCurrentUrlWithAlert2() {
		when(driver.getCurrentUrl()).thenThrow(new UnhandledAlertException("Alert preset"));
		Assert.assertThrows(UnhandledAlertException.class, eventDriver::getCurrentUrl);
		verify(eventDriver).switchTo();
	}

	/**
	 * getCurrentUrl is invalid for native mobile application, UnsupportedOperationException is raised
	 */
	@Test(groups = {"ut"})
	public void testGetCurrentUrlMobile() {
		when(driver.getCurrentUrl()).thenThrow(new UnsupportedCommandException("command invalid"));
		Assert.assertEquals(eventDriver.getCurrentUrl(), "");
	}

	@Test(groups = {"ut"})
	public void testGetCurrentUrlNoWindow() {
		when(driver.getCurrentUrl()).thenThrow(new NoSuchWindowException("no window"));
		Assert.assertEquals(eventDriver.getCurrentUrl(), "");
	}

	@Test(groups = {"ut"})
	public void testGeoLocationWithChrome() {
		try (MockedStatic<ChromiumUtils> mockedChromiumUtils = mockStatic(ChromiumUtils.class)) {
			when(browserInfo.getBrowser()).thenReturn(BrowserType.CHROME);
			eventDriver.setGeolocation(10.0, 12.0);
			mockedChromiumUtils.verify(() -> ChromiumUtils.setGeolocation(eventDriver.getWebDriver(), 10.0, 12.0));
		}
	}

	@Test(groups = {"ut"})
	public void testGeoLocationWithEdge() {
		try (MockedStatic<ChromiumUtils> mockedChromiumUtils = mockStatic(ChromiumUtils.class)) {
			when(browserInfo.getBrowser()).thenReturn(BrowserType.EDGE);
			eventDriver.setGeolocation(10.0, 12.0);
			mockedChromiumUtils.verify(() -> ChromiumUtils.setGeolocation(eventDriver.getWebDriver(), 10.0, 12.0));
		}
	}

	@Test(groups = {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Geolocation is supported only on chrome or edge")
	public void testGeoLocationWithFirefox() {
		try (MockedStatic<ChromiumUtils> mockedChromiumUtils = mockStatic(ChromiumUtils.class)) {
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);
			eventDriver.setGeolocation(10.0, 12.0);
		}
	}

	@Test(groups = {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Latitude must be between -90 and 90 inclusive")
	public void testGeoLocationWithWrongLatitude1() {
		eventDriver.setGeolocation(-90.01, 12.0);
	}
	@Test(groups = {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Latitude must be between -90 and 90 inclusive")
	public void testGeoLocationWithWrongLatitude2() {
		eventDriver.setGeolocation(90.01, 12.0);
	}

	@Test(groups = {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Longitude must be between -180 and 180 inclusive")
	public void testGeoLocationWithWrongLongitude1() {
		eventDriver.setGeolocation(10.0, -180.01);
	}
	@Test(groups = {"ut"}, expectedExceptions = ScenarioException.class, expectedExceptionsMessageRegExp = "Longitude must be between -180 and 180 inclusive")
	public void testGeoLocationWithWrongLongitude2() {
		eventDriver.setGeolocation(10.0, 180.01);
	}
	
}

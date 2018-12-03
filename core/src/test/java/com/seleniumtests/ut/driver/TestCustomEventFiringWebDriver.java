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
package com.seleniumtests.ut.driver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.connectors.selenium.SeleniumGridConnector;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverExceptionListener;
import com.seleniumtests.driver.DriverMode;
import com.seleniumtests.driver.Keyboard;
import com.seleniumtests.driver.screenshots.VideoRecorder;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

@PrepareForTest({OSUtilityFactory.class, CustomEventFiringWebDriver.class})
@PowerMockIgnore("javax.imageio.*")
public class TestCustomEventFiringWebDriver extends MockitoTest {


	@Mock
	private RemoteWebDriver driver;
	
	@Mock
	private Options options;
	
	@Mock
	private Window window;
	
	@Mock
	private OSUtility osUtility;
	
	@Mock
	private BrowserInfo browserInfo;
	
	@Mock
	private Robot robot;
	
	@Mock
	private Keyboard keyboard;
	
	@Mock 
	private SeleniumGridConnector gridConnector;
	
	@Mock
	private VideoRecorder videoRecorder;
	
	private EventFiringWebDriver eventDriver;

	@BeforeMethod(groups={"ut"})
	private void init() throws Exception {
		

		PowerMockito.spy(CustomEventFiringWebDriver.class);
		
		// add DriverExceptionListener to reproduce driver behavior
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, browserInfo, true, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		
		when(driver.manage()).thenReturn(options);
		when(driver.getSessionId()).thenReturn(new SessionId("1234"));
		when(driver.getPageSource()).thenReturn("<html></html>");
		when(options.window()).thenReturn(window);
		when(window.getSize()).thenReturn(new Dimension(100, 100));
		
		PowerMockito.mockStatic(OSUtilityFactory.class);
		when(OSUtilityFactory.getInstance()).thenReturn(osUtility);
		
		PowerMockito.whenNew(Robot.class).withNoArguments().thenReturn(robot);
		PowerMockito.whenNew(VideoRecorder.class).withAnyArguments().thenReturn(videoRecorder);
		PowerMockito.whenNew(Keyboard.class).withNoArguments().thenReturn(keyboard);
		PowerMockito.doReturn(new Rectangle(1900, 1000)).when(CustomEventFiringWebDriver.class, "getScreensRectangle");
		
		
	}
	
	@Test(groups = {"ut"})
	public void testGetSessionId() {
		Assert.assertEquals(((CustomEventFiringWebDriver)eventDriver).getSessionId(), "1234");
	}
	
	/**
	 * Test the case where driver is not a RemoteWebDriver (htmlunit)
	 * We shoud get a generated id
	 */
	@Test(groups = {"ut"})
	public void testGetSessionIdWithIncopmatibleDriver() {
		when(driver.getSessionId()).thenThrow(ClassCastException.class);
		Assert.assertNotEquals(((CustomEventFiringWebDriver)eventDriver).getSessionId(), "1234");
	}
	
	@Test(groups = {"ut"})
	public void testGetPageSource() {
		Assert.assertEquals(((CustomEventFiringWebDriver)eventDriver).getPageSource(), "<html></html>");
	}
	
	/**
	 * Test the case where driver cannot return page source (case for mobile browsers or old edge versions for example)
	 */
	@Test(groups = {"ut"})
	public void testGetPageSourceWithIncopmatibleDriver() {
		doThrow(new WebDriverException("some error")).when(driver).getPageSource();
		Assert.assertNull(((CustomEventFiringWebDriver)eventDriver).getPageSource());
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimension() {
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testContentDimension() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getContentDimension();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForContentDimensionWithoutScrollbar() {
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbar() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testViewPortDimensionWithoutScrollbarWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 80);
		Assert.assertEquals(dim.width, 120);
	}

	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"})
	public void testContentDimensionWithoutScrollbarNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Dimension dim = ((CustomEventFiringWebDriver)eventDriver).getViewPortDimensionWithoutScrollbar();
		
		// check we get the window dimension
		Assert.assertEquals(dim.height, 100);
		Assert.assertEquals(dim.width, 100);
	}
	
	/**
	 * check that if an error occurs during javascript invocation, window size is returned (issue #161)
	 */
	@Test(groups = {"ut"})
	public void testNullJavascriptReplyForScrollPosition() {
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the default position: (0,0)
		Assert.assertEquals(point.x, 0);
		Assert.assertEquals(point.y, 0);
	}
	
	/**
	 * Check standard web case where dimension comes from javascript call
	 */
	@Test(groups = {"ut"})
	public void testScrollPosition() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the scroll position
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}

	/**
	 * issue #165: Check case where browser is not at 100% zoom and so, double reply is returned
	 */
	@Test(groups = {"ut"})
	public void testScrollPositionWithZoomFactor() {
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120.5, 80.67));
		Point point = ((CustomEventFiringWebDriver)eventDriver).getScrollPosition();
		
		// check we get the window dimension
		Assert.assertEquals(point.x, 120);
		Assert.assertEquals(point.y, 80);
	}
	
	/**
	 * For non web test, dimension is returned from driver call, not javascript
	 */
	@Test(groups = {"ut"}, expectedExceptions=WebDriverException.class)
	public void testScrollPositionNonWebTest() {
		eventDriver = spy(new CustomEventFiringWebDriver(driver, null, null, false, DriverMode.LOCAL, null, null).register(new DriverExceptionListener()));
		when(driver.executeScript(anyString())).thenReturn(Arrays.asList(120L, 80L));
		((CustomEventFiringWebDriver)eventDriver).getScrollPosition();

	}
	
	/**
	 * Check driver is quit and all pids are killed
	 */
	@Test(groups = {"ut"})
	public void testQuit() {
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(Arrays.asList(1000L));
		
		((CustomEventFiringWebDriver)eventDriver).quit();
		verify(osUtility).killProcess(eq("1000"), eq(true));
	}
	
	/**
	 * Check that even if error is raised when driver is quit, killing process is done
	 */
	@Test(groups = {"ut"})
	public void testQuitInError() {
		when(browserInfo.getAllBrowserSubprocessPids(new ArrayList<>())).thenReturn(Arrays.asList(1000L));
		doThrow(new WebDriverException("some error")).when(driver).quit();
		
		try {
			((CustomEventFiringWebDriver)eventDriver).quit();
		} catch (WebDriverException e) {}
		verify(osUtility).killProcess(eq("1000"), eq(true));
	}
	
	/**
	 * Test left clic in local mode
	 */
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktop() {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
		
		verify(robot).mousePress(eq(InputEvent.BUTTON1_DOWN_MASK));
		verify(robot).mouseRelease(eq(InputEvent.BUTTON1_DOWN_MASK));
		verify(robot).mouseMove(eq(0), eq(0));
		verify(gridConnector, never()).leftClic(eq(0), eq(0));
	}
	
	/**
	 * Test left clic in headless mode: an error should be raised because there is no session
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickOnDesktopWithoutDesktop() throws Exception {
		PowerMockito.whenNew(Robot.class).withNoArguments().thenThrow(AWTException.class);
		
		CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
	}
	
	/**
	 * Test left click with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testLeftClickWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * Test left clic in grid mode
	 */
	@Test(groups = {"ut"})
	public void testLeftClickOnDesktopWithGrid() {
		CustomEventFiringWebDriver.leftClicOnDesktopAt(0, 0, DriverMode.GRID, gridConnector);
		
		verify(robot, never()).mousePress(eq(InputEvent.BUTTON1_DOWN_MASK));
		verify(robot, never()).mouseRelease(eq(InputEvent.BUTTON1_DOWN_MASK));
		verify(robot, never()).mouseMove(eq(0), eq(0));
		verify(gridConnector).leftClic(eq(0), eq(0));
	}
	
	/**
	 * Test right clic in local mode
	 */
	@Test(groups = {"ut"})
	public void testRightClickOnDesktop() {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
		
		verify(robot).mousePress(eq(InputEvent.BUTTON2_DOWN_MASK));
		verify(robot).mouseRelease(eq(InputEvent.BUTTON2_DOWN_MASK));
		verify(robot).mouseMove(eq(0), eq(0));
		verify(gridConnector, never()).rightClic(eq(0), eq(0));
	}
	
	/**
	 * Test right clic in headless mode: an error should be raised because there is no session
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickOnDesktopWithoutDesktop() throws Exception {
		PowerMockito.whenNew(Robot.class).withNoArguments().thenThrow(AWTException.class);
		
		CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.LOCAL, gridConnector);
	}

	/**
	 * Test right click with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testRightClickWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * Test right clic in grid mode
	 */
	@Test(groups = {"ut"})
	public void testRightClickOnDesktopWithGrid() {
		CustomEventFiringWebDriver.rightClicOnDesktopAt(0, 0, DriverMode.GRID, gridConnector);
		
		verify(robot, never()).mousePress(eq(InputEvent.BUTTON2_DOWN_MASK));
		verify(robot, never()).mouseRelease(eq(InputEvent.BUTTON2_DOWN_MASK));
		verify(robot, never()).mouseMove(eq(0), eq(0));
		verify(gridConnector).rightClic(eq(0), eq(0));
	}
	
	/**
	 * write to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testWriteToDesktop() {
		CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.LOCAL, gridConnector);
		
		verify(keyboard).typeKeys(eq("text"));
		verify(gridConnector, never()).writeText(anyString());
	}

	/**
	 * Write text in headless mode: an error should be raised because there is no session
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteToDesktopWithoutDesktop() throws Exception {
		PowerMockito.whenNew(Keyboard.class).withNoArguments().thenThrow(AWTException.class);
		
		CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.LOCAL, gridConnector);
	}

	/**
	 * write textk with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testWriteTextWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * write to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testWriteToDesktopWithGrid() {
		CustomEventFiringWebDriver.writeToDesktop("text", DriverMode.GRID, gridConnector);

		verify(keyboard, never()).typeKeys("text");
		verify(gridConnector).writeText(eq("text"));
	}
	
	/**
	 * send keys to desktop in local mode
	 */
	@Test(groups = {"ut"})
	public void testSendKeysToDesktop() {
		CustomEventFiringWebDriver.sendKeysToDesktop(Arrays.asList(10, 20), DriverMode.LOCAL, gridConnector);
		
		verify(robot).keyPress(eq(10));
		verify(robot).keyPress(eq(20));
		verify(robot).keyRelease(eq(10));
		verify(robot).keyRelease(eq(20));
		verify(gridConnector, never()).sendKeysWithKeyboard(any(List.class));
	}

	/**
	 * Write text in headless mode: an error should be raised because there is no session
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysToDesktopWithoutDesktop() throws Exception {
		PowerMockito.whenNew(Robot.class).withNoArguments().thenThrow(AWTException.class);
		
		CustomEventFiringWebDriver.sendKeysToDesktop(Arrays.asList(10, 20), DriverMode.LOCAL, gridConnector);
	}

	/**
	 * send keys with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testSendKeysWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.sendKeysToDesktop(Arrays.asList(10, 20), DriverMode.SAUCELABS, gridConnector);
	}
	
	/**
	 * send keys to desktop in grid mode
	 */
	@Test(groups = {"ut"})
	public void testSendKeysToDesktopWithGrid() {
		CustomEventFiringWebDriver.sendKeysToDesktop(Arrays.asList(10, 20), DriverMode.GRID, gridConnector);

		verify(robot, never()).keyPress(eq(10));
		verify(robot, never()).keyPress(eq(20));
		verify(robot, never()).keyRelease(eq(10));
		verify(robot, never()).keyRelease(eq(20));
		verify(gridConnector).sendKeysWithKeyboard(any(List.class));
	}
	
	/**
	 * capture picture in local mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktop() throws IOException {
		File imageFile = File.createTempFile("image-", ".png");
		FileUtils.copyInputStreamToFile(getClass().getClassLoader().getResourceAsStream("tu/images/ffLogoConcat.png"), imageFile);
		BufferedImage bi = ImageIO.read(imageFile);

		when(robot.createScreenCapture(any(Rectangle.class))).thenReturn(bi);
		
		String b64img = CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
		Assert.assertTrue(b64img.startsWith("iVBORw0KGgoAAAANSUhEUgAAALoAAACMCAIAAABETyQWAACAAElEQVR42pT8ZVRbafs+fmeKx3B3"));
		verify(gridConnector, never()).captureDesktopToBuffer();
	}
	
	/**
	 * capture picture in local headless mode, ScenarioException should be raised
	 * @throws IOException 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithoutDesktop() throws IOException {

		when(robot.createScreenCapture(any(Rectangle.class))).thenThrow(AWTException.class);
		
		CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.LOCAL, gridConnector);
	}
	
	/**
	 * capture desktop with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testCaptureDesktopWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.SAUCELABS, gridConnector);
	}
	

	/**
	 * capture picture in grid mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testCaptureDesktopWithGrid() throws IOException {

		CustomEventFiringWebDriver.captureDesktopToBase64String(DriverMode.GRID, gridConnector);
		verify(gridConnector).captureDesktopToBuffer();
	}

	/**
	 * start video capture to desktop in local mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testStartVideoCaptureToDesktop() throws IOException {
		File videoFolder = File.createTempFile("video", ".avi").getParentFile();
		CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");
		
		verify(videoRecorder).start();
		verify(gridConnector, never()).startVideoCapture();
	}

	/**
	 * start video capture in headless mode: an error should be raised because there is no session
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureToDesktopWithoutDesktop() throws Exception {
		File videoFolder = File.createTempFile("video", ".avi").getParentFile();
		PowerMockito.whenNew(VideoRecorder.class).withArguments(any(File.class), anyString()).thenThrow(HeadlessException.class);

		CustomEventFiringWebDriver.startVideoCapture(DriverMode.LOCAL, gridConnector, videoFolder, "video.avi");
	}

	/**
	 * start video capture with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStartVideoCaptureWithDeviceProviders() throws Exception {
		File videoFolder = File.createTempFile("video", ".avi").getParentFile();
		CustomEventFiringWebDriver.startVideoCapture(DriverMode.SAUCELABS, gridConnector, videoFolder, "video.avi");
	}
	
	/**
	 * start video capture to desktop in grid mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testStartVideoCaptureToDesktopWithGrid() throws IOException {
		File videoFolder = File.createTempFile("video", ".avi").getParentFile();
		CustomEventFiringWebDriver.startVideoCapture(DriverMode.GRID, gridConnector, videoFolder, "video.avi");

		verify(videoRecorder, never()).start();
		verify(gridConnector).startVideoCapture();
	}
	
	/**
	 * stop video capture to desktop in local mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktop() throws IOException {
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.LOCAL, gridConnector, videoRecorder);
		
		verify(videoRecorder).stop();
		verify(gridConnector, never()).stopVideoCapture(anyString());
	}
	
	/**
	 * stop video capture whereas it has not been started, ScenarioException should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureIfNotStarted() throws Exception {
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.LOCAL, gridConnector, null);
	}

	/**
	 * stop video capture with device providers: this is not supported, so exception should be raised
	 * @throws Exception 
	 */
	@Test(groups = {"ut"}, expectedExceptions=ScenarioException.class)
	public void testStopVideoCaptureWithDeviceProviders() throws Exception {
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.SAUCELABS, gridConnector, videoRecorder);
	}
	
	/**
	 * stop video capture to desktop in grid mode
	 * @throws IOException 
	 */
	@Test(groups = {"ut"})
	public void testStopVideoCaptureToDesktopWithGrid() throws IOException {

		File videoFile = File.createTempFile("video", ".avi");
		when(videoRecorder.getFolderPath()).thenReturn(videoFile.getParentFile());
		when(videoRecorder.getFileName()).thenReturn(videoFile.getName());
		CustomEventFiringWebDriver.stopVideoCapture(DriverMode.GRID, gridConnector, videoRecorder);
		
		verify(videoRecorder, never()).start();
		verify(gridConnector).stopVideoCapture(anyString());
	}
}

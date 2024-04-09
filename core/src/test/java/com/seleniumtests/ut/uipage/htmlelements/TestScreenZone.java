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
package com.seleniumtests.ut.uipage.htmlelements;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestScreenZone extends MockitoTest {
	
	@Mock
	ImageDetector imageDetector;
	
	@Mock
	ScreenshotUtil screenshotUtil;

	@Mock
	CustomEventFiringWebDriver driver;
	
	@Mock
	BrowserInfo	browserInfo;
	
	@InjectMocks
	ScreenZone screenZone = new ScreenZone();
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		screenZone.clearMemory();
	}

	@Test(groups={"ut"})
	public void testClick() {
		try (MockedStatic mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
			MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class);
		) {
			ScreenZone picElement = spy(screenZone);
			picElement.setObjectPictureFile(new File(""));

			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			picElement.click();
			verify(picElement).moveAndLeftClick(35, 60);
		}
	}
	
	/**
	 * Check search is done only once even if we click twice
	 */
	@Test(groups={"ut"})
	public void testClickTwice() {
		try (MockedStatic mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
			 MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class);
		) {
			ScreenZone picElement = spy(screenZone);
			picElement.setObjectPictureFile(new File(""));

			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			picElement.click();
			picElement.click();
			verify(imageDetector).getDetectedRectangle();
			verify(picElement, times(2)).findElement();
			verify(picElement, times(2)).moveAndLeftClick(35, 60);
		}
	}
	
	/**
	 * issue #359: return a specific message when resource is not found
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "Resource 'invalidPath' cannot be found")
	public void testInvalidResource() {
		new ScreenZone("", "invalidPath");
	}
	
	/**
	 * issue #359: return a specific message when file is not found
	 */
	@Test(groups= {"ut"}, expectedExceptions = ConfigurationException.class, expectedExceptionsMessageRegExp = "File for scene to detect object at path 'invalidPath' does not exist")
	public void testInvalidFile() {
		new ScreenZone("", new File("invalidPath"));
	}
	
	@Test(groups={"ut"})
	public void testDoubleClick() {
		try (MockedStatic mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
			 MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class);
		) {
			ScreenZone picElement = spy(screenZone);
			picElement.setObjectPictureFile(new File(""));

			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			picElement.doubleClickAt(0, 0);
			verify(picElement).moveAndDoubleClick(35, 60);
		}
	}

	@Test(groups={"ut"})
	public void testRightClick() {
		try (MockedStatic mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
			 MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class);
		) {
			ScreenZone picElement = spy(screenZone);
			picElement.setObjectPictureFile(new File(""));

			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			picElement.rightClickAt(0, 0);
			verify(picElement).moveAndRightClick(35, 60);
		}
	}

	@Test(groups={"ut"})
	public void testSendKeys() {
		try (MockedStatic mockedCustomFiringWebDriver = mockStatic(CustomEventFiringWebDriver.class);
			 MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class);
		) {
			ScreenZone picElement = spy(screenZone);
			picElement.setObjectPictureFile(new File(""));

			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			picElement.sendKeys(0, 0, KeyEvent.VK_0);
			verify(picElement).moveAndLeftClick(35, 60);
		}
	}
	
	
	@Test(groups={"ut"})
	public void testPictureNotVisible() throws AWTException {
		ScreenZone picElement = spy(screenZone);
		picElement.setObjectPictureFile(new File(""));
		doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent());
		
		verify(picElement).findElement();
		
	}
	
	@Test(groups={"ut"})
	public void testPictureNotVisibleWithReplay() throws AWTException {
		ScreenZone picElement = spy(screenZone);
		picElement.setObjectPictureFile(new File(""));
		doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent(350));
		
		verify(picElement, times(2)).findElement();
	}
	
	@Test(groups={"ut"})
	public void testPictureVisible() throws AWTException {
		ScreenZone picElement = spy(screenZone);
		picElement.setObjectPictureFile(new File(""));
		doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.SCREEN, File.class, true)).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		
		Assert.assertTrue(picElement.isElementPresent(2000));
		verify(picElement).findElement();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testTapOnDesktop() throws AWTException {	
		screenZone.tap();
	}
	
	@Test(groups={"ut"}, expectedExceptions=ScenarioException.class)
	public void testSwipeOnDesktop() throws AWTException {	
		screenZone.swipe(0, 0);
	}

	
	@AfterMethod(groups={"ut"}, alwaysRun=true)
	public void reset(ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
	}


	@Test(groups = { "ut" })
	public void testGetNameWithLabel() {
		ScreenZone el = new ScreenZone("myScreen", "tu/ffLogo1.png");
		el.setFieldName("el");
		Assert.assertEquals(el.getName(), "myScreen");
	}
	@Test(groups = { "ut" })
	public void testGetNameWithEmptyLabel() {
		ScreenZone el = new ScreenZone("", "tu/ffLogo1.png");
		el.setFieldName("el");
		Assert.assertEquals(el.getName(), "el");
	}
	@Test(groups = { "ut" })
	public void testGetNameWithNullLabel() {
		ScreenZone el = new ScreenZone(null, "tu/ffLogo1.png");
		el.setFieldName("el");
		Assert.assertEquals(el.getName(), "el");
	}
	@Test(groups = { "ut" })
	public void testGetNameResourcePath() {
		ScreenZone el = new ScreenZone(null, "tu/ffLogo1.png");
		Assert.assertEquals(el.getName(), "tu/ffLogo1.png");
	}
	@Test(groups = { "ut" })
	public void testGetNameFile() {
		ScreenZone el = new ScreenZone(null, Paths.get(SeleniumTestsContextManager.getApplicationDataPath(), "images", "googleSearch.png").toFile());
		Assert.assertEquals(el.getName(), "googleSearch.png");
	}
	@Test(groups = { "ut" })
	public void testGetNameNoInfo() {
		ScreenZone el = new ScreenZone();
		Assert.assertEquals(el.getName(), "screen");
	}
}

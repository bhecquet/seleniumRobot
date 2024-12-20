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
import java.io.File;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Coordinates;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.DriverConfig;
import com.seleniumtests.driver.TestType;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.driver.screenshots.SnapshotTarget;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.util.imaging.ImageDetector;

public class TestPictureElement extends MockitoTest {
	
	@Mock
	ImageDetector imageDetector;
	
	@Mock
	ScreenshotUtil screenshotUtil;
	
	@Mock
	HtmlElement intoElement;
	
	@Mock
	Coordinates coordinates;
	
	@Mock
	CustomEventFiringWebDriver driver;
	
	@Mock
	WebUIDriver uiDriver;
	
	@Mock
	DriverConfig driverConfig;
	
	@Mock
	BrowserInfo browserInfo;

	@InjectMocks
	PictureElement pictureElement = new PictureElement();
	
	@BeforeMethod(groups={"ut"})
	public void init() {
		SeleniumTestsContextManager.getThreadContext().setTestType(TestType.WEB);
		pictureElement.clearMemory();
	}
	
	@Test(groups={"ut"})
	public void testClick() {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));

		try (MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			mockedWebUIDriver.when(() -> WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
			when(uiDriver.getDriver()).thenReturn(driver);
			when(uiDriver.getConfig()).thenReturn(driverConfig);
			when(driverConfig.getBrowserType()).thenReturn(BrowserType.FIREFOX);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(((CustomEventFiringWebDriver) driver).getDeviceAspectRatio()).thenReturn(1.0);
			when(((CustomEventFiringWebDriver) driver).getViewPortDimensionWithoutScrollbar(false)).thenReturn(new Dimension(200, 200));
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);
			when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);
			when(coordinates.inViewPort()).thenReturn(new Point(100, 120));
			when(coordinates.onPage()).thenReturn(new Point(100, 120));
			when(intoElement.getCoordinates()).thenReturn(coordinates);
			when(intoElement.getSize()).thenReturn(new Dimension(200, 200));

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();

			picElement.click();
			verify(picElement).moveAndClick(intoElement, -65, -60);
		}
		
	}
	
	/**
	 * Check search is done only once even if we click twice
	 */
	@Test(groups={"ut"})
	public void testClickTwice() {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));

		try (MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			mockedWebUIDriver.when(() -> WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
			when(uiDriver.getDriver()).thenReturn(driver);
			when(uiDriver.getConfig()).thenReturn(driverConfig);
			when(driverConfig.getBrowserType()).thenReturn(BrowserType.FIREFOX);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(((CustomEventFiringWebDriver) driver).getDeviceAspectRatio()).thenReturn(1.0);
			when(((CustomEventFiringWebDriver) driver).getViewPortDimensionWithoutScrollbar(false)).thenReturn(new Dimension(200, 200));
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);
			when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);
			when(coordinates.inViewPort()).thenReturn(new Point(100, 120));
			when(coordinates.onPage()).thenReturn(new Point(100, 120));
			when(intoElement.getCoordinates()).thenReturn(coordinates);
			when(intoElement.getSize()).thenReturn(new Dimension(200, 200));

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();

			picElement.click();
			picElement.click();
			verify(imageDetector).getDetectedRectangle();            // image search only done once
			verify(picElement, times(2)).findElement(); // search called 2 times
			verify(picElement, times(2)).moveAndClick(intoElement, -65, -60);
		}
		
	}
	@Test(groups={"ut"})
	public void testClickOtherPixelRatio() {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));

		try (MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			mockedWebUIDriver.when(() -> WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
			when(uiDriver.getDriver()).thenReturn(driver);
			when(uiDriver.getConfig()).thenReturn(driverConfig);
			when(driverConfig.getBrowserType()).thenReturn(BrowserType.FIREFOX);
			when(driver.getBrowserInfo()).thenReturn(browserInfo);
			when(((CustomEventFiringWebDriver) driver).getDeviceAspectRatio()).thenReturn(1.5);
			when(((CustomEventFiringWebDriver) driver).getViewPortDimensionWithoutScrollbar(false)).thenReturn(new Dimension(200, 200));
			when(browserInfo.getBrowser()).thenReturn(BrowserType.FIREFOX);
			when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.5);
			when(coordinates.inViewPort()).thenReturn(new Point(100, 120));
			when(coordinates.onPage()).thenReturn(new Point(100, 120));
			when(intoElement.getCoordinates()).thenReturn(coordinates);
			when(intoElement.getSize()).thenReturn(new Dimension(200, 200));

			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();

			picElement.click();
			verify(picElement).moveAndClick(intoElement, -78, -81); // as pixel ratio changed, real rectangle is different
		}
	}
	
	
	@Test(groups={"ut"})
	public void testPictureNotVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
		doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent());
		
		verify(picElement).findElement();
		
	}
	
	@Test(groups={"ut"})
	public void testPictureNotVisibleWithReplay() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
		when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent(350));
		
		verify(picElement, times(2)).findElement();
		
	}
	
	@Test(groups={"ut"})
	public void testPictureVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);

		try (MockedStatic mockedWebUIDriver = mockStatic(WebUIDriver.class)) {
			mockedWebUIDriver.when(() -> WebUIDriver.getWebDriver(anyBoolean())).thenReturn(driver);
			mockedWebUIDriver.when(() -> WebUIDriver.getWebUIDriver(anyBoolean())).thenReturn(uiDriver);
			when(uiDriver.getDriver()).thenReturn(driver);
			when(((CustomEventFiringWebDriver) driver).getDeviceAspectRatio()).thenReturn(1.0);
			picElement.setObjectPictureFile(new File(""));
			doReturn(screenshotUtil).when(picElement).getScreenshotUtil();
			when(screenshotUtil.capture(SnapshotTarget.PAGE, File.class, true)).thenReturn(new File(""));
			when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
			when(imageDetector.getSizeRatio()).thenReturn(1.0);

			Assert.assertTrue(picElement.isElementPresent(2000));
			verify(picElement).findElement();
		}
		
	}

	@Test(groups={"ut"})
	public void testPNGResourceCreation() {
		PictureElement pic = new PictureElement("picture", "tu/images/step.png", null, 0.1);
		Assert.assertTrue(pic.getObjectPictureFile().getName().endsWith(".png"));
	}
	@Test(groups={"ut"})
	public void testJPGResourceCreation() {
		PictureElement pic = new PictureElement("picture", "tu/images/goat-4.jpg", null, 0.1);
		Assert.assertTrue(pic.getObjectPictureFile().getName().endsWith(".jpg"));
	}
	@Test(groups={"ut"})
	public void testINIResourceCreation() {
		PictureElement pic = new PictureElement("picture", "tu/env.ini", null, 0.1);
		Assert.assertTrue(pic.getObjectPictureFile().getName().endsWith(".tmp"));
	}

	
	@AfterMethod(groups={"ut"}, alwaysRun=true)
	public void reset(ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
	}
}

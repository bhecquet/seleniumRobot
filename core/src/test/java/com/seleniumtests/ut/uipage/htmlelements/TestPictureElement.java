/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.AWTException;
import java.io.File;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.uipage.htmlelements.HtmlElement;
import com.seleniumtests.uipage.htmlelements.PictureElement;
import com.seleniumtests.util.imaging.ImageDetector;

//@PrepareForTest(WebUIDriver.class)
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
	Mouse mouse;
	@Mock
	Keyboard keyboard;

	@InjectMocks
	PictureElement pictureElement = new PictureElement();
	
	
	/**
	 * test click
	 * This test is disabled as it cannot be executed with all other unit tests. It results in 
	 * "UnsatisfiedLinkError, library already loaded in another class loader" because ImageDetector has already loaded openCV
	 * To enable it, uncomment "@PrepareForTest" line and set "enabled" to true
	 * 
	 */
	@Test(groups={"ut"}, enabled=false)
	public void testClick() {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));

		PowerMockito.mockStatic(WebUIDriver.class);
		when(WebUIDriver.getWebDriver()).thenReturn(driver);
		when(driver.getMouse()).thenReturn(mouse);
		when(driver.getKeyboard()).thenReturn(keyboard);
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		when(coordinates.inViewPort()).thenReturn(new Point(100, 120));
		when(intoElement.getCoordinates()).thenReturn(coordinates);
		
		picElement.click();
		verify(picElement).moveAndClick(intoElement, -65, -60);
	}
	
	
	@Test(groups={"ut"})
	public void testPictureNotVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent());
		
		verify(picElement).findElement(true);
		
	}
	
	@Test(groups={"ut"})
	public void testPictureNotVisibleWithReplay() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		doThrow(ImageSearchException.class).when(imageDetector).detectExactZoneWithScale();
		
		Assert.assertFalse(picElement.isElementPresent(350));
		
		verify(picElement, times(2)).findElement(true);
		
	}
	
	@Test(groups={"ut"})
	public void testPictureVisible() throws AWTException {
		PictureElement picElement = spy(pictureElement);
		picElement.setObjectPictureFile(new File(""));
		when(screenshotUtil.captureWebPageToFile()).thenReturn(new File(""));
		when(imageDetector.getDetectedRectangle()).thenReturn(new Rectangle(10, 10, 100, 50));
		when(imageDetector.getSizeRatio()).thenReturn(1.0);
		
		Assert.assertTrue(picElement.isElementPresent(2000));
		verify(picElement).findElement(true);
		
	}

	
	@AfterMethod(alwaysRun=true)
	public void reset(ITestContext testNGCtx) {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
	}
}

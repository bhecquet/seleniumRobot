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

import java.awt.event.KeyEvent;

import com.seleniumtests.customexception.WebSessionEndedException;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.uipage.htmlelements.ScreenZone;
import com.seleniumtests.util.helper.WaitHelper;
import org.w3c.dom.css.Rect;

public class TestScreenZone extends GenericMultiBrowserTest {
	
	public TestScreenZone(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestScreenZone() throws Exception {
		super(BrowserType.CHROME, "DriverTestPageWithoutFixedPattern");  
	}

	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {

			try {
				Alert alert = driver.switchTo().alert();
				alert.dismiss();
			} catch (Exception e) {}

			DriverTestPageWithoutFixedPattern.logoText.clear();
			DriverTestPageWithoutFixedPattern.textElement.clear();
			((CustomEventFiringWebDriver)driver).scrollTop();
			
			DriverTestPageWithoutFixedPattern.googleForDesktop.moveAndLeftClick(0, 200);
			WaitHelper.waitForSeconds(2); // slow down tests because, with search picture cache introduced by #510, 2 successive clicks may be interpreted by a double click
		}
	}
	
	@Test(groups={"it"})
	public void testClickOnGooglePicture() {
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktop.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Check that "captureSnapshot=false" do not prevent to use ScreenZone
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureWithCaptureSnapshotFalse() {
		
		try {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(false);
			DriverTestPageWithoutFixedPattern.googleForDesktop.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		} finally {
			SeleniumTestsContextManager.getThreadContext().setCaptureSnapshot(SeleniumTestsContext.DEFAULT_CAPTURE_SNAPSHOT);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testClickAtCoordinates() {
		try {
			// search zone to click
			DriverTestPageWithoutFixedPattern.googleForDesktop.findElement();
			Rectangle rectangle = DriverTestPageWithoutFixedPattern.googleForDesktop.getDetectedObjectRectangle();
			
			// clic with a new ScreenZone
			new ScreenZone("image").clickAt(rectangle.x + 10, rectangle.y + 10);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testDoubleClickAtCoordinates() {
		try {
			// search zone to click
			DriverTestPageWithoutFixedPattern.googleForDesktop.findElement();
			Rectangle rectangle = DriverTestPageWithoutFixedPattern.googleForDesktop.getDetectedObjectRectangle();
			
			// clic with a new ScreenZone
			new ScreenZone("image").doubleClickAt(rectangle.x + 10, rectangle.y + 10);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "double click image");
	}
	
	/**
	 * test correction of issue #134 by clicking on element defined by a File object
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureFromFile() {
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	

	/**
	 * test that an action changed actionDuration value
	 */
	@Test(groups={"it"})
	public void testActionDurationIsLogged() {
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.getActionDuration(), 0);
		try {
			DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.googleForDesktopWithFile.getActionDuration() > 0);
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(0, 40, "hello");
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "hello");
	}
	
	@Test(groups={"it"})
	public void testSendKeyboardKeysOnPicture() { 
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ab");
	}

	@Test(groups={"it"})
	public void testIsVisible() { 
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.googleForDesktop.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(DriverTestPageWithoutFixedPattern.zoneNotPresent.isElementPresent());
	}
	
	
	/**
	 * Test the param mainScreen when sending keys to a screenzone 
	 */
	@Test(groups={"it"})
	public void testSendKeysOnMainScreen() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(true, 0, 40, "hello");
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "hello");
	}
	@Test(groups={"it"})
	public void testSendKeyboardKeysOnMainScreen() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys(true, 0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ab");
	}
	@Test(groups={"it"})
	public void testSendKeysOnMainScreenNoPicture() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(DriverTestPageWithoutFixedPattern.table, 200);
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.findElement();
			Rectangle position = DriverTestPageWithoutFixedPattern.firefoxForDesktop.getDetectedObjectRectangle();

			// compute relative position of main screen so that offset we pass to sendKeys are relative to main screen
			java.awt.Rectangle mainScreenPosition = CustomEventFiringWebDriver.getMainScreenRectangle();
			java.awt.Rectangle screenPosition = CustomEventFiringWebDriver.getScreensRectangle();
			DriverTestPageWithoutFixedPattern.screenZoneFullScreen.sendKeys(true, position.x - (mainScreenPosition.x - screenPosition.x) + 50, position.y - (mainScreenPosition.y - screenPosition.y) + 70, KeyEvent.VK_C, KeyEvent.VK_B);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "cb");
	}

	@Test(groups={"it"})
	public void testSendKeysNoFocus() {
		try {
			DriverTestPageWithoutFixedPattern.carre.click();
			testPageWithoutPattern.getAlert(); // will break if alert is not present
			DriverTestPageWithoutFixedPattern.screenZoneFullScreen.sendKeysNoFocus(KeyEvent.VK_ENTER);
			try {
				testPageWithoutPattern.getDriver().switchTo().alert();
			} catch (NoAlertPresentException e) {
				// Ok here
			}
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
	}
	
}

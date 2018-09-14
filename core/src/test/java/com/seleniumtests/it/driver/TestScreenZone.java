/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017 B.Hecquet
 * 				Copyright 2018 Covea
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

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.driver.CustomEventFiringWebDriver;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.uipage.htmlelements.ScreenZone;

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
			DriverTestPageWithoutFixedPattern.logoText.clear();
			DriverTestPageWithoutFixedPattern.textElement.clear();
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
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testClickAtCoordinates() {
		try {
			// search zone to clic
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
			DriverTestPageWithoutFixedPattern.firefoxForDesktop.sendKeys("hello", 0, 40);
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
	
	
}

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
import com.seleniumtests.uipage.htmlelements.ScreenZone;

public class TestScreenZone extends GenericMultiBrowserTest {
	
	public TestScreenZone(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestScreenZone() throws Exception {
		super(BrowserType.CHROME, "DriverTestPageWithoutFixedPattern");  
	}

	@AfterMethod(groups={"it"})
	public void reset() {
		if (driver != null) {
			testPageWithoutPattern.logoText.clear();
			testPageWithoutPattern.textElement.clear();
		}
	}
	
	@Test(groups={"it"})
	public void testClickOnGooglePicture() {
		try {
			testPageWithoutPattern.googleForDesktop.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.textElement.getValue(), "image");
	}
	
	/**
	 * Clic at given coordinate on screen without picture reference
	 */
	@Test(groups={"it"})
	public void testClickAtCoordinates() {
		try {
			// search zone to clic
			testPageWithoutPattern.googleForDesktop.findElement();
			Rectangle rectangle = testPageWithoutPattern.googleForDesktop.getDetectedObjectRectangle();
			
			// clic with a new ScreenZone
			new ScreenZone("image").clickAt(rectangle.x + 10, rectangle.y + 10);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.textElement.getValue(), "image");
	}
	
	/**
	 * test correction of issue #134 by clicking on element defined by a File object
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureFromFile() {
		try {
			testPageWithoutPattern.googleForDesktopWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.textElement.getValue(), "image");
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		try {
			testPageWithoutPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(testPageWithoutPattern.table, 200);
			testPageWithoutPattern.firefoxForDesktop.sendKeys("hello", 0, 40);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "hello");
	}
	
	@Test(groups={"it"})
	public void testSendKeyboardKeysOnPicture() { 
		try {
			testPageWithoutPattern.logoText.clear();
			((CustomEventFiringWebDriver)driver).scrollToElement(testPageWithoutPattern.table, 200);
			testPageWithoutPattern.firefoxForDesktop.sendKeys(0, 40, KeyEvent.VK_A, KeyEvent.VK_B);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "ab");
	}

	@Test(groups={"it"})
	public void testIsVisible() { 
		Assert.assertTrue(testPageWithoutPattern.googleForDesktop.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(testPageWithoutPattern.zoneNotPresent.isElementPresent());
	}
	
	
}

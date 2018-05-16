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

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;

public class TestPictureElement extends GenericMultiBrowserTest {
	
	public TestPictureElement(WebDriver driver, DriverTestPage testPage) throws Exception {
		super(driver, testPage);
	}
	
	public TestPictureElement(BrowserType browserType) throws Exception {
		super(browserType, "DriverTestPageWithoutFixedPattern"); 
	}
	
	public TestPictureElement() throws Exception {
		super(null, "DriverTestPageWithoutFixedPattern");
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		testPageWithoutPattern.logoText.clear();
		testPageWithoutPattern.textElement.clear();
	}
	
	@Test(groups={"it"})
	public void testClickOnPicture() {
		try {
			testPageWithoutPattern.picture.clickAt(0, -20);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "ff logo");
	}
	
	/**
	 * test correction of issue #131 by clicking on element which does not have a "intoElement" parameter
	 */
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
			testPageWithoutPattern.picture.sendKeys("hello", 0, 40);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "hello");
	}

	@Test(groups={"it"})
	public void testIsVisible() { 
		Assert.assertTrue(testPageWithoutPattern.picture.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsVisibleOnDesktop() {
		Assert.assertTrue(testPageWithoutPattern.googleForDesktop.isElementPresentOnDesktop());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(testPageWithoutPattern.pictureNotPresent.isElementPresent());
	}
	
	
}

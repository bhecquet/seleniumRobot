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
import com.seleniumtests.util.helper.WaitHelper;

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
	
	@AfterMethod(groups={"it"}, alwaysRun=true)
	public void reset() {
		if (driver != null) {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			DriverTestPageWithoutFixedPattern.textElement.clear();
		}
	}
	
	@Test(groups={"it"})
	public void testClickOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.picture.clickAt(0, -20);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ff logo");
	}
	
	@Test(groups={"it"})
	public void testDoubleClickOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.picture.doubleClickAt(0, -20);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "double click ff logo");
	}
	
	/**
	 * test correction of issue #131 by clicking on element which does not have a "intoElement" parameter
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePicture() {
		try {
			DriverTestPageWithoutFixedPattern.googlePicture.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * test that an action changed actionDuration value
	 */
	@Test(groups={"it"})
	public void testActionDurationIsLogged() {
		// be sure action duration has been reset
		DriverTestPageWithoutFixedPattern.googlePicture.setActionDuration(0);
		try {
			DriverTestPageWithoutFixedPattern.googlePicture.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.googlePicture.getActionDuration() > 0);
	}
	
	/**
	 * test correction of issue #134 by clicking on element defined by a File object
	 */
	@Test(groups={"it"})
	public void testClickOnGooglePictureFromFile() {
		try {
			DriverTestPageWithoutFixedPattern.googlePictureWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.logoText.clear();
			DriverTestPageWithoutFixedPattern.picture.sendKeys("hello", 0, 40);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "hello");
	}

	@Test(groups={"it"})
	public void testIsVisible() { 
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.picture.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(DriverTestPageWithoutFixedPattern.pictureNotPresent.isElementPresent());
	}
	
	
}

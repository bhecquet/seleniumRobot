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

import java.time.LocalDateTime;
import java.util.Calendar;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.util.imaging.ImageDetector;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;

import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.util.helper.WaitHelper;

import static org.mockito.Mockito.*;

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
//			driver.get(testPageUrl); // reload
			try {
				DriverTestPageWithoutFixedPattern.logoText.clear();
				DriverTestPageWithoutFixedPattern.textElement.clear();
				DriverTestPageWithoutFixedPattern.textElement.click(); // issue #408: this click is not necessary but it seems that it "resets" firefox javascript state
			} catch (NoSuchElementException e) {
				logger.error("Cannot reset");
				logger.error(driver.getPageSource());
				throw e;
			}
		}
	}
	
	
	public void testClickOnPicture() {
		try {
			DriverTestPageWithoutFixedPattern.picture.clickAt(0, -20);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ff logo");
	}
	
	/**
	 * Check that if the same Picture element is used in a single instance of the same page, then, search will be done only once
	 */
	public void testMultipleActionsOnPicture() {
		ImageDetector originalDetector = DriverTestPageWithoutFixedPattern.googlePicture.getDetector();
		try {
			ImageDetector spiedDetector = spy(originalDetector);
			DriverTestPageWithoutFixedPattern.googlePicture.setDetector(spiedDetector);

			DriverTestPageWithoutFixedPattern.googlePicture.clearMemory(); // reset memory for object to detect
			Calendar start = Calendar.getInstance();
			testPageWithoutPattern.clickGooglePicture();
			long totalTime1 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			reset();

			// second click should not search for the element again as we are in the same page
			start = Calendar.getInstance();
			testPageWithoutPattern.clickGooglePicture();
			verify(spiedDetector).detectExactZoneWithScale(); // check detector has been called only once

			long totalTime2 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			logger.info(String.format("Time first click: %d ms - time second click: %d ms", totalTime1, totalTime2));

		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		} finally {
			DriverTestPageWithoutFixedPattern.googlePicture.setDetector(originalDetector);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Check that if the same Picture element is used in 2 instances of the same page, then, search will be done twice
	 */
	public void testMultipleActionsOnPictureWithAnotherPage() {
		ImageDetector originalDetector = DriverTestPageWithoutFixedPattern.googlePicture.getDetector();
		try {
			ImageDetector spiedDetector = spy(originalDetector);
			DriverTestPageWithoutFixedPattern.googlePicture.setDetector(spiedDetector);

			DriverTestPageWithoutFixedPattern.googlePicture.clearMemory(); // reset memory for object to detect
			Calendar start = Calendar.getInstance();
			testPageWithoutPattern.clickGooglePicture();
			long totalTime1 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			reset();
			
			// second click should  search for the elementas we are not in the same page
			start = Calendar.getInstance();
			new DriverTestPageWithoutFixedPattern().clickGooglePicture();
			long totalTime2 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			logger.info(String.format("Time first click: %d ms - time second click: %d ms", totalTime1, totalTime2));

			verify(spiedDetector, times(2)).detectExactZoneWithScale(); // check detector has been called 2 times
			
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		} finally {
			DriverTestPageWithoutFixedPattern.googlePicture.setDetector(originalDetector);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	/**
	 * Check that if the same Picture element is used in 2 instances of the same page and PictureElement is declared private, then, search will be done twice
	 */
	public void testMultipleActionsOnPictureWithAnotherPagePrivateField() {
		ImageDetector originalDetector = DriverTestPageWithoutFixedPattern.getGooglePrivatePicture().getDetector();
		try {
			ImageDetector spiedDetector = spy(originalDetector);
			DriverTestPageWithoutFixedPattern.getGooglePrivatePicture().setDetector(spiedDetector);

			DriverTestPageWithoutFixedPattern.getGooglePrivatePicture().clearMemory(); // reset memory for object to detect
			Calendar start = Calendar.getInstance();
			testPageWithoutPattern.clickGooglePrivatePicture();
			long totalTime1 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			reset();
			
			// second click should  search for the element as we are not in the same page
			start = Calendar.getInstance();
			new DriverTestPageWithoutFixedPattern().clickGooglePrivatePicture();
			long totalTime2 = Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis();
			logger.info(String.format("Time first click: %d ms - time second click: %d ms", totalTime1, totalTime2));

			verify(spiedDetector, times(2)).detectExactZoneWithScale(); // check detector has been called 2 times
			
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		} finally {
			DriverTestPageWithoutFixedPattern.getGooglePrivatePicture().setDetector(originalDetector);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
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
	
	public void testClickOnGooglePictureFromFile() {
		try {
			DriverTestPageWithoutFixedPattern.googlePictureWithFile.click();
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
		Assert.assertEquals(DriverTestPageWithoutFixedPattern.textElement.getValue(), "image");
	}
	
	
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

	
	public void testIsVisible() { 
		Assert.assertTrue(DriverTestPageWithoutFixedPattern.picture.isElementPresent());
	}
	
	
	public void testIsNotVisible() {
		Assert.assertFalse(DriverTestPageWithoutFixedPattern.pictureNotPresent.isElementPresent());
	}

	public void testClickOnNonExistingPicture() {
		LocalDateTime start = LocalDateTime.now();
		try {
			SeleniumTestsContextManager.getThreadContext().setReplayTimeout(6);
			DriverTestPageWithoutFixedPattern.pictureNotPresent.click();
		} finally {
			// replay delay is set to 1 second, and after each try, we check if we can wait this replay delay + 200 ms, so
			// effective replay duraction is less than the replay timeout
			Assert.assertTrue(LocalDateTime.now().minusSeconds(4).isAfter(start));
		}

	}
	
	
}

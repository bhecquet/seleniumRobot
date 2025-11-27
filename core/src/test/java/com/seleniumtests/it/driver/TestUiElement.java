/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 * 				Copyright 2017-2019 B.Hecquet
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.driver;

import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.connectors.selenium.SeleniumRobotSnapshotServerConnector;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.BrowserType;
import com.seleniumtests.it.driver.support.GenericMultiBrowserTest;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;
import com.seleniumtests.util.helper.WaitHelper;

/**
 * Test class that aims at testing UiElement in real context
 * SeleniumServer must be started on port 8002 and at least an "image-field-detector" worker is available
 * @author S047432
 *
 */
public class TestUiElement extends GenericMultiBrowserTest {
	
	
	private SeleniumRobotSnapshotServerConnector connector;

	public TestUiElement() {
		super(BrowserType.CHROME, "DriverTestPageWithoutFixedPattern");
	}
	
	@BeforeMethod(groups={"it"}, alwaysRun = true)
	public void initConnector() {
		
		// pass the token via  -DseleniumRobotServerToken=xxxxxx
		connector = new SeleniumRobotSnapshotServerConnector(true, "http://localhost:8002", SeleniumTestsContextManager.getThreadContext().seleniumServer().getSeleniumRobotServerToken());
		if (!connector.getActive()) {
			throw new SkipException("no seleniumrobot server available");
		}

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
	
	@Test(groups= {"it"})
	public void testSendKeysWithLabelOnTheLeft() {
//		testPageWithoutPattern.move();
		DriverTestPageWithoutFixedPattern.uiTextElement.sendKeys("foo");
//		
//		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
//		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ff logo");
	}
	
	@Test(groups= {"it"})
	public void testSendKeysWithLabelAbove() {
//		testPageWithoutPattern.move();
		DriverTestPageWithoutFixedPattern.uiTextElementBelow.sendKeys("foo");
//		
//		WaitHelper.waitForMilliSeconds(500); // in case of browser slowness
//		Assert.assertEquals(DriverTestPageWithoutFixedPattern.logoText.getValue(), "ff logo");
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
	
	
}

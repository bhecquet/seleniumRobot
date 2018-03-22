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
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ImageSearchException;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPageWithoutFixedPattern;

public class TestPictureElement extends GenericTest {
	
	private static WebDriver driver;
	private static DriverTestPageWithoutFixedPattern testPage;
	
	public TestPictureElement() throws Exception {
	}
	
	public TestPictureElement(WebDriver driver, DriverTestPageWithoutFixedPattern testPage) throws Exception {
		TestPictureElement.driver = driver;
		TestPictureElement.testPage = testPage;
	}

	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("firefox");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		testPage = new DriverTestPageWithoutFixedPattern(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterMethod(groups={"it"})
	public void reset() {
		testPage.logoText.clear();
	}
	
	@Test(groups={"it"})
	public void testClickOnPicture() {
		try {
			testPage.picture.clickAt(0, -30);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "ff logo");
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		try {
			testPage.logoText.clear();
			testPage.picture.sendKeys("hello", 0, 5);
		} catch (ImageSearchException e) {
			throw new SkipException("Image not found, we may be on screenless slave", e);
		}
		Assert.assertEquals(testPage.logoText.getValue(), "hello");
	}

	@Test(groups={"it"})
	public void testIsVisible() {
		Assert.assertTrue(testPage.picture.isElementPresent());
	}
	
	@Test(groups={"it"})
	public void testIsNotVisible() {
		Assert.assertFalse(testPage.pictureNotPresent.isElementPresent());
	}
	
	
}

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;

public class TestPictureElement extends GenericDriverTest {
	
	private static WebDriver driver;
	private static DriverTestPage testPage;
	
	public TestPictureElement() throws Exception {
	}
	
	public TestPictureElement(WebDriver driver, DriverTestPage testPage) throws Exception {
		TestPictureElement.driver = driver;
		TestPictureElement.testPage = testPage;
	}

	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	@Test(groups={"it"})
	public void testClickOnPicture() {
		testPage.picture.clickAt(0, -30);
		Assert.assertEquals(testPage.logoText.getValue(), "ff logo");
	}
	
	@Test(groups={"it"})
	public void testSendKeysOnPicture() {
		testPage.logoText.clear();
		testPage.picture.sendKeys("hello", 0, 5);
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

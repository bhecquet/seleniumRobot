/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.it.webelements;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;

/**
 * Test PageObject
 * @author behe
 *
 */
public class TestPageObject{
	
	private static DriverTestPage testPage;
	private WebDriver driver;

	@BeforeClass()
	public void initDriver(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@Test()
	public void testPageParam() {
		Assert.assertEquals(testPage.param("variable1"), "value3");
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
}

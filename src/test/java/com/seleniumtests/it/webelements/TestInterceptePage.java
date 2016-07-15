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
import org.testng.xml.XmlTest;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;


public class TestInterceptePage {
	public TestInterceptePage() throws Exception {
		super();
	}

	private WebDriver driver;
	private static DriverTestPage testPage;
	
	@BeforeClass(groups={"it"})
	public void initDriver(final ITestContext testNGCtx, final XmlTest xmlTest) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		testPage = new DriverTestPage(true);
		driver = WebUIDriver.getWebDriver(true);
	}
	
	@AfterClass(alwaysRun = true)
	public void closeBrowser() {
		WebUIDriver.cleanUp();
	}
	
	
	@Test(groups={"it"})
	public void interceptBy() {
		Assert.assertEquals(testPage.findById("map:Text"), testPage.findById("text2"), "intercept by with map doesn't work");
	}

}

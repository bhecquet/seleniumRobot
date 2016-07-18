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

package com.seleniumtests.it.driver;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.OSUtility;
import com.seleniumtests.util.StringUtility;

public class TestDevMode {

	private static final Logger logger = TestLogging.getLogger(TestDevMode.class);
	
	private static WebDriver driver;
	private static DriverTestPage testHomePage;
	
	@BeforeClass(groups={"it"})
	public void initContext(final ITestContext testNGCtx) throws Exception {
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
		SeleniumTestsContextManager.getThreadContext().setExplicitWaitTimeout(2);
	}

	/**
	 * Test if the dev mode answers to the given JVM option.
	 */
	@Test(groups={"it"})
	public void testDevMode() {
		
		boolean devMode = SeleniumTestsContextManager.getThreadContext().getDevMode();
		logger.info("Test, dev mode = " + devMode);
		
		launchPageTest();
		
		if (OSUtility.isWebBrowserRunning(true) && !devMode) {
			Assert.fail("Except in development mode, all web browser processes should be over.");
		}
	}
	
	/**
	 * Forces devMode to false, and check if it closes all the browsers.
	 */
	@Test(groups={"it"}, enabled = false)
	public void testDevModeFalse() {
		
		logger.info("Test, dev mode = " + false);
		SeleniumTestsContextManager.getThreadContext().setDevMode(false);
		
		launchPageTest();
		
		if (OSUtility.isWebBrowserRunning(true)) {
			Assert.fail("All web browser processes should be over.");
		}
	}
	
	/**
	 * Forces devMode to true, and check if it the browser processes remain.
	 * NOTE : if those got closed in previous tests, this one becomes useless...
	 */
	@Test(groups={"it"})
	public void testDevModeTrue() {
		
		logger.info("Test, dev mode = " + true);
		SeleniumTestsContextManager.getThreadContext().setDevMode(true);
		
		List<String> webBrowserRunningListBefore = OSUtility.whichWebBrowserRunning();
		
		launchPageTest();
		
		List<String> webBrowserRunningListAfter = OSUtility.whichWebBrowserRunning();
		
		logger.info("webBrowserRunningListBefore : \n" + StringUtility.fromListToString(webBrowserRunningListBefore) );
		logger.info("webBrowserRunningListAfter : \n" + StringUtility.fromListToString(webBrowserRunningListAfter));
		
		Assert.assertEquals(webBrowserRunningListBefore.size(), webBrowserRunningListAfter.size());
	}
	
	/**
	 * launch one basic Selenium test
	 */
	public void launchPageTest(){
		try {
			driver = WebUIDriver.getWebDriver(true);
			testHomePage = new DriverTestPage(true);
			testHomePage.selectList.selectByText("option2");
			if (driver != null) {
				WebUIDriver.cleanUp();
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
}
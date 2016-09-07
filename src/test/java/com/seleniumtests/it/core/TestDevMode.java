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

package com.seleniumtests.it.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.DriverTestPage;
import com.seleniumtests.reporter.TestLogging;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.ProcessInfo;

public class TestDevMode extends GenericTest {

	private static final Logger logger = TestLogging.getLogger(TestDevMode.class);
	
	private OSUtility osUtil;
	
	private static WebDriver driver;
	private static DriverTestPage testHomePage;
	
	@BeforeClass(groups={"it"})
	public void initContext(final ITestContext testNGCtx) throws Exception {
		osUtil = new OSUtility();
		SeleniumTestsContextManager.initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}
	
	/**
	 * Forces devMode to false, and check if it closes all the browsers.
	 * test disabled to avoid annoying developer
	 */
	@Test(groups={"it"}, enabled = false)
	public void testDevModeFalse() {
		
		logger.info("Test, dev mode = " + false);
		SeleniumTestsContextManager.getThreadContext().setDevMode(false);
		
		launchPageTest();
		
		if (osUtil.isWebBrowserRunning(true)) {
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
		List<ProcessInfo> webBrowserRunningListBefore = osUtil.whichWebBrowserRunning();
		
		launchPageTest();
		
		List<ProcessInfo> webBrowserRunningListAfter = osUtil.whichWebBrowserRunning();

		Assert.assertEquals(webBrowserRunningListBefore.size(), webBrowserRunningListAfter.size());
	}
	
	/**
	 * launch one basic Selenium test
	 */
	private void launchPageTest(){
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
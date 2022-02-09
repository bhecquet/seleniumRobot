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
package com.seleniumtests.it.core;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.util.logging.SeleniumRobotLogger;
import com.seleniumtests.util.osutility.OSUtility;
import com.seleniumtests.util.osutility.OSUtilityFactory;

public class TestDebug extends GenericTest {

	private static final Logger logger = SeleniumRobotLogger.getLogger(TestDebug.class);
	
	private OSUtility osUtil;
	
	@BeforeClass(groups={"it"})
	public void initContext(final ITestContext testNGCtx) throws Exception {
		osUtil = OSUtilityFactory.getInstance();
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("chrome");
	}
	
	/**
	 * Forces devMode to false, and check if it closes all the browsers.
	 * test disabled to avoid annoying developer
	 */
	@Test(groups={"it"}, enabled = false)
	public void testDevModeFalse() {
		
		logger.info("Test, dev mode = " + false);
		SeleniumTestsContextManager.getThreadContext().setDebug("none");
		
		launchPageTest();
		
		if (osUtil.isWebBrowserRunning(true)) {
			Assert.fail("All web browser processes should be over.");
		}
	}
	
	/**
	 * launch one basic Selenium test
	 */
	private void launchPageTest(){
		try {
			WebUIDriver.getWebDriver(true);
			new DriverTestPage(true);
			DriverTestPage.selectList.selectByText("option2");
			
		} catch (Exception e) {
			logger.error(e);
		} finally {
			if (WebUIDriver.getWebDriver(false) != null) {
				WebUIDriver.cleanUp();
			}
		}
	}
	
}
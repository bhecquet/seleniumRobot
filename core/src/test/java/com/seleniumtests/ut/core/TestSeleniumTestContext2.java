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
package com.seleniumtests.ut.core;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.connectors.selenium.SeleniumRobotVariableServerConnector;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Unit test for correction of issue #65
 * When setting output directory before some TestNG init occured, context is not a testrunner, so ClassCastException was raised
 *
 */
@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class})
public class TestSeleniumTestContext2 extends MockitoTest {

	@BeforeTest(groups="ut")
	public void init() {
		
		// init of properties is done there so that it's taken into account when creating global context 
		System.setProperty(SeleniumTestsContext.OUTPUT_DIRECTORY, "/home/user/test-output");
	}
	@AfterTest(groups="ut")
	public void reset() {
		System.clearProperty(SeleniumTestsContext.OUTPUT_DIRECTORY);
	}
	
	/**
	 * No error should be raised
	 * @param testNGCtx
	 * @param xmlTest
	 */
	@Test(groups="ut")
	public void testOutputDirectoryFromSystem(final ITestContext testNGCtx, final XmlTest xmlTest) {
		Assert.assertTrue(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory().endsWith("/home/user/test-output"));
	}

	
}

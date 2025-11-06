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
package com.seleniumtests.ut.core;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContext;
import com.seleniumtests.core.SeleniumTestsContextManager;

/**
 * Unit test for correction of issue #65
 * When setting output directory before some TestNG init occured, context is not a testrunner, so ClassCastException was raised
 *
 */
//@PrepareForTest({SeleniumRobotVariableServerConnector.class, SeleniumTestsContext.class})
public class TestSeleniumTestContext2 extends GenericTest {

	@BeforeTest(groups="ut context2")
	public void init() {
	
		// init of properties is done there so that it's taken into account when creating global context 
		System.setProperty(SeleniumTestsContext.OUTPUT_DIRECTORY, System.getProperty("java.io.tmpdir") + "/home/user/test-output");
	}
	
	@AfterTest(groups="ut context2")
	public void reset() {
		System.clearProperty(SeleniumTestsContext.OUTPUT_DIRECTORY);
	}
	
	/**
	 * No error should be raised
	 */
	@Test(groups="ut context2")
	public void testOutputDirectoryFromSystem() {
		Assert.assertTrue(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory().replace("\\", "/").endsWith("/home/user/test-output"));
	}

	
}

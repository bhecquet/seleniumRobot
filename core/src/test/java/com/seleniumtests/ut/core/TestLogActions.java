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
package com.seleniumtests.ut.core;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.seleniumtests.GenericDriverTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.it.driver.support.pages.DriverTestPage;
import com.seleniumtests.reporter.logger.TestLogging;
import com.seleniumtests.reporter.logger.TestStep;

public class TestLogActions extends GenericDriverTest {
	

	private DriverTestPage testPage;

	@BeforeMethod(groups={"ut"})
	public void initDriver(final ITestContext testNGCtx) throws Exception {

		TestLogging.reset();
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setBrowser("htmlunit");
		driver = WebUIDriver.getWebDriver(true);
		testPage = new DriverTestPage(true);
		
	}

	/**
	 * Check that when calling a method with password in it, this is masked
	 * parameter name contains 'password' but not only that to check replacement is done on similar strings
	 */
	@Test(groups= {"ut"})
	public void testPassworkMasking() {

		testPage._setPassword("someText");
		TestStep step = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(2);
		
		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 2);
	}
	
	/**
	 * Same as above but parameter name is 'pwd'
	 */
	@Test(groups= {"ut"})
	public void testPassworkMasking2() {
		
		testPage._setPassword2("someText");
		TestStep step = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(2);
		
		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 2);
	}
	
	/**
	 * Check that if a step accepts a variable number of arguments, they are replaced
	 */
	@Test(groups= {"ut"})
	public void testMultiplePassworkMasking() {
		
		testPage._setPasswords("someText", "someOtherText");
		TestStep step = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(2);

		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertFalse(step.toString().contains("someOtherText"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 3);
	}
	
	/**
	 * Check that if a step accepts a variable number of arguments, they are replaced
	 */
	@Test(groups= {"ut"})
	public void testMultiplePassworkMaskingWithList() {
		
		testPage._setPasswords(Arrays.asList("someText", "someOtherText"));
		TestStep step = TestLogging.getTestsSteps().get(TestLogging.getCurrentTestResult()).get(2);
	
		// all occurences of the password have been replaced
		Assert.assertFalse(step.toString().contains("someText"));
		Assert.assertFalse(step.toString().contains("someOtherText"));
		Assert.assertTrue(step.toString().contains("sendKeys on TextFieldElement Text, by={By.id: text2} with args: (true, true, [******,], )"));
		Assert.assertEquals(StringUtils.countMatches(step.toString(), "******"), 3);
	}
	
}

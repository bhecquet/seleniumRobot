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
package com.seleniumtests.ut.reporter;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.utils.TestNGResultUtils;

/**
 * Check that SoftAssert aspect is working and has been weaved
 * @author behe
 *
 */
public class TestCustomAssertion extends GenericTest {

	/**
	 * Test soft assertion enabled, raise an assertion and check it's intercept by aspect
	 */
	@Test(groups={"ut"})
	public void testSoftAssertionEnabled(final ITestContext testNGCtx) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
			Assert.assertTrue(false, "error should not be raised");
		} finally {
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false); // be sure it's false
		}
		
	}
	
	/**
	 * Test soft assertion disabled, check AssertionError is not intercept by aspect
	 */
	@Test(groups={"ut"}, expectedExceptions=AssertionError.class)
	public void testSoftAssertionNotEnabled(final ITestContext testNGCtx) {
		initThreadContext(testNGCtx);
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		Assert.assertTrue(false, "error should be raised");
	}
	
	@Test(groups={"ut"})
	public void testSoftAssertionEnabledWithChangedResult(final ITestContext testNGCtx) {
		try {
			initThreadContext(testNGCtx);
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
			Assert.assertTrue(false, "error should not be raised");
		} finally {
			SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false); // be sure it's false
		}
	}
	
	@AfterMethod(groups={"ut"})
	public void changeTestResult(Method method, ITestResult result) {
		if (method.getName().equals("testSoftAssertionEnabledWithChangedResult")) {
			ITestResult previousResult = Reporter.getCurrentTestResult();
			Reporter.setCurrentTestResult(result);
			try {
				TestNGResultUtils.changeTestResultWithSoftAssertion(result);
			
				// test result should be changed in failure because of soft assertion
				Assert.assertEquals(result.getStatus(), TestResult.FAILURE);
				
				// 
				
				Assert.assertTrue(result.getTestContext().getFailedTests().getAllMethods().contains(result.getMethod()));
			} finally {
				Reporter.setCurrentTestResult(previousResult);
			}
		}
	}
	
	@AfterClass(groups={"ut"}, alwaysRun=true)
	public void clean() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
	}
}

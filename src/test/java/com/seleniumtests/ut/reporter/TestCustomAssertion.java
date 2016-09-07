package com.seleniumtests.ut.reporter;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import com.seleniumtests.GenericTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter;

public class TestCustomAssertion extends GenericTest {

	/**
	 * Test soft assertion enabled, raise an assertion and check it's intercept by aspect
	 */
	@Test(groups={"ut"})
	public void testSoftAssertionEnabled() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
		Assert.assertTrue(false, "error should not be raised");
	}
	
	/**
	 * Test soft assertion disabled, check AssertionError is not intercept by aspect
	 */
	@Test(groups={"ut"}, expectedExceptions=AssertionError.class)
	public void testSoftAssertionNotEnabled() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
		Assert.assertTrue(false, "error should be raised");
	}
	
	@Test(groups={"ut"})
	public void testSoftAssertionEnabledWithChangedResult() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(true);
		Assert.assertTrue(false, "error should not be raised");
	}
	
	@AfterMethod(alwaysRun = true)
	public void changeTestResult(Method method, ITestResult result) {
		if (method.getName().equals("testSoftAssertionEnabledWithChangedResult")) {
			ITestResult previousResult = Reporter.getCurrentTestResult();
			Reporter.setCurrentTestResult(result);
			try {
				new SeleniumTestsReporter().changeTestResult(result);
			
				// test result should be changed in failure because of soft assertion
				Assert.assertEquals(result.getStatus(), TestResult.FAILURE);
				
				// 
				
				Assert.assertTrue(result.getTestContext().getFailedTests().getAllMethods().contains(result.getMethod()));
			} finally {
				Reporter.setCurrentTestResult(previousResult);
			}
		}
	}
	
	@AfterClass(alwaysRun = true)
	public void clean() {
		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
	}
}

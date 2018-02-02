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
package com.seleniumtests.ut.reporter;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriverException;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotTestListener;

public class TestSeleniumTestsReporter extends MockitoTest {
	
	@Mock
	private ITestContext testContext; 
	
	@Mock
	private IResultMap failedTests; 
	@Mock
	private IResultMap skippedTests; 
	@Mock
	private IResultMap passedTests; 
	
	@Mock
	private ITestNGMethod testMethod;
	
	@Mock
	private ITestResult testResult;
	
	private void init() {
		
		Mockito.when(testContext.getName()).thenReturn("a test");
		Mockito.when(testContext.getFailedTests()).thenReturn(failedTests);
		Mockito.when(testContext.getSkippedTests()).thenReturn(skippedTests);
		Mockito.when(testContext.getPassedTests()).thenReturn(passedTests);
		
		Mockito.when(testResult.getTestContext()).thenReturn(testContext);
		Mockito.when(testResult.getMethod()).thenReturn(testMethod);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithoutKo() {

		SeleniumRobotTestListener listener = new SeleniumRobotTestListener();
		listener.changeTestResult(testResult);
		
		// check setStatus has not been called as no verification failure has been provided
		Mockito.verify(testResult, Mockito.never()).setStatus(2);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithOneKo() {
		
		Throwable ex = new WebDriverException("test exception");
		SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), ex);
		init();
		SeleniumRobotTestListener listener = new SeleniumRobotTestListener();
		listener.changeTestResult(testResult);
		
		// check setStatus has not been called as no verification failure has been provided
		Mockito.verify(testResult).setStatus(2);
		Mockito.verify(testResult).setThrowable(ex);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithSeveralKo() {
		
		List<Throwable> throwables = new ArrayList<>();
		throwables.add(new WebDriverException("test exception"));
		throwables.add(new WebDriverException("test exception 2"));
		
		// make this test successful, it will be changed to failed
		List<ITestNGMethod> methods = new ArrayList<>();
		methods.add(testMethod);
		Mockito.when(passedTests.getAllMethods()).thenReturn(methods);
		
		SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), throwables);
		init();
		SeleniumRobotTestListener listener = new SeleniumRobotTestListener();
		listener.changeTestResult(testResult);
		
		// check that the throwable associated to result is not the first declared one
		Mockito.verify(testResult).setStatus(2);
		Mockito.verify(testResult, Mockito.never()).setThrowable(throwables.get(0));
		
		// check that test result has been changed
		Mockito.verify(passedTests).removeResult(testResult);
		Mockito.verify(failedTests).addResult(testResult, testMethod);
	}
}

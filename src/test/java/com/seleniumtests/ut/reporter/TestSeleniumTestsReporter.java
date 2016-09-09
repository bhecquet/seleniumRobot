package com.seleniumtests.ut.reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.WebDriverException;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.seleniumtests.MockitoTest;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.reporter.SeleniumTestsReporter;

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

		SeleniumTestsReporter reporter = new SeleniumTestsReporter();
		reporter.changeTestResult(testResult);
		
		// check setStatus has not been called as no verification failure has been provided
		Mockito.verify(testResult, Mockito.never()).setStatus(2);
	}
	
	@Test(groups={"ut"})
	public void testChangeTestResultWithOneKo() {
		
		Throwable ex = new WebDriverException("test exception");
		SeleniumTestsContextManager.getThreadContext().addVerificationFailures(Reporter.getCurrentTestResult(), ex);
		init();
		SeleniumTestsReporter reporter = new SeleniumTestsReporter();
		reporter.changeTestResult(testResult);
		
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
		SeleniumTestsReporter reporter = new SeleniumTestsReporter();
		reporter.changeTestResult(testResult);
		
		// check that the throwable associated to result is not the first declared one
		Mockito.verify(testResult).setStatus(2);
		Mockito.verify(testResult, Mockito.never()).setThrowable(throwables.get(0));
		
		// check that test result has been changed
		Mockito.verify(passedTests).removeResult(testResult);
		Mockito.verify(failedTests).addResult(testResult, testMethod);
	}
}

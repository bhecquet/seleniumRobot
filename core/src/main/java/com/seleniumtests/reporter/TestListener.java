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
package com.seleniumtests.reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.ResultMap;
import org.testng.internal.TestResult;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.runner.SeleniumRobotRunner;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

/**
 * Class for modifying test results when some events occur (assertion, test retry, ...)
 * @author behe
 *
 */
public class TestListener implements IInvokedMethodListener, ITestListener {
	
	protected static Logger logger = SeleniumRobotLogger.getLogger(TestListener.class);
	
	private Map<String, Boolean> isRetryHandleNeeded = new HashMap<>();
	private Map<String, IResultMap> failedTests = new HashMap<>();
	private Map<String, IResultMap> skippedTests = new HashMap<>();
	private Map<String, IResultMap> passedTests = new HashMap<>();
	
	private static TestListener currentListener;
	
	public TestListener() {
		currentListener = this;
	}

	public static TestListener getCurrentListener() {
		return currentListener;
	}
	
	public Map<String, Boolean> getIsRetryHandleNeeded() {
		return isRetryHandleNeeded;
	}
	
	@Override
	public void beforeInvocation(final IInvokedMethod invokedMethod, final ITestResult result) { 
		TestLogging.setCurrentTestResult(result);
		
		if (invokedMethod.isTestMethod()) {
			if (result.getMethod().getRetryAnalyzer() == null) {
				result.getMethod().setRetryAnalyzer(new TestRetryAnalyzer());
			}
			
			if (SeleniumRobotRunner.isCucumberTest()) {
				result.setAttribute(SeleniumRobotLogger.METHOD_NAME, result.getParameters()[0].toString());
			} else {
				result.setAttribute(SeleniumRobotLogger.METHOD_NAME, invokedMethod.getTestMethod().getMethodName());
			}
			
		}
		
	}
	
	@Override
	public void afterInvocation(final IInvokedMethod method, final ITestResult result) {
		Reporter.setCurrentTestResult(result);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(result);
		}
		
		if (result.getThrowable() != null) {
			logger.error(result.getThrowable().getMessage());
		}
	}
	
	/* ----------------------- ITestListener ------------------------------ */
	@Override
	public void onFinish(final ITestContext arg0) {
		if (isRetryHandleNeeded.get(arg0.getName())) {
			removeIncorrectlySkippedTests(arg0, failedTests.get(arg0.getName()));
			removeFailedTestsInTestNG(arg0);
		} else {
			failedTests.put(arg0.getName(), arg0.getFailedTests());
			skippedTests.put(arg0.getName(), arg0.getSkippedTests());
			passedTests.put(arg0.getName(), arg0.getPassedTests());
		}
	}

	@Override
	public void onStart(final ITestContext arg0) {
		isRetryHandleNeeded.put(arg0.getName(), false);
		failedTests.put(arg0.getName(), new ResultMap());
		skippedTests.put(arg0.getName(), new ResultMap());
		passedTests.put(arg0.getName(), new ResultMap());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) { 
		// overriden
	}
	

	/**
	 * At the end of a failed test. 
	 * Log a screenshot and retry the test.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public synchronized void onTestFailure(final ITestResult testResult) {
		if (testResult.getMethod().getRetryAnalyzer() != null) {
			TestRetryAnalyzer testRetryAnalyzer = (TestRetryAnalyzer) testResult.getMethod().getRetryAnalyzer();

			// test will be retried
			if (testRetryAnalyzer.retryPeek(testResult)) {
				testResult.setStatus(ITestResult.SKIP);
				Reporter.setCurrentTestResult(null);
			} else {
				IResultMap rMap = failedTests.get(testResult.getTestContext().getName());
				rMap.addResult(testResult, testResult.getMethod());
				failedTests.put(testResult.getTestContext().getName(), rMap);
			}

			logger.info(testResult.getMethod() + " Failed in " + testRetryAnalyzer.getCount() + " times");
			isRetryHandleNeeded.put(testResult.getTestContext().getName(), true);
		}

		// capture snap shot
		logLastStep(testResult);
	}

	@Override
	public void onTestSkipped(final ITestResult testResult) {
		// overriden
	}

	@Override
	public void onTestStart(final ITestResult testResult) {
		// overriden
	}
	

	/**
	 * Remove failed test cases in TestNG.
	 *
	 * @param   tc
	 *
	 * @return
	 */
	private void removeFailedTestsInTestNG(final ITestContext tc) {
		IResultMap returnValue = tc.getFailedTests();
		ResultMap removeMap = new ResultMap();
		for (ITestResult result : returnValue.getAllResults()) {
			boolean isFailed = false;
			for (ITestResult resultToCheck : failedTests.get(tc.getName()).getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					isFailed = true;
					break;
				}
			}

			if (!isFailed) {
				logger.info("Removed failed cases:" + result.getMethod().getMethodName());
				removeMap.addResult(result, result.getMethod());
			}
		}

		for (ITestResult result : removeMap.getAllResults()) {
			ITestResult removeResult = null;
			for (ITestResult resultToCheck : returnValue.getAllResults()) {
				if (result.getMethod().equals(resultToCheck.getMethod())
						&& result.getEndMillis() == resultToCheck.getEndMillis()) {
					removeResult = resultToCheck;
					break;
				}
			}

			if (removeResult != null) {
				returnValue.getAllResults().remove(removeResult);
			}
		}
	}

	/**
	 * Remove retrying failed test cases from skipped test cases.
	 *
	 * @param   tc
	 * @param   map
	 *
	 * @return
	 */
	private void removeIncorrectlySkippedTests(final ITestContext tc, final IResultMap map) {
		List<ITestNGMethod> failsToRemove = new ArrayList<>();
		IResultMap returnValue = tc.getSkippedTests();

		for (ITestResult result : returnValue.getAllResults()) {
			for (ITestResult resultToCheck : map.getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}

			for (ITestResult resultToCheck : tc.getPassedTests().getAllResults()) {
				if (resultToCheck.getMethod().equals(result.getMethod())) {
					failsToRemove.add(resultToCheck.getMethod());
					break;
				}
			}
		}

		for (ITestNGMethod method : failsToRemove) {
			returnValue.removeResult(method);
		}

		skippedTests.put(tc.getName(), tc.getSkippedTests());

	}
	
	/**
	 * In case test result is SUCCESS but some softAssertions were raised, change test result to 
	 * FAILED
	 * 
	 * @param result
	 */
	public void changeTestResult(final ITestResult result) {
		List<Throwable> verificationFailures = SeleniumTestsContextManager.getThreadContext().getVerificationFailures(Reporter.getCurrentTestResult());

		int size = verificationFailures.size();
		if (size == 0 || result.getStatus() == TestResult.FAILURE) {
			return;
		}

		result.setStatus(TestResult.FAILURE);

		if (size == 1) {
			result.setThrowable(verificationFailures.get(0));
		} else {
			
			StringBuilder stackString = new StringBuilder("!!! Many Test Failures (").append(size).append(")\n\n");
			
			for (int i = 0; i < size - 1; i++) {
				CommonReporter.generateTheStackTrace(verificationFailures.get(i), String.format("Failure %d of %d%n", i + 1, size), stackString);
			}
			
			Throwable last = verificationFailures.get(size - 1);
			stackString.append(String.format("%n.%nFailure %d of %d%n", size, size));
			stackString.append(last.toString());

			// set merged throwable
			Throwable merged = new AssertionError(stackString.toString());
			merged.setStackTrace(last.getStackTrace());

			result.setThrowable(merged);
		}

		// move test for passedTests to failedTests if test is not already in failed tests
		if (result.getTestContext().getPassedTests().getAllMethods().contains(result.getMethod())) {
			result.getTestContext().getPassedTests().removeResult(result);
			result.getTestContext().getFailedTests().addResult(result, result.getMethod());
		}

	}

	/**
	 * At the end of a successful test. T
	 * Log a screenshot.
	 * 
	 * @param argO
	 * 
	 **/
	@Override
	public void onTestSuccess(final ITestResult testResult) {
		// capture snap shot at the end of the test
		logLastStep(testResult);
	}
	

	/**
	 * On test end, will take a snap shot and store it
	 */
	private void logLastStep(ITestResult testResult) {
		TestStep tearDownStep = new TestStep("Test end");
		TestLogging.setCurrentRootTestStep(tearDownStep);
		TestLogging.log(String.format("Test is %s", testResult.isSuccess() ? "OK": "KO with error: " + testResult.getThrowable().getMessage()));
		
		if (WebUIDriver.getWebDriver(false) != null) {
			for (ScreenShot screenshot: new ScreenshotUtil().captureWebPageSnapshots(true)) {
				TestLogging.logScreenshot(screenshot);
			}
		}
		TestLogging.logTestStep(tearDownStep);
	}
}

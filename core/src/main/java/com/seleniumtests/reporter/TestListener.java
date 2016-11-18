package com.seleniumtests.reporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
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
import org.testng.internal.Utils;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.core.testretry.TestRetryAnalyzer;
import com.seleniumtests.driver.WebUIDriver;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.driver.screenshots.ScreenshotUtil;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class TestListener implements IInvokedMethodListener, ITestListener {
	
	private static final String RESOURCE_LOADER_PATH = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	private String uuid = new GregorianCalendar().getTime().toString();
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


	public Map<String, IResultMap> getFailedTests() {
		return failedTests;
	}

	public Map<String, IResultMap> getSkippedTests() {
		return skippedTests;
	}

	public Map<String, IResultMap> getPassedTests() {
		return passedTests;
	}
	
	public Map<String, Boolean> getIsRetryHandleNeeded() {
		return isRetryHandleNeeded;
	}

	/**
	 * Initializes the VelocityEngine
	 * @return
	 * @throws Exception
	 */
	protected VelocityEngine initVelocityEngine() throws Exception {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", RESOURCE_LOADER_PATH);
		ve.init();
		return ve;
	}
	

	/**
	 * create writer used for writing report file
	 * @param outDir
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected PrintWriter createWriter(final String outDir, final String fileName) throws IOException {
		System.setProperty("file.encoding", "UTF8");
		uuid = uuid.replaceAll(" ", "-").replaceAll(":", "-");

		File f = new File(outDir, fileName);
		logger.info("generating report " + f.getAbsolutePath());

		OutputStream out = new FileOutputStream(f);
		Writer writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
		return new PrintWriter(writer);
	}
	
	@Override
	public void beforeInvocation(final IInvokedMethod arg0, final ITestResult arg1) { 
		TestLogging.setCurrentTestResult(arg1);
	}
	
	@Override
	public void afterInvocation(final IInvokedMethod method, final ITestResult result) {
		Reporter.setCurrentTestResult(result);

		// Handle Soft CustomAssertion
		if (method.isTestMethod()) {
			changeTestResult(result);
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
	 * Remote failed test cases in TestNG.
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
			stackString.append(String.format("\n.\nFailure %d of %d%n", size, size));
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
	 * At the end of a successful test. 
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
			ScreenShot screenShot = new ScreenshotUtil().captureWebPageSnapshot();
			TestLogging.logScreenshot(screenShot, false);
		}
		TestLogging.logTestStep(tearDownStep);
	}
}
